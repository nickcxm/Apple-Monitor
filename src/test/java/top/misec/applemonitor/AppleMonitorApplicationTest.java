package top.misec.applemonitor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import top.misec.applemonitor.config.AppCfg;
import top.misec.applemonitor.config.CfgSingleton;
import top.misec.applemonitor.config.PushConfig;
import top.misec.applemonitor.job.AppleMonitor;
import top.misec.bark.BarkPush;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.pojo.PushDetails;


/**
 * Apple监控应用测试类
 * 
 * 提供各种测试用例，用于测试应用的各项功能
 * 包括配置读取、推送功能、监控逻辑等
 * 
 * @author moshi
 */
@Slf4j
class AppleMonitorApplicationTest {

    /**
     * 空测试方法
     * 用于验证测试环境是否正常加载
     */
    @Test
    void contextLoads() {

    }

    /**
     * 测试配置文件读取和Bark推送功能
     * 
     * 测试流程：
     * 1. 读取测试配置文件
     * 2. 提取推送配置
     * 3. 发送测试推送消息
     */
    @Test
    void test() {
        // 指定测试配置文件名
        String jpCfg = "config-test.json";
        // 获取配置对象
        AppCfg config = getAppCfg(jpCfg);

        System.out.println(config);

        // 获取第一个设备的推送配置
        PushConfig pushConfig = config.getAppleTaskConfig().getDeviceCodeList().get(0).getPushConfigs().get(0);

        // 创建Bark推送实例
        BarkPush barkPush = new BarkPush(pushConfig.getBarkPushUrl(), pushConfig.getBarkPushToken());
        // 构建推送详情
        PushDetails pushDetails = PushDetails.builder()
                .title("苹果商店监控")
                .body("123")
                .category("苹果商店监控")
                .group("Apple Monitor")
                .sound(SoundEnum.MULTIWAYINVITATION.getSoundName())
                .build();
        // 发送推送
        barkPush.simpleWithResp(pushDetails);
        log.info("read config : {}", config);
    }

    /**
     * 测试日本地区配置的推送功能
     */
    @Test
    void pushTest() {
        String jpCfg = "config-jp.json";
        AppCfg config = getAppCfg(jpCfg);
        // 执行监控
        new AppleMonitor().monitor();
    }

    /**
     * 测试本地配置的监控功能
     */
    @Test
    void monitorLocal() {
        String jpCfg = "config-test.json";
        AppCfg config = getAppCfg(jpCfg);
        log.info("monitor local config: {}", config);
        // 执行监控
        new AppleMonitor().monitor();
    }

    /**
     * 测试日本地区的监控功能
     */
    @Test
    void monitorTest() {
        String jpCfg = "config-jp.json";
        AppCfg config = getAppCfg(jpCfg);
        log.info("config jp: {}", config);
        // 执行监控
        new AppleMonitor().monitor();
    }

    /**
     * 测试中国地区的监控功能
     */
    @Test
    void monitorTestCN() {
        String cnCfg = "config.json";
        AppCfg config = getAppCfg(cnCfg);
        log.info("config: {}", config);
        // 执行监控
        new AppleMonitor().monitor();
    }

    /**
     * 测试韩国地区的监控功能
     */
    @Test
    void monitorTestKR() {
        String krCfg = "config-kr.json";
        AppCfg config = getAppCfg(krCfg);
        log.info("config: {}", config);
        // 执行监控
        new AppleMonitor().monitor();
    }


    /**
     * 获取应用配置的辅助方法
     * 
     * @param fileName 配置文件名
     * @return 应用配置对象
     */
    private AppCfg getAppCfg(String fileName) {
        return CfgSingleton.getTestInstance(fileName).config;
    }

}
