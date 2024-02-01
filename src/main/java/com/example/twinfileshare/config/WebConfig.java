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
        final String web_panel = "/web/panel";
        registry.addViewController("/dev")
                .setViewName(web_panel + "/dev-panel.html");
        registry.addViewController("/dev/user")
                .setViewName(web_panel + "/user-panel.html");

        final String web = "/web";
        registry.addViewController("/").setViewName(web + "/index.html");
        registry.addViewController("/error").setViewName(web + "/error.html");
    }
}
