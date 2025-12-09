package top.misec.applemonitor.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;

import java.nio.charset.StandardCharsets;

/**
 * 飞书工具类
 * 
 * 提供飞书机器人相关的工具方法
 * 主要用于生成飞书机器人消息签名，确保消息的安全性
 * 
 * @author Moshi
 * @since 2023/5/10
 */
@Slf4j
public class FeiShuUtils {
    /**
     * 生成飞书机器人消息签名
     * 
     * 飞书机器人的签名验证机制：
     * 1. 将时间戳和密钥拼接成字符串：timestamp + "\n" + secret
     * 2. 使用HmacSHA256算法对字符串进行加密
     * 3. 将加密结果进行Base64编码
     * 
     * 这个签名会随请求一起发送，飞书服务器会验证签名的有效性
     * 
     * @param secret 飞书机器人的密钥
     * @param timestamp 当前时间戳（秒级）
     * @return Base64编码后的签名字符串
     */
    public static String genSign(String secret, long timestamp) {
        // 把timestamp+"\n"+密钥当做签名字符串
        String stringToSign = timestamp + "\n" + secret;

        // 使用HmacSHA256算法计算签名
        byte[] signData = HmacUtils.getInitializedMac("HmacSHA256", stringToSign.getBytes(StandardCharsets.UTF_8))
                .doFinal();
        
        // 对签名结果进行Base64编码并返回
        return new String(Base64.encodeBase64(signData));
    }
}
