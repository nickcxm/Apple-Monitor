package top.misec.applemonitor.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 国家/地区枚举类
 * 
 * 定义支持的国家/地区及其对应的Apple官网URL
 * 用于根据国家代码获取正确的API请求地址
 * 
 * @author moshi
 */
@Getter
@AllArgsConstructor
public enum CountryEnum {
    /**
     * 国家/地区代码 和 对应的Apple官网URL
     */
    CN("CN", "https://www.apple.com.cn"),              // 中国大陆
    CN_HK("CN-HK", "https://www.apple.com/hk"),        // 中国香港
    CN_MO("CN-MO", "https://www.apple.com/mo"),        // 中国澳门
    CN_TW("CN-TW", "https://www.apple.com/tw"),        // 中国台湾
    JP("JP", "https://www.apple.com/jp"),              // 日本
    KR("KR", "https://www.apple.com/kr"),              // 韩国
    SG("SG", "https://www.apple.com/sg"),              // 新加坡
    MY("MY", "https://www.apple.com/my"),              // 马来西亚
    AU("AU", "https://www.apple.com/au"),              // 澳大利亚
    UK("UK", "https://www.apple.com/uk"),              // 英国
    CA("CA", "https://www.apple.com/ca"),              // 加拿大
    US("US", "https://www.apple.com"),                 // 美国

    ;

    /**
     * 国家/地区代码
     */
    final String country;
    
    /**
     * 对应的Apple官网URL
     */
    final String url;

    /**
     * 根据国家代码获取对应的Apple官网URL
     * 
     * @param country 国家/地区代码（如：CN、JP、US等）
     * @return 对应的Apple官网URL，如果找不到则返回中国大陆的URL
     */
    public static String getUrlByCountry(String country) {
        // 遍历所有枚举值
        for (CountryEnum countryEnum : CountryEnum.values()) {
            // 如果找到匹配的国家代码，返回对应的URL
            if (countryEnum.getCountry().equals(country)) {
                return countryEnum.getUrl();
            }
        }
        // 默认返回中国大陆的URL
        return CN.url;
    }
}
