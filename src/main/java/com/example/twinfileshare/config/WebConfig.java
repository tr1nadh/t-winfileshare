package com.example.twinfileshare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/dev")
                .setViewName("/web/panel/dev-panel.html");
        registry.addViewController("/dev/user")
                .setViewName("/web/panel/user-panel.html");
        registry.addViewController("/").setViewName("/web/index.html");
        registry.addViewController("/error").setViewName("/web/error.html");
    }
}
