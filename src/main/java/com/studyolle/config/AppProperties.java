package com.studyolle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app")
public class AppProperties {
    // application.properties에 있는 app host를 가져옴
    private String host;
}
