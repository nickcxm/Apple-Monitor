package top.misec.applemonitor.push.pojo.feishu;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

/**
 * 飞书推送请求体
 * 
 * 用于构建发送到飞书机器人的请求JSON
 * 符合飞书机器人API的消息格式规范
 * 
 * @author Moshi
 * @since 2023/5/10
 */
@Data
@Builder
public class FeiShuPushReq {

    /**
     * 消息类型
     * 支持：text（文本）、post（富文本）、image（图片）等
     */
    @JSONField(name = "msg_type")
    private String msgType;

    /**
     * 消息内容
     * 根据消息类型的不同，内容结构也不同
     * 文本消息使用TextContent封装
     */
    private TextContent content;

    /**
     * 时间戳（秒级）
     * 用于签名计算，防止重放攻击
     */
    private long timestamp;

    /**
     * 消息签名
     * 使用密钥和时间戳生成的签名
     * 飞书服务器会验证签名的有效性
     */
    private String sign;

}
