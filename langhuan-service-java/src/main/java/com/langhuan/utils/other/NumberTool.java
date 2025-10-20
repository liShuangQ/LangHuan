package com.langhuan.utils.other;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NumberTool {
    /**
     * 安全地将Object转换为Double类型
     * 处理Integer、Float、Double等数值类型的转换
     *
     * @param obj          待转换的对象
     * @param defaultValue 默认值
     * @return 转换后的Double值
     */
    public static Double convertToDouble(Object obj, Double defaultValue) {
        if (obj == null) {
            return defaultValue;
        }

        if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        } else if (obj instanceof Float) {
            return ((Float) obj).doubleValue();
        } else if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            log.warn("无法转换类型 {} 为Double，使用默认值 {}", obj.getClass().getSimpleName(), defaultValue);
            return defaultValue;
        }

    }

}
