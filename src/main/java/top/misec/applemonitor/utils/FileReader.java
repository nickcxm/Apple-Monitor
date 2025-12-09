package top.misec.applemonitor.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 文件读取工具类
 * 
 * 提供读取文件内容的静态方法
 * 主要用于读取配置文件（config.json）
 * 
 * @author moshi
 */
@Slf4j
public class FileReader {
    /**
     * 读取指定路径的文件内容
     * 
     * 使用UTF-8编码读取文件内容并返回为字符串
     * 如果文件不存在或读取失败，返回null
     * 
     * @param filePath 文件的完整路径
     * @return 文件内容字符串，如果读取失败则返回null
     */
    public static String readFile(String filePath) {
        String fileContentStr = null;
        try {
            // 创建文件输入流
            InputStream inputStream = new FileInputStream(filePath);
            // 获取文件大小
            int size = inputStream.available();
            // 创建缓冲区
            byte[] buffer = new byte[size];
            // 读取文件内容到缓冲区
            inputStream.read(buffer);
            // 关闭输入流
            inputStream.close();
            // 将字节数组转换为UTF-8字符串
            fileContentStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            // 文件未找到异常
            log.debug("file not found", e);
        } catch (IOException e) {
            // 文件读取异常
            log.warn("file read exception", e);
        }
        return fileContentStr;
    }
}
