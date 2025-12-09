package top.misec.applemonitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import top.misec.applemonitor.config.AppCfg;
import top.misec.applemonitor.config.CfgSingleton;

/**
 * Apple库存监控程序主入口类
 * 
 * 该类负责初始化和启动Apple商店库存监控任务，支持通过cron表达式自定义监控频率
 * 
 * @author moshi
 */

@Slf4j
public class AppleMonitorMain {

    // 用于控制主线程阻塞的可重入锁
    private static final ReentrantLock LOCK = new ReentrantLock();
    // 条件变量，用于使主线程保持运行状态
    private static final Condition STOP = LOCK.newCondition();

    /**
     * 程序主入口方法
     * 
     * 主要执行流程：
     * 1. 加载配置文件
     * 2. 验证配置的有效性
     * 3. 根据监控设备数量推荐合适的cron表达式
     * 4. 初始化并启动定时任务
     * 5. 保持程序运行状态
     * 
     * @param args 命令行参数（暂未使用）
     */
    public static void main(String[] args) {

        // 获取配置单例实例
        AppCfg appCfg = CfgSingleton.getInstance().config;

        // 验证配置是否有效
        if (appCfg.getAppleTaskConfig().valid()) {

            // 获取需要监控的设备型号数量
            int size = appCfg.getAppleTaskConfig().deviceCodeList.size();

            // 根据设备数量计算推荐的cron表达式（每个设备间隔3秒）
            String cronExpress = StrUtil.format("*/{} * * * * ?", size * 3);

            // 提示用户推荐的cron表达式，避免请求过于频繁被限制
            log.info("您本次共监控{}个机型，过短的执行时间间隔会导致请求被限制，建议您的cron表达式设置为:{}", size, cronExpress);

            // 创建定时任务配置
            Setting setting = new Setting();
            // 设置AppleMonitor.monitor方法的执行时间表达式
            setting.set("top.misec.applemonitor.job.AppleMonitor.monitor", appCfg.getAppleTaskConfig().cronExpressions);

            // 应用cron配置
            CronUtil.setCronSetting(setting);
            // 启用秒级匹配（支持秒级cron表达式）
            CronUtil.setMatchSecond(true);
            // 启动定时任务（守护线程模式）
            CronUtil.start(true);
        }

        // 加锁并使主线程保持运行状态，防止程序退出
        LOCK.lock();
        try {
            // 等待被唤醒（实际上会一直等待，直到程序被中断）
            STOP.await();
        } catch (InterruptedException e) {
            log.info("AppleMonitorMain is interrupted");
        } finally {
            // 确保锁被释放
            LOCK.unlock();
        }


    }

}
