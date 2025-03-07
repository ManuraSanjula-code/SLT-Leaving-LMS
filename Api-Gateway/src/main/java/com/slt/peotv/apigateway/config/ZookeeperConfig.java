package com.slt.peotv.apigateway.config;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class ZookeeperConfig {

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";

    @Bean
    public ZooKeeper zooKeeper() throws IOException {
        return new ZooKeeper(ZOOKEEPER_ADDRESS, 3000, event -> {});
    }
}