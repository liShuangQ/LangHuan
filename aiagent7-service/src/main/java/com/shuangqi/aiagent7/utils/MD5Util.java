package com.shuangqi.aiagent7.utils;

import com.shuangqi.aiagent7.common.Constant;
import org.apache.tomcat.util.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String getMd5(String value) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        // 加盐
        return Base64.encodeBase64String(md5.digest((value + Constant.SALT).getBytes()));
    }

}
