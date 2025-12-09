package top.misec.applemonitor.config;

import lombok.Data;


/**
 * 推送配置类
 * 
 * 包含各种推送方式的配置信息
 * 支持Bark推送（iOS）和飞书机器人推送
 * 
 * @author moshi itning
 */
@SuppressWarnings("all")
@Data
public class PushConfig {

    /**
     * Bark推送服务器地址
     * 默认为：https://api.day.app/push
     * 也可以使用自建的Bark服务器
     */
    public String barkPushUrl;
    
    /**
     * Bark推送令牌
     * 在Bark应用中生成的设备唯一标识
     * 用于标识推送目标设备
     */
    public String barkPushToken;

    /**
     * Bark推送铃声
     * 指定收到通知时播放的铃声
     * 可选值参考Bark支持的铃声列表
     * 例如：glass.caf、multiwayinvitation.caf等
     */
    public String barkPushSound;

    /**
     * 飞书机器人Webhook地址
     * 在飞书群聊中添加自定义机器人后获取的Webhook URL
     */
    public String feishuBotWebhooks;
    
    /**
     * 飞书机器人密钥
     * 飞书机器人的签名校验密钥
     * 用于验证请求的合法性
     */
    public String feishuBotSecret;
}
