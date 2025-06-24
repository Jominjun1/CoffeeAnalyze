package com.example.coffeeproject.Utill.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class WebDriverConfig {
    public WebDriver create() {
        System.setProperty("webdriver.chrome.driver", new File("src/main/resources/chromedriver-win32/chromedriver.exe").getAbsolutePath());
        return new ChromeDriver();
    }
}
