package com.slt.peotv.userservice.lms.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Handle circular references
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Or use this to detect and handle circular references
        mapper.enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);

        return mapper;
    }
}
