package top.misec.applemonitor.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.misec.applemonitor.push.impl.FeiShuBotPush;
import top.misec.applemonitor.push.pojo.feishu.FeiShuPushDTO;
import top.misec.bark.BarkPush;
import top.misec.bark.enums.SoundEnum;

import java.util.Collections;
import java.util.List;

/**
 * Apple监控任务配置类
 * 
 * 包含监控任务的所有配置信息，如设备列表、位置、定时表达式、国家代码等
 * 提供配置验证方法，确保配置的完整性和正确性
 * 
 * @author Moshi
 */
@Data
@Slf4j
public class AppleTaskConfig {
    /**
     * 需要监控的设备列表
     * 每个设备包含设备代码、商店白名单、推送配置等信息
     */
    public List<DeviceItem> deviceCodeList;
    
    /**
     * 监控位置
     * 格式：广东 深圳 南山区（中国大陆）或邮政编码（其他国家/地区）
     */
    public String location;
    
    /**
     * 定时任务cron表达式
     * 控制监控任务的执行频率，建议间隔时间为（设备数量 * 3）秒
     */
    public String cronExpressions;
    
    /**
     * 国家/地区代码
     * 支持：CN（中国大陆）、CN-HK（香港）、CN-MO（澳门）、CN-TW（台湾）、
     *      JP（日本）、KR（韩国）、SG（新加坡）、MY（马来西亚）、AU（澳大利亚）、
     *      UK（英国）、CA（加拿大）、US（美国）
     */
    public String country;

    /**
     * 验证配置的有效性
     * 
     * 检查项：
     * 1. 设备型号列表不能为空
     * 2. 监控地区不能为空
     * 3. cron表达式不能为空
     * 4. 国家代码不能为空
     * 5. 为空的商店白名单设置默认值
     * 6. 为空的推送铃声设置默认值
     * 7. 发送测试推送通知
     * 
     * @return 如果配置有效返回true，否则返回false
     */
    public boolean valid() {
        // 验证设备列表不为空
        if (CollectionUtil.isEmpty(deviceCodeList)) {
            log.info("需要监控的设备型号号码不能为空，类似于 MQ0D3CH/A ");
            return false;
        }
        
        // 验证位置不为空
        if (StrUtil.isBlank(location)) {
            log.info("需要监控的地区不能为空，类似于 广东 深圳 南山区 ，请使用苹果官网的地区格式");
            return false;
        }

        // 验证cron表达式不为空
        if (StrUtil.isBlank(cronExpressions)) {
            log.info("监控的时间表达式不能为空，类似于 0 0 0/1 * * ? ");
            return false;
        }

        // 验证国家代码不为空
        if (StrUtil.isBlank(country)) {
            log.info("国家代码不能为空，类似于 CN , JP");
            return false;
        }

        // 处理每个设备的配置
        deviceCodeList.forEach(k -> {
            // 如果商店白名单为空，设置为空列表（表示监控所有商店）
            if (k.getStoreWhiteList() == null) {
                k.setStoreWhiteList(Collections.emptyList());
                log.info("{},需要监控的门店为空，默认监控您附近的所有门店", k.getDeviceCode());
            }
            
            // 处理每个设备的推送配置
            k.getPushConfigs().forEach(push -> {

                // 如果未设置推送铃声，使用默认铃声
                if (StrUtil.isEmpty(push.getBarkPushSound())) {
                    push.setBarkPushSound(SoundEnum.GLASS.getSoundName());
                }

                // 发送启动通知
                log.info("机器人开始干活啦");
                String content = StrUtil.format("您的机器人开始监控{}附近的Apple直营店啦", location);

                // 如果配置了Bark推送，发送启动通知
                if (StrUtil.isAllNotEmpty(push.getBarkPushUrl(), push.getBarkPushToken())) {
                    BarkPush pusher = new BarkPush(push.getBarkPushUrl(), push.getBarkPushToken());
                    pusher.simpleWithResp(content);
                }
                
                // 如果配置了飞书机器人，发送启动通知
                if (StrUtil.isAllNotEmpty(push.getFeishuBotSecret(), push.getFeishuBotWebhooks())) {
                    FeiShuBotPush.pushTextMessage(FeiShuPushDTO.builder()
                            .text(content).secret(push.getFeishuBotSecret())
                            .botWebHooks(push.getFeishuBotWebhooks())
                            .build());
                }

            });
        });


        log.info("配置校验通过，开始监控{}附近的Apple直营店", location);

        return true;

    }
}
