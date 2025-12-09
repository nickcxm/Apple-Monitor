package top.misec.applemonitor.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import top.misec.applemonitor.utils.FileReader;

import java.io.File;

/**
 * 配置单例类
 * 
 * 采用双重检查锁定（Double-Checked Locking）实现的线程安全单例模式
 * 负责读取并解析config.json配置文件，提供全局唯一的配置实例
 * 
 * @author moshi
 */
public class CfgSingleton {

    /**
     * 应用配置对象
     */
    public AppCfg config;

    /**
     * 单例实例，使用volatile保证可见性
     */
    private volatile static CfgSingleton uniqueInstance;

    /**
     * 私有构造函数，防止外部实例化
     * 默认读取当前工作目录下的config.json文件
     */
    private CfgSingleton() {
        // 获取当前工作目录下的config.json文件路径
        String currentPath = System.getProperty("user.dir") + File.separator + "config.json";
        // 读取配置文件内容
        String configStr = FileReader.readFile(currentPath);

        // 将JSON字符串解析为配置对象
        this.config = JSONObject.parseObject(configStr, AppCfg.class);

    }

    /**
     * 私有构造函数（支持自定义配置文件名）
     * 主要用于测试场景
     * 
     * @param fileName 配置文件名，如果为空则使用默认的config.json
     */
    private CfgSingleton(String fileName) {
        // 如果文件名为空，使用默认配置文件名
        if (StrUtil.isBlank(fileName)) {
            fileName = "config.json";
        }
        // 获取配置文件完整路径
        String currentPath = System.getProperty("user.dir") + File.separator + fileName;
        // 读取配置文件内容
        String configStr = FileReader.readFile(currentPath);
        // 解析配置
        this.config = JSONObject.parseObject(configStr, AppCfg.class);
    }

    /**
     * 获取单例实例
     * 使用双重检查锁定确保线程安全和性能
     * 
     * @return 配置单例实例
     */
    public static CfgSingleton getInstance() {

        // 第一次检查，避免不必要的同步
        if (uniqueInstance == null) {
            // 同步代码块
            synchronized (CfgSingleton.class) {
                // 第二次检查，确保只创建一个实例
                if (uniqueInstance == null) {
                    uniqueInstance = new CfgSingleton();
                }
            }
        }
        return uniqueInstance;
    }

    /**
     * 获取测试实例（支持自定义配置文件）
     * 主要用于单元测试，可以指定不同的配置文件
     * 
     * @param fileName 配置文件名
     * @return 配置单例实例
     */
    public static CfgSingleton getTestInstance(String fileName) {

        // 第一次检查
        if (uniqueInstance == null) {
            synchronized (CfgSingleton.class) {
                // 第二次检查
                if (uniqueInstance == null) {
                    uniqueInstance = new CfgSingleton(fileName);
                }
            }
        }
        return uniqueInstance;
    }


}
