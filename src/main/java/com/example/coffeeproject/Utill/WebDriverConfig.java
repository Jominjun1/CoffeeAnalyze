package com.example.coffeeproject.Utill;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

    @Value("${webdriver.chrome.path}")
    private String chromeDriverPath;

    @Bean
    public WebDriver webDriver(){
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        return new ChromeDriver();
    }
}
