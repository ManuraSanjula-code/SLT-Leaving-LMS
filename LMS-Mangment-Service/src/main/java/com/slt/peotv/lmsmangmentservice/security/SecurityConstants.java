package com.slt.peotv.lmsmangmentservice.security;

import com.slt.peotv.lmsmangmentservice.SpringApplicationContext;
import com.slt.peotv.lmsmangmentservice.utils.AppProperties;

public class SecurityConstants {
    public static final long EXPIRATION_TIME = 864000000; // 10 days
    public static final long PASSWORD_RESET_EXPIRATION_TIME = 3600000; // 1 hour
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String PRODUCTS = "/products/**";
    public static final String COMMENT = "/products/comments/**";
    public static String getTokenSecret()
    {
        AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("AppProperties");
        return appProperties.getTokenSecret();
    }
    
}
