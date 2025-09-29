package com.slt.radio.rosterservice.utils;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfiguration {
    @NestedConfigurationProperty
    private Header header = new Header();
    private int expiration = 3600;
    private int reseteExpiration = 600;
    private String privateKey = "qxBEEQv7E8aviX1KUcdOiF5ve5COUPAr";
    private String type = "encrypted";

    public JwtConfiguration() {}

    public JwtConfiguration(Header header, int expiration, int reseteExpiration,
                            String privateKey, String type) {
        this.header = header != null ? header : new Header();
        this.expiration = expiration;
        this.reseteExpiration = reseteExpiration;
        this.privateKey = privateKey != null ? privateKey : "qxBEEQv7E8aviX1KUcdOiF5ve5COUPAr";
        this.type = type != null ? type : "encrypted";
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public int getReseteExpiration() {
        return reseteExpiration;
    }

    public void setReseteExpiration(int reseteExpiration) {
        this.reseteExpiration = reseteExpiration;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "JwtConfiguration{" +
                "header=" + header +
                ", expiration=" + expiration +
                ", reseteExpiration=" + reseteExpiration +
                ", privateKey='" + privateKey + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtConfiguration that = (JwtConfiguration) o;
        return expiration == that.expiration &&
                reseteExpiration == that.reseteExpiration &&
                java.util.Objects.equals(header, that.header) &&
                java.util.Objects.equals(privateKey, that.privateKey) &&
                java.util.Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(header, expiration, reseteExpiration, privateKey, type);
    }

    public static class Header {
        private String name = "Authorization";
        private String prefix = "Bearer ";

        public Header() {}

        public Header(String name, String prefix) {
            this.name = name != null ? name : "Authorization";
            this.prefix = prefix != null ? prefix : "Bearer ";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return "Header{" +
                    "name='" + name + '\'' +
                    ", prefix='" + prefix + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Header header = (Header) o;
            return java.util.Objects.equals(name, header.name) &&
                    java.util.Objects.equals(prefix, header.prefix);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, prefix);
        }
    }
}