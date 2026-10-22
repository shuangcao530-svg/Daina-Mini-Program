package com.xiaosuda.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    private String appid;
    private String secret;
    private boolean mockLoginEnabled;
}
