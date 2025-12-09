package top.misec.applemonitor.push.pojo.feishu;

import lombok.Builder;
import lombok.Data;

/**
 * 飞书文本消息内容
 * 
 * 封装文本类型消息的内容
 * 用于飞书机器人发送纯文本消息
 * 
 * @author Moshi
 * @since 2023/5/10
 */
@Data
@Builder
public class TextContent {
    /**
     * 文本消息内容
     * 支持纯文本格式
     */
    private String text;
}
