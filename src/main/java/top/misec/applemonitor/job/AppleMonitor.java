package top.misec.applemonitor.job;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.misec.applemonitor.config.*;
import top.misec.applemonitor.push.impl.FeiShuBotPush;
import top.misec.applemonitor.push.pojo.feishu.FeiShuPushDTO;
import top.misec.bark.BarkPush;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.pojo.PushDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Apple商店库存监控核心类
 * 
 * 该类实现了Apple线下商店库存监控的核心功能，包括：
 * 1. 定期查询Apple官网API获取商店库存信息
 * 2. 根据配置过滤需要监控的商店
 * 3. 当有库存时通过Bark或飞书机器人推送通知
 * 
 * @author MoshiCoCo
 */
@Slf4j
public class AppleMonitor {
    // 全局配置实例，包含监控任务配置、推送配置等
    private final AppCfg CONFIG = CfgSingleton.getInstance().config;


    /**
     * 监控入口方法（被cron定时任务调用）
     * 
     * 遍历配置的所有设备型号，依次进行监控
     * 每个设备监控完成后休眠1.5秒，避免请求过于频繁
     */
    public void monitor() {

        // 获取需要监控的设备列表
        List<DeviceItem> deviceItemList = CONFIG.getAppleTaskConfig().getDeviceCodeList();

        try {
            // 遍历每个需要监控的设备型号
            for (DeviceItem deviceItem : deviceItemList) {
                // 执行具体的监控逻辑
                doMonitor(deviceItem);
                // 休眠1.5秒，避免请求过于频繁导致被限制
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            log.error("AppleMonitor Error", e);
        }
    }


    /**
     * 通过配置的所有推送渠道发送通知
     * 
     * 支持的推送方式：
     * 1. Bark推送（iOS通知）
     * 2. 飞书机器人推送
     * 
     * @param content 推送的消息内容
     * @param pushConfigs 推送配置列表
     */
    public void pushAll(String content, List<PushConfig> pushConfigs) {

        pushConfigs.forEach(push -> {

            // 如果配置了Bark推送，则通过Bark发送通知
            if (StrUtil.isAllNotEmpty(push.getBarkPushUrl(), push.getBarkPushToken())) {
                BarkPush barkPush = new BarkPush(push.getBarkPushUrl(), push.getBarkPushToken());
                // 构建推送详情
                PushDetails pushDetails= PushDetails.builder()
                        .title("苹果商店监控")
                        .body(content)
                        .category("苹果商店监控")
                        .group("Apple Monitor")
                        .sound(StrUtil.isEmpty(push.getBarkPushSound()) ? SoundEnum.GLASS.getSoundName() : push.getBarkPushSound())
                        .build();
                barkPush.simpleWithResp(pushDetails);
            }
            
            // 如果配置了飞书机器人，则通过飞书发送通知
            if (StrUtil.isAllNotEmpty(push.getFeishuBotSecret(), push.getFeishuBotWebhooks())) {

                FeiShuBotPush.pushTextMessage(FeiShuPushDTO.builder()
                        .text(content).secret(push.getFeishuBotSecret())
                        .botWebHooks(push.getFeishuBotWebhooks())
                        .build());
            }
        });

    }

    /**
     * 执行具体设备的库存监控逻辑
     * 
     * 主要流程：
     * 1. 构建查询参数和请求头
     * 2. 调用Apple官网API查询库存信息
     * 3. 解析返回的商店列表
     * 4. 根据白名单过滤商店
     * 5. 检查是否有库存
     * 6. 如果有库存，发送推送通知
     * 
     * @param deviceItem 需要监控的设备信息，包含设备代码、商店白名单、推送配置等
     */
    public void doMonitor(DeviceItem deviceItem) {

        // 构建查询参数Map
        Map<String, Object> queryMap = new HashMap<>(5);
        queryMap.put("pl", "true");  // 参数：pl
        queryMap.put("mts.0", "regular");  // 消息类型：常规
        queryMap.put("parts.0", deviceItem.getDeviceCode());  // 设备型号代码
        queryMap.put("location", CONFIG.getAppleTaskConfig().getLocation());  // 位置信息

        // 根据国家代码获取对应的Apple官网基础URL
        String baseCountryUrl = CountryEnum.getUrlByCountry(CONFIG.getAppleTaskConfig().getCountry());

        // 构建请求头
        Map<String, List<String>> headers = buildHeaders(baseCountryUrl, deviceItem.getDeviceCode());

        // 拼接完整的查询URL
        String url = baseCountryUrl + "/shop/fulfillment-messages?" + URLUtil.buildQuery(queryMap, CharsetUtil.CHARSET_UTF_8);

        try {
            JSONObject responseJsonObject;
            // 发送HTTP GET请求查询库存信息
            try (HttpResponse httpResponse = HttpRequest.get(url).header(headers).execute()) {
                // 检查响应状态
                if (!httpResponse.isOk()) {
                    log.info("请求过于频繁，请调整cronExpressions，建议您参考推荐的cron表达式");
                    return;
                }

                // 解析响应JSON
                responseJsonObject = JSONObject.parseObject(httpResponse.body());
            }

            // 提取取货信息
            JSONObject pickupMessage = responseJsonObject.getJSONObject("body").getJSONObject("content").getJSONObject("pickupMessage");

            // 获取商店列表
            JSONArray stores = pickupMessage.getJSONArray("stores");

            // 验证商店列表是否存在
            if (stores == null) {
                log.info("您可能填错产品代码了，目前仅支持监控中国和日本地区的产品，注意不同国家的机型型号不同，下面是是错误信息");
                log.debug(pickupMessage.toString());
                return;
            }

            // 检查是否有附近的Apple直营店
            if (stores.isEmpty()) {
                log.info("您所在的 {} 附近没有Apple直营店，请检查您的地址是否正确", CONFIG.getAppleTaskConfig().getLocation());
            }

            // 过滤商店列表并处理每个商店的库存信息
            stores.stream().filter(store -> {
                // 如果白名单为空，监控所有商店
                if (deviceItem.getStoreWhiteList().isEmpty()) {
                    return true;
                } else {
                    // 否则只监控白名单中的商店
                    return filterStore((JSONObject) store, deviceItem);
                }
            }).forEach(k -> {

                JSONObject storeJson = (JSONObject) k;

                // 获取零件库存信息
                JSONObject partsAvailability = storeJson.getJSONObject("partsAvailability");

                // 提取商店名称、设备名称和库存状态
                String storeNames = storeJson.getString("storeName").trim();
                String deviceName = partsAvailability.getJSONObject(deviceItem.getDeviceCode()).getJSONObject("messageTypes").getJSONObject("regular").getString("storePickupProductTitle");
                String productStatus = partsAvailability.getJSONObject(deviceItem.getDeviceCode()).getString("pickupSearchQuote");

                // 构建基础消息内容
                String strTemp = "门店:{},型号:{},状态:{}";
                String content = StrUtil.format(strTemp, storeNames, deviceName, productStatus);

                // 判断商店是否有库存
                if (judgingStoreInventory(storeJson, deviceItem.getDeviceCode())) {
                    // 如果有库存，添加取货信息
                    JSONObject retailStore = storeJson.getJSONObject("retailStore");
                    content += buildPickupInformation(retailStore);
                    log.info(content);

                    // 发送推送通知
                    pushAll(content, deviceItem.getPushConfigs());

                }
                // 记录监控信息
                log.info(content);
            });

        } catch (Exception e) {
            log.error("AppleMonitor error", e);
        }

    }


    /**
     * 判断商店是否有库存
     *
     * @param storeJson   商店信息JSON对象
     * @param productCode 产品代码
     * @return 如果状态为"available"返回true，否则返回false
     */
    private boolean judgingStoreInventory(JSONObject storeJson, String productCode) {

        // 获取零件库存信息
        JSONObject partsAvailability = storeJson.getJSONObject("partsAvailability");
        // 获取取货显示状态
        String status = partsAvailability.getJSONObject(productCode).getString("pickupDisplay");
        // 判断是否可用
        return "available".equals(status);

    }

    /**
     * 构建取货信息字符串
     *
     * @param retailStore 零售商店信息JSON对象
     * @return 格式化后的取货信息字符串，包含地址、电话和距离
     */
    private String buildPickupInformation(JSONObject retailStore) {
        // 获取距离信息（带单位）
        String distanceWithUnit = retailStore.getString("distanceWithUnit");
        // 获取两行格式的地址
        String twoLineAddress = retailStore.getJSONObject("address").getString("twoLineAddress");
        if (StrUtil.isEmpty(twoLineAddress)) {
            twoLineAddress = "暂无取货地址";
        }

        // 获取日间联系电话
        String daytimePhone = retailStore.getJSONObject("address").getString("daytimePhone");
        if (StrUtil.isEmpty(daytimePhone)) {
            daytimePhone = "暂无联系电话";
        }

        // 获取配置的位置信息
        String lo = CONFIG.getAppleTaskConfig().getLocation();
        // 构建消息模板
        String messageTemplate = "\n取货地址:{},电话:{},距离{}:{}";
        // 格式化并返回取货信息（移除地址中的换行符）
        return StrUtil.format(messageTemplate, twoLineAddress.replace("\n", " "), daytimePhone, lo, distanceWithUnit);
    }

    /**
     * 根据白名单过滤商店
     * 
     * 支持模糊匹配：商店名称包含白名单关键词，或白名单关键词包含商店名称
     *
     * @param storeInfo  商店信息JSON对象
     * @param deviceItem 设备项，包含商店白名单
     * @return 如果商店在白名单中返回true，否则返回false
     */
    private boolean filterStore(JSONObject storeInfo, DeviceItem deviceItem) {
        String storeName = storeInfo.getString("storeName");
        // 使用流式处理判断商店名称是否匹配白名单中的任意一项
        return deviceItem.getStoreWhiteList().stream().anyMatch(k -> storeName.contains(k) || k.contains(storeName));
    }

    /**
     * 构建HTTP请求头
     *
     * @param baseCountryUrl 国家对应的Apple官网基础URL
     * @param productCode    产品代码
     * @return 包含Referer等信息的请求头Map
     */
    private Map<String, List<String>> buildHeaders(String baseCountryUrl, String productCode) {

        // 构建Referer头，模拟从购买页面发起的请求
        ArrayList<String> referer = new ArrayList<>();
        referer.add(baseCountryUrl + "/shop/buy-iphone/iphone-14-pro/" + productCode);

        // 创建请求头Map并添加Referer
        Map<String, List<String>> headers = new HashMap<>(10);
        headers.put(Header.REFERER.getValue(), referer);

        return headers;
    }
}
