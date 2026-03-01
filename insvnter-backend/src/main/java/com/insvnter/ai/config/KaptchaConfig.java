package com.insvnter.ai.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Value("${kaptcha.border:false}")
    private String border;

    @Value("${kaptcha.text-producer-char-length:4}")
    private String charLength;

    @Value("${kaptcha.text-producer-font-size:32}")
    private String fontSize;

    @Value("${kaptcha.image-width:120}")
    private String imageWidth;

    @Value("${kaptcha.image-height:40}")
    private String imageHeight;

    @Bean
    public DefaultKaptcha kaptchaProducer() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", border);
        properties.setProperty("kaptcha.textproducer.char.length", charLength);
        properties.setProperty("kaptcha.textproducer.font.size", fontSize);
        properties.setProperty("kaptcha.image.width", imageWidth);
        properties.setProperty("kaptcha.image.height", imageHeight);
        properties.setProperty("kaptcha.textproducer.font.color", "80,80,160");
        properties.setProperty("kaptcha.noise.color", "100,100,200");
        properties.setProperty("kaptcha.background.clear.from", "245,245,255");
        properties.setProperty("kaptcha.background.clear.to", "230,230,250");
        properties.setProperty("kaptcha.textproducer.char.space", "4");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(new Config(properties));
        return kaptcha;
    }
}
