package top.misec.applemonitor.push.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.misec.applemonitor.push.pojo.feishu.FeiShuPushDTO;
import top.misec.applemonitor.push.pojo.feishu.FeiShuPushReq;
import top.misec.applemonitor.push.pojo.feishu.TextContent;
import top.misec.applemonitor.utils.FeiShuUtils;

/**
 * 飞书机器人推送实现类
 * 
 * 提供通过飞书机器人发送消息通知的功能
 * 支持文本消息推送，并使用签名验证确保消息安全性
 * 
 * @author Moshi
 * @since 2023/5/10
 */
@Slf4j
public class FeiShuBotPush {

    /**
     * 推送文本消息到飞书群聊
     * 
     * 功能说明：
     * 1. 生成当前时间戳
     * 2. 使用密钥和时间戳生成签名
     * 3. 构建推送请求体（包含消息内容、时间戳和签名）
     * 4. 发送POST请求到飞书机器人Webhook地址
     * 5. 记录推送结果
     * 
     * @param feiShuPushDTO 飞书推送数据传输对象，包含消息内容、Webhook地址和密钥
     */

    public static void pushTextMessage(FeiShuPushDTO feiShuPushDTO) {
        // 获取当前时间戳（秒级）
        long timestamp = System.currentTimeMillis() / 1000;
        
        try (
            // 发送POST请求到飞书机器人Webhook
            HttpResponse httpResponse = HttpRequest.post(feiShuPushDTO.getBotWebHooks())
                // 构建请求体JSON
                .body(JSONObject.toJSONString(FeiShuPushReq.builder()
                        // 设置消息内容
                        .content(TextContent.builder().text(feiShuPushDTO.getText()).build())
                        // 设置时间戳
                        .timestamp(timestamp)
                        // 设置消息类型为文本
                        .msgType("text")
                        // 生成并设置签名
                        .sign(FeiShuUtils.genSign(feiShuPushDTO.getSecret(), timestamp))
                        .build()))
                // 执行请求
                .execute()
        ) {
            // 记录推送响应状态
            log.info("飞书机器人推送状态:{}", httpResponse.getStatus());
            // 记录响应内容
            log.info(httpResponse.body());
        }
    }
}
