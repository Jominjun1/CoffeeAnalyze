package com.example.coffeeproject.service;

import com.example.coffeeproject.model.Ingredient;
import com.example.coffeeproject.model.PaiksCoffee;
import com.example.coffeeproject.respoitory.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CoffeeService {

    private final ATwosomePlaceRepository atwosomePlaceRepository;
    private final ComposeRepository composeRepository;
    private final EDIYARepository ediyaRepository;
    private final IngredientRepository ingredientRepository;
    private final MegaRepository megaRepository;
    private final PaiksRepository paiksRepository;
    private final StarbucksRepository starbucksRepository;
    private final WebDriver driver;

    @Autowired
    public CoffeeService(ATwosomePlaceRepository atwosomePlaceRepository, ComposeRepository composeRepository,
                         EDIYARepository ediyaRepository, IngredientRepository ingredientRepository, MegaRepository megaRepository,
                         PaiksRepository paiksRepository, StarbucksRepository starbucksRepository , WebDriver driver) {
        this.atwosomePlaceRepository = atwosomePlaceRepository;
        this.composeRepository = composeRepository;
        this.ediyaRepository = ediyaRepository;
        this.ingredientRepository = ingredientRepository;
        this.megaRepository = megaRepository;
        this.paiksRepository = paiksRepository;
        this.starbucksRepository = starbucksRepository;
        this.driver = driver;
    }


    // 빽다방 크롤링
    public void crawlPaiksCoffee() throws InterruptedException {
        Map<String , String> categoryMap = Map.of(
                "커피" , "https://paikdabang.com/menu/menu_coffee/",
                "음료" , "https://paikdabang.com/menu/menu_drink/",
                "디저트" , "https://paikdabang.com/menu/menu_dessert/",
                "빽스치노" , "https://paikdabang.com/menu/menu_ccino/"
        );

        for(Map.Entry<String , String> entry : categoryMap.entrySet()) {
            String category = entry.getKey();
            String url = entry.getValue();
            driver.get(url);
            Thread.sleep(2000);

            List<WebElement> menuItems = driver.findElements(By.cssSelector(".menu-list li .menu_link"));

            for (WebElement menuItem : menuItems) {
                try {
                    String detailUrl = menuItem.getAttribute("href");
                    driver.get(detailUrl);
                    Thread.sleep(2000);

                    String name = driver.findElement(By.cssSelector("h3.font-bl")).getText();
                    String engName = driver.findElement(By.cssSelector("div.menu_tit2.color-1")).getText();
                    String note = driver.findElement(By.cssSelector("p.txt")).getText();

                    String imageUrl = driver.findElement(By.cssSelector("div.thumb img")).getAttribute("src");

                    String allergic = "";
                    try {
                        allergic = driver.findElement(By.cssSelector("div.ingredient_table_box")).getText();
                    } catch (NoSuchElementException e) {
                        allergic = "알레르기 정보 없음";
                    }

                    double kcal = 0, caffeine = 0, sodium = 0, sugar = 0, saturatedFat = 0, protein = 0;
                    try {
                        WebElement table = driver.findElement(By.cssSelector("table.ingredient_table"));
                        List<WebElement> rows = table.findElements(By.tagName("tr"));
                        for (WebElement row : rows) {
                            String label = row.findElement(By.tagName("th")).getText();
                            String valueStr = row.findElement(By.tagName("td")).getText().replaceAll("[^0-9.]", "");
                            double value = valueStr.isEmpty() ? 0 : Double.parseDouble(valueStr);
                            switch (label) {
                                case "칼로리":
                                    kcal = value; break;
                                case "카페인":
                                    caffeine = value; break;
                                case "나트륨":
                                    sodium = value; break;
                                case "당류":
                                    sugar = value; break;
                                case "포화지방":
                                    saturatedFat = value; break;
                                case "단백질":
                                    protein = value; break;
                            }
                        }
                    } catch (NoSuchElementException e) {
                        System.out.println("해당 요소가 존재하지 않습니다: " + e.getMessage());
                    }
                    Ingredient ingredient = new Ingredient();
                    ingredient.setKcal(kcal);
                    ingredient.setCaffeine(caffeine);
                    ingredient.setSodium(sodium);
                    ingredient.setSugar(sugar);
                    ingredient.setSaturated_fat(saturatedFat);
                    ingredient.setProtein(protein);
                    ingredient.setAllergic_ingredients(allergic);

                    ingredientRepository.save(ingredient);

                    PaiksCoffee coffee = new PaiksCoffee();
                    coffee.setName(name);
                    coffee.setEng_name(engName);
                    coffee.setImageUrl(imageUrl);
                    coffee.setNote("[" + category + "] " + note);
                    coffee.setPrice(0);
                    coffee.setOunce(0);
                    coffee.setIngredients(ingredient);

                    paiksRepository.save(coffee);

                    driver.get(url);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.out.println("[" + category + "] 실패 :" + e.getMessage());
                    driver.get(url);
                    Thread.sleep(2000);
                }
            }
        }
        driver.quit();
    }
}
