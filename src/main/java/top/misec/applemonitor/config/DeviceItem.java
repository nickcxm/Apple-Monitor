package top.misec.applemonitor.config;

import lombok.Data;


import java.util.List;

/**
 * 设备项配置类
 * 
 * 表示单个需要监控的设备配置信息
 * 包含设备代码、商店白名单、推送配置等
 * 
 * @author Moshi
 */
@Data
public class DeviceItem {
    /**
     * 设备型号代码
     * 例如：MTQA3CH/A（iPhone 14 Pro Max 深空黑色 128GB）
     * 不同国家/地区的设备代码可能不同
     */
    private String deviceCode;
    
    /**
     * 商店白名单
     * 仅监控白名单中的商店，支持模糊匹配
     * 如果为空列表，则监控所有附近的商店
     * 例如：["益田假日", "万象城"]
     */
    public List<String> storeWhiteList;
    
    /**
     * 推送配置列表
     * 支持配置多个推送渠道（Bark、飞书机器人等）
     */
    private List<PushConfig> pushConfigs;
}
