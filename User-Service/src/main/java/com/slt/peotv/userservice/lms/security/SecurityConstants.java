package com.slt.peotv.userservice.lms.security;

import com.slt.peotv.userservice.lms.SpringApplicationContext;

public class SecurityConstants {
    public static final long EXPIRATION_TIME = 864000000; // 10 days
    public static final long PASSWORD_RESET_EXPIRATION_TIME = 3600000; // 1 hour
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ALL_USERS = "/users/all";
    public static final String USERS = "/users";

    public static final String VERIFICATION_EMAIL_URL = "/users/email-verification";
    public static final String UPLOAD_CSV_URL = "/users/api/upload/csv";
    public static final String UPLOAD_JSON_URL = "/users/api/upload/json";
    public static final String PASSWORD_RESET_REQUEST_URL = "/users/password-reset-request";
    public static final String PASSWORD_RESET_URL = "/users/password-reset";
    public static final String IMAGE = "/users/image/**";
    public static final String GET_ROLE = "/users/get-role/**";
    
    public static final String GET_ROLE_ = "/users/roles";
    public static final String GET_PROFILE = "/users/profile";
    public static final String GET_SECTION = "/users/sections";
    public static final String GET_AUTH = "/users/authorities";
    public static final String GET_All_NAMES_ROLE = "/users/names/roles";
    public static final String GET_All_NAMES_SECTIONS = "/users/names/sections";
    public static final String GET_All_NAMES_PROFILES = "/users/names/profiles";

    public static final String CHECK_ = "/users/check/**";
    public static final String TEMP_USERS = "/users/temp";

    public static String getTokenSecret()
    {
        AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("AppProperties");
        return appProperties.getTokenSecret();
    }

}
