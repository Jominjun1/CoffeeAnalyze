package com.example.coffeeproject.Coffee.service;

import com.example.coffeeproject.Coffee.Model.EDIYACoffee;
import com.example.coffeeproject.Coffee.Repository.EDIYARepository;
import com.example.coffeeproject.Utill.util.WebDriverConfig;
import com.example.coffeeproject.Coffee.Model.Ingredient;
import com.example.coffeeproject.Coffee.Model.MegaCoffee;
import com.example.coffeeproject.Coffee.Model.PaiksCoffee;
import com.example.coffeeproject.Coffee.Repository.IngredientRepository;
import com.example.coffeeproject.Coffee.Repository.MegaRepository;
import com.example.coffeeproject.Coffee.Repository.PaiksRepository;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.regex.*;

@Service
public class CoffeeService {

    private final IngredientRepository ingredientRepository;
    private final PaiksRepository paiksRepository;
    private final MegaRepository  megaRepository;
    private final WebDriverConfig webDriverConfig;
    private final EDIYARepository eDIYARepository;

    @Autowired
    public CoffeeService(IngredientRepository ingredientRepository, PaiksRepository paiksRepository,
                         MegaRepository megaRepository, WebDriverConfig webDriverConfig, EDIYARepository eDIYARepository){
        this.ingredientRepository = ingredientRepository;
        this.paiksRepository = paiksRepository;
        this.megaRepository = megaRepository;
        this.webDriverConfig = webDriverConfig;
        this.eDIYARepository = eDIYARepository;
    }

    public void crawlPaiksCoffee(){
        WebDriver webDriver = webDriverConfig.create();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        Map<String, String> categoryMap = Map.of(
                "커피", "https://paikdabang.com/menu/menu_coffee/",
                "음료", "https://paikdabang.com/menu/menu_drink/",
                "디저트", "https://paikdabang.com/menu/menu_dessert/",
                "빽스치노", "https://paikdabang.com/menu/menu_ccino/"
        );

        try{
            for(Map.Entry<String, String> entry : categoryMap.entrySet()){
                String category = entry.getKey();
                String url = entry.getValue();

                webDriver.get(url);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.menu_list.clear > ul > li")));
                List<WebElement> menuItems = webDriver.findElements(By.cssSelector("div.menu_list.clear > ul > li"));
                System.out.println("크롤링한 메뉴 개수: " + menuItems.size());

                for(WebElement item : menuItems){
                    try{
                        Actions actions = new Actions(webDriver);
                        actions.moveToElement(item).perform();
                        Thread.sleep(300);
                        WebElement hover = item.findElement(By.cssSelector("div.hover"));

                        JavascriptExecutor js = (JavascriptExecutor) webDriver;
                        js.executeScript("arguments[0].style.display='block';", hover);

                        String name = item.findElement(By.cssSelector("p.menu_tit")).getText();
                        String engName = hover.findElement(By.cssSelector("div.menu_tit2.color-1")).getText();
                        String note = hover.findElement(By.cssSelector("p.txt")).getText();
                        String imageUrl = item.findElement(By.cssSelector("div.thumb img")).getAttribute("src");
                        double ounce =0;
                        String allergic = "알레르기 정보 없음";
                        try{
                            List<WebElement> baseInfos = hover.findElements(By.cssSelector("p.menu_ingredient_basis"));
                            for (WebElement baseInfo : baseInfos) {
                                String text = baseInfo.getText();
                                if (text.contains("1회")) {
                                    Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(oz|ml)");
                                    Matcher matcher = pattern.matcher(text);
                                    if (matcher.find()) {
                                        double value = Double.parseDouble(matcher.group(1));
                                        String unit = matcher.group(3).toLowerCase();
                                        ounce = switch (unit) {
                                            case "oz" -> Math.round(value * 29.5 * 10) / 10.0;
                                            case "ml" -> Math.round(value * 10) / 10.0;
                                            case "g" -> value;
                                            default -> ounce;
                                        };
                                    }
                                    break;
                                }
                            }
                            String fullInfo = hover.findElement(By.cssSelector("div.ingredient_table_box")).getText();
                            String[] lines = fullInfo.split("\\r?\\n");
                            allergic = Arrays.stream(lines)
                                    .filter(line -> line.contains("알레르기"))
                                    .findFirst()
                                    .orElse("알레르기 정보 없음");
                        }catch(NoSuchElementException e){
                            System.out.println("정보 없음");
                        }
                        double kcal = 0, caffeine = 0, sodium = 0, sugar = 0, saturatedFat = 0, protein = 0;
                        try{
                            List<WebElement> rows = hover.findElements(By.cssSelector("ul.ingredient_table > li"));
                            for(WebElement row : rows){
                                String text = row.getText().trim();
                                if(text.startsWith("칼로리")){
                                    kcal = extractNumber(text);
                                }else if(text.startsWith("카페인")){
                                    caffeine = extractNumber(text);
                                }else if(text.startsWith("나트륨")){
                                    sodium = extractNumber(text);
                                }else if(text.startsWith("당류")){
                                    sugar = extractNumber(text);
                                }else if(text.startsWith("포화지방")){
                                    saturatedFat = extractNumber(text);
                                }else if(text.startsWith("단백질")){
                                    protein = extractNumber(text);
                                }
                            }
                        }catch(NoSuchElementException e){
                            System.out.println("영양정보 없음: " + name);
                        }
                        Optional<PaiksCoffee> optPaiks = paiksRepository.findByName(name);
                        if(optPaiks.isPresent()){
                            PaiksCoffee paiksCoffee = optPaiks.get();
                            Ingredient ingredient = paiksCoffee.getIngredients();

                            boolean isDifferent =
                                    Math.abs(ingredient.getKcal() - kcal) > 0.001 ||
                                            Math.abs(ingredient.getCaffeine() - caffeine) > 0.001 ||
                                            Math.abs(ingredient.getSodium() - sodium) > 0.001 ||
                                            Math.abs(ingredient.getSugar() - sugar) > 0.001 ||
                                            Math.abs(ingredient.getSaturated_fat() - saturatedFat) > 0.001 ||
                                            Math.abs(ingredient.getProtein() - protein) > 0.001 ||
                                            !Objects.equals(ingredient.getAllergic_ingredients(), allergic);

                            if(isDifferent){
                                ingredient.setKcal(kcal);
                                ingredient.setCaffeine(caffeine);
                                ingredient.setSodium(sodium);
                                ingredient.setSugar(sugar);
                                ingredient.setSaturated_fat(saturatedFat);
                                ingredient.setProtein(protein);
                                ingredient.setAllergic_ingredients(allergic);
                                ingredientRepository.save(ingredient);

                                paiksCoffee.setNote("[" + category + "] " + note);
                                paiksCoffee.setImageUrl(imageUrl);
                                paiksCoffee.setOunce(ounce);
                                paiksRepository.save(paiksCoffee);
                            }
                        }else{
                            Ingredient ingredient = new Ingredient();
                            ingredient.setKcal(kcal);
                            ingredient.setCaffeine(caffeine);
                            ingredient.setSodium(sodium);
                            ingredient.setSugar(sugar);
                            ingredient.setSaturated_fat(saturatedFat);
                            ingredient.setProtein(protein);
                            ingredient.setAllergic_ingredients(allergic);
                            ingredientRepository.save(ingredient);

                            PaiksCoffee paiksCoffee = new PaiksCoffee();
                            paiksCoffee.setName(name);
                            paiksCoffee.setEng_name(engName);
                            paiksCoffee.setOunce(ounce);
                            paiksCoffee.setImageUrl(imageUrl);
                            paiksCoffee.setNote("[" + category + "] " + note);
                            paiksCoffee.setPrice(0);
                            paiksCoffee.setIngredients(ingredient);
                            paiksRepository.save(paiksCoffee);
                        }
                    }catch (Exception e){
                        System.out.println("크롤링 실패: " + e.getMessage());
                        break;
                    }
                }
            }
        }finally{
            webDriver.quit();
        }
    }

    public void crawlMegaCoffee() {
        WebDriver webDriver = webDriverConfig.create();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(15));
        Map<String, String> categoryMap = Map.of(
                "음료", "https://www.mega-mgccoffee.com/menu/?menu_category1=1&menu_category2=1/",
                "푸드", "https://www.mega-mgccoffee.com/menu/?menu_category1=2&menu_category2=2/"
        );

        try {
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                String category = entry.getKey();
                String url = entry.getValue();

                webDriver.get(url);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul#menu_list > li")));
                int currentPage = 1;

                while (true) {
                    int itemCount = webDriver.findElements(By.cssSelector("ul#menu_list > li")).size();
                    for (int i = 0; i < itemCount; i++) {
                        try {
                            List<WebElement> items = webDriver.findElements(By.cssSelector("ul#menu_list > li"));
                            WebElement item = items.get(i);

                            Actions actions = new Actions(webDriver);
                            actions.moveToElement(item).perform();
                            Thread.sleep(300);

                            WebElement innerDiv = item.findElement(By.cssSelector("div.inner_modal"));
                            JavascriptExecutor js = (JavascriptExecutor) webDriver;
                            js.executeScript("arguments[0].style.display='block';", innerDiv);
                            wait.until(ExpectedConditions.visibilityOf(innerDiv));

                            String name = innerDiv.findElement(By.cssSelector("b")).getText();
                            String engName = innerDiv.findElement(By.cssSelector("div.cont_text_inner.cont_text_info")).getText();
                            String note = innerDiv.findElement(By.cssSelector("div.cont_text")).getText();
                            String allergic = innerDiv.findElement(By.cssSelector("div.cont_text.cont_text_info")).getText();
                            String imageUrl = item.findElement(By.cssSelector("div.cont_gallery_list_img img")).getAttribute("src");

                            List<WebElement> infoList = innerDiv.findElements(By.cssSelector("div.cont_text_inner"));
                            double ounce = 0, kcal = 0;
                            for (WebElement info : infoList) {
                                String text = info.getText();
                                if (text.endsWith("ml")) {
                                    String number = text.replaceAll("[^0-9]", "");
                                    if (!number.isEmpty()) {
                                        ounce = Integer.parseInt(number);
                                    }
                                } else if (text.endsWith("oz")) {
                                    String number = text.replaceAll("[^0-9]", "");
                                    if (!number.isEmpty()) {
                                        ounce = Integer.parseInt(number) * 29.5;
                                    }
                                } else if (text.endsWith("kcal")) {
                                    String number = text.replaceAll("1회", "").replaceAll("[^0-9.]", "");
                                    if (!number.isEmpty()) {
                                        kcal = Double.parseDouble(number);
                                    }
                                }
                            }

                            double caffeine = 0, sodium = 0, sugar = 0, saturatedFat = 0, protein = 0;
                            try {
                                WebElement elementList = innerDiv.findElement(By.cssSelector("div.cont_list ul"));
                                List<WebElement> rows = elementList.findElements(By.tagName("li"));
                                for (WebElement row : rows) {
                                    String text = row.getText().trim();
                                    if (text.startsWith("카페인")) caffeine = extractNumber(text);
                                    else if (text.startsWith("나트륨")) sodium = extractNumber(text);
                                    else if (text.startsWith("당류")) sugar = extractNumber(text);
                                    else if (text.startsWith("포화지방")) saturatedFat = extractNumber(text);
                                    else if (text.startsWith("단백질")) protein = extractNumber(text);
                                }
                            } catch (NoSuchElementException e) {
                                System.out.println("영양정보 없음");
                            }

                            Optional<MegaCoffee> optMega = megaRepository.findByName(name);
                            if (optMega.isPresent()) {
                                MegaCoffee megaCoffee = optMega.get();
                                Ingredient ingredient = megaCoffee.getIngredients();

                                boolean isDifferent =
                                        Math.abs(ingredient.getKcal() - kcal) > 0.001 ||
                                                Math.abs(ingredient.getCaffeine() - caffeine) > 0.001 ||
                                                Math.abs(ingredient.getSodium() - sodium) > 0.001 ||
                                                Math.abs(ingredient.getSugar() - sugar) > 0.001 ||
                                                Math.abs(ingredient.getSaturated_fat() - saturatedFat) > 0.001 ||
                                                Math.abs(ingredient.getProtein() - protein) > 0.001 ||
                                                !Objects.equals(ingredient.getAllergic_ingredients(), allergic);

                                if (isDifferent) {
                                    ingredient.setKcal(kcal);
                                    ingredient.setCaffeine(caffeine);
                                    ingredient.setSodium(sodium);
                                    ingredient.setSugar(sugar);
                                    ingredient.setSaturated_fat(saturatedFat);
                                    ingredient.setProtein(protein);
                                    ingredient.setAllergic_ingredients(allergic);
                                    ingredientRepository.save(ingredient);

                                    megaCoffee.setOunce(ounce);
                                    megaCoffee.setNote("[" + category + "] " + note);
                                    megaCoffee.setImageUrl(imageUrl);
                                    megaRepository.save(megaCoffee);
                                }
                            } else {
                                Ingredient ingredient = new Ingredient();
                                ingredient.setKcal(kcal);
                                ingredient.setCaffeine(caffeine);
                                ingredient.setSodium(sodium);
                                ingredient.setSugar(sugar);
                                ingredient.setSaturated_fat(saturatedFat);
                                ingredient.setProtein(protein);
                                ingredient.setAllergic_ingredients(allergic);
                                ingredientRepository.save(ingredient);

                                MegaCoffee megaCoffee = new MegaCoffee();
                                megaCoffee.setName(name);
                                megaCoffee.setOunce(ounce);
                                megaCoffee.setEng_name(engName);
                                megaCoffee.setImageUrl(imageUrl);
                                megaCoffee.setNote("[" + category + "] " + note);
                                megaCoffee.setPrice(0);
                                megaCoffee.setIngredients(ingredient);
                                megaRepository.save(megaCoffee);
                            }

                        } catch (StaleElementReferenceException se) {
                            System.out.println("Stale 요소 재시도 실패: " + se.getMessage());
                        } catch (Exception e) {
                            System.out.println("모달 크롤링 실패: " + e.getMessage());
                        }
                    }

                    List<WebElement> pageLinks = webDriver.findElements(By.cssSelector("ul#board_page > li > a.board_page_link"));
                    if (currentPage < pageLinks.size()) {
                        WebElement nextPageLink = pageLinks.get(currentPage); 
                        ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", nextPageLink);
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul#menu_list > li")));
                        currentPage++;
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("크롤링 실패: " + e.getMessage());
        }
    }

    public void crawlStarBucks(){
        WebDriver webDriver = webDriverConfig.create();
        WebDriverWait wait = new WebDriverWait(webDriver , Duration.ofSeconds(15));
        Map<String , String> categoryMap = Map.of(
                "음료" , "https://www.starbucks.co.kr/menu/drink_list.do",
                "푸드" , "https://www.starbucks.co.kr/menu/food_list.do"
        );
        try{
            for(Map.Entry<String , String> entry : categoryMap.entrySet()){
                String category = entry.getKey();
                String url = entry.getValue();
                webDriver.get(url);

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("li.menuDataSet")));
                List<WebElement> menuItems = webDriver.findElements(By.cssSelector("li.menuDataSet"));

                for(WebElement item : menuItems){
                    WebElement link = item.findElement(By.cssSelector("a.goDrinkVew"));
                    String productCd = link.getAttribute("prod");

                    String detailUrl;
                    if(category.equals("음료")){
                        detailUrl = "https://www.starbucks.co.kr/menu/drink_view.do?product_cd=" + productCd;
                    }else if (category.equals("푸드")){
                        detailUrl = "https://www.starbucks.co.kr/menu/food_view.do?product_cd=" + productCd;
                    }else{
                        continue;
                    }
                    System.out.println("상세 페이지 URL: " + detailUrl);
                }
            }
        }catch(Exception e){
            System.out.println("오류: " + e.getMessage());
        }
    }
    public void crawlEdiya() {
        WebDriver webDriver = webDriverConfig.create();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(15));
        Map<String, String> categoryMap = Map.of(
                "음료", "https://ediya.com/contents/drink.html",
                "푸드", "https://ediya.com/contents/bakery.html"
        );
        try {
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                String category = entry.getKey();
                String url = entry.getValue();
                webDriver.get(url);

                while (true) {
                    try {
                        WebElement moreBtn = webDriver.findElement(By.cssSelector("a.line_btn"));
                        if (moreBtn.isDisplayed()) {
                            ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", moreBtn);
                            Thread.sleep(1000);
                        } else {break;}
                    } catch (NoSuchElementException e) {break;}
                }

                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.con_align > ul > li > a")));
                List<WebElement> items = webDriver.findElements(By.cssSelector("div.con_align > ul > li > a"));
                System.out.printf("갯수 : %d\n", items.size());

                for (int i = 0; i < items.size(); i++) {
                    try {
                        items = webDriver.findElements(By.cssSelector("div.con_align > ul > li > a"));
                        WebElement item = items.get(i);
                        String imageUrl = item.findElement(By.tagName("img")).getAttribute("src");

                        WebElement li = (WebElement) ((JavascriptExecutor) webDriver).executeScript("arguments[0].click(); return arguments[0].parentElement;", item);

                        Thread.sleep(300);

                        WebElement h2 = li.findElement(By.cssSelector("div.detail_con > h2"));
                        String engName = "";
                        try {
                            engName = h2.findElement(By.tagName("span")).getText().trim();
                        } catch (NoSuchElementException e) {}
                        String name = h2.getText().replace(engName, "").trim();

                        List<WebElement> pList = li.findElements(By.cssSelector("div.detail_con > div.detail_txt > p"));
                        StringBuilder descBuilder = new StringBuilder();
                        for (WebElement p : pList) {
                            descBuilder.append(p.getText().trim()).append(" ");
                        }
                        String note = descBuilder.toString().trim();

                        Map<String, String> nutrition = new HashMap<>();
                        List<WebElement> dtList = li.findElements(By.cssSelector("div.pro_nutri > dl > dt"));
                        List<WebElement> ddList = li.findElements(By.cssSelector("div.pro_nutri > dl > dd"));
                        for (int j = 0; j < Math.min(dtList.size(), ddList.size()); j++) {
                            String label = dtList.get(j).getText().trim();
                            String value = ddList.get(j).getText().trim();
                            nutrition.put(label, value);
                        }

                        String allergic = "";
                        try {
                            allergic = li.findElement(By.cssSelector("div.pro_allergy")).getText().trim();
                        } catch (NoSuchElementException e) {
                            allergic = "";
                        }

                        String ounce = "";
                        try {
                            String rawOunce = li.findElement(By.cssSelector("div.pro_size")).getText().trim();
                            if (rawOunce.contains(":")) {
                                String part = rawOunce.split(":")[1].trim();
                                ounce = part.replaceAll("[^0-9]", "");
                            } else {
                                ounce = rawOunce.replaceAll("[^0-9]", "");
                            }
                        } catch (NoSuchElementException e) {
                            ounce = "";
                        }
                        double kcal = parseNutritionValue(nutrition.getOrDefault("칼로리", "0"));
                        double caffeine = parseNutritionValue(nutrition.getOrDefault("카페인", "0"));
                        double sodium = parseNutritionValue(nutrition.getOrDefault("나트륨", "0"));
                        double sugar = parseNutritionValue(nutrition.getOrDefault("당류", "0"));
                        double saturatedFat = parseNutritionValue(nutrition.getOrDefault("포화지방", "0"));
                        double protein = parseNutritionValue(nutrition.getOrDefault("단백질", "0"));

                        Optional<EDIYACoffee> optEDIYA = eDIYARepository.findByName(name);
                        if (optEDIYA.isPresent()) {
                            EDIYACoffee ediyaCoffee = optEDIYA.get();
                            Ingredient ingredient = ediyaCoffee.getIngredients();

                            boolean isDifferent =
                                    Math.abs(ingredient.getKcal() - kcal) > 0.001 ||
                                            Math.abs(ingredient.getCaffeine() - caffeine) > 0.001 ||
                                            Math.abs(ingredient.getSodium() - sodium) > 0.001 ||
                                            Math.abs(ingredient.getSugar() - sugar) > 0.001 ||
                                            Math.abs(ingredient.getSaturated_fat() - saturatedFat) > 0.001 ||
                                            Math.abs(ingredient.getProtein() - protein) > 0.001 ||
                                            !Objects.equals(ingredient.getAllergic_ingredients(), allergic);

                            if (isDifferent) {
                                ingredient.setKcal(kcal);
                                ingredient.setCaffeine(caffeine);
                                ingredient.setSodium(sodium);
                                ingredient.setSugar(sugar);
                                ingredient.setSaturated_fat(saturatedFat);
                                ingredient.setProtein(protein);
                                ingredient.setAllergic_ingredients(allergic);
                                ingredientRepository.save(ingredient);

                                ediyaCoffee.setOunce(Integer.parseInt(ounce));
                                ediyaCoffee.setNote("[" + category + "] " + note);
                                ediyaCoffee.setImageUrl(imageUrl);
                                eDIYARepository.save(ediyaCoffee);
                            }
                        } else {
                            Ingredient ingredient = new Ingredient();
                            ingredient.setKcal(kcal);
                            ingredient.setCaffeine(caffeine);
                            ingredient.setSodium(sodium);
                            ingredient.setSugar(sugar);
                            ingredient.setSaturated_fat(saturatedFat);
                            ingredient.setProtein(protein);
                            ingredient.setAllergic_ingredients(allergic);
                            ingredientRepository.save(ingredient);

                            EDIYACoffee ediyaCoffee = new EDIYACoffee();
                            ediyaCoffee.setName(name);
                            ediyaCoffee.setOunce(Integer.parseInt(ounce));
                            ediyaCoffee.setEng_name(engName);
                            ediyaCoffee.setImageUrl(imageUrl);
                            ediyaCoffee.setNote("[" + category + "] " + note);
                            ediyaCoffee.setPrice(0);
                            ediyaCoffee.setIngredients(ingredient);
                            eDIYARepository.save(ediyaCoffee);
                        }
                    } catch (Exception e) {
                        System.out.println("메뉴 항목 처리 중 오류: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        } finally {
            webDriver.quit();
        }
    }

    public void crawlCompose() {
        WebDriver webDriver = webDriverConfig.create();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(15));
        Map<String, String> categoryMap = Map.of(
                "음료", "https://ediya.com/contents/drink.html",
                "푸드", "https://ediya.com/contents/bakery.html"
        );
        try {
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                String category = entry.getKey();
                String url = entry.getValue();
                webDriver.get(url);
            }
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        } finally {
            webDriver.quit();
        }
    }

    private double extractNumber(String text){
        String numStr = text.replaceAll("[^0-9.]", "");
        if(!numStr.isEmpty()){
            return Double.parseDouble(numStr);
        }
        return 0.0;
    }

    private double parseNutritionValue(String str) {
        if (str == null || str.isEmpty()) return 0;
        String digits = str.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
