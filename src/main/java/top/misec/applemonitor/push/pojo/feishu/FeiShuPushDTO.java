package top.misec.applemonitor.push.pojo.feishu;

import lombok.Builder;
import lombok.Data;

/**
 * 飞书推送数据传输对象
 * 
 * 用于封装飞书机器人推送所需的参数
 * 包含消息文本、Webhook地址和密钥信息
 * 
 * @author moshi
 */
@Data
@Builder
public class FeiShuPushDTO {
    /**
     * 推送的文本消息内容
     */
    private String text;
    
    /**
     * 飞书机器人的Webhook地址
     * 在飞书群聊中添加自定义机器人后获取
     */
    private String botWebHooks;
    
    /**
     * 飞书机器人的签名密钥
     * 用于生成消息签名，验证请求的合法性
     */
    private String secret;

}
