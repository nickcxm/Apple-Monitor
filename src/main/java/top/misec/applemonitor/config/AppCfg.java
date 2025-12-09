package top.misec.applemonitor.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 应用配置类
 * 
 * 该类是应用程序的顶层配置容器，包含所有监控任务相关的配置信息
 * 
 * @author Moshi
 */
@Data
@Slf4j
public class AppCfg {
    /**
     * Apple监控任务配置
     * 包含监控的设备列表、位置、定时表达式、国家代码等信息
     */
    private AppleTaskConfig appleTaskConfig;
}
