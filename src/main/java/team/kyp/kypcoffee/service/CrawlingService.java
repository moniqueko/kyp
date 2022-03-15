package team.kyp.kypcoffee.service;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import team.kyp.kypcoffee.domain.admin.ReviewRegi;
import team.kyp.kypcoffee.mapper.admin.ReviewManageMapper;

import java.util.concurrent.TimeUnit;

@Service
public class CrawlingService {

    private ReviewManageMapper mapper;
    private FileUploadService fileUploadService;

    public CrawlingService(ReviewManageMapper mapper, FileUploadService fileUploadService) {
        this.mapper = mapper;
        this.fileUploadService = fileUploadService;
    }

    public static final String WEB_DRIVER_ID = "webdriver.chrome.driver"; // 드라이버 ID
    public static final String WEB_DRIVER_PATH = "C:\\chromedriver\\chromedriver.exe"; // 드라이버 경로
    private WebElement element;
    private ReviewRegi reviewRegi = new ReviewRegi();
    private int cnt = 0;

    public void instaKypCoffee() {
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        // 브라우저 보이지 않기
        // options.addArguments("headless");

        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.instagram.com/explore/tags/%EA%B3%A0%EC%9C%A4%EB%B0%95%EC%BB%A4%ED%94%BC/");

        //대기
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

//        driver.findElement(By.name("username")).sendKeys("enjuk91@gmail.com");
//        driver.findElement(By.name("password")).sendKeys("koyunpark1234");
        //로그인 하기
        driver.findElement(By.name("username")).sendKeys("happyjoe1108");
        driver.findElement(By.name("password")).sendKeys("ghghgh22@");
        driver.findElement(By.name("password")).sendKeys(Keys.ENTER);

        //로그인 후 안내창 닫기
        driver.findElement(By.className("cmbtv")).click();
        //첫번째 게시글 클릭
        driver.findElement(By.className("eLAPa")).click();

        while(true){
            try{
                element = driver.findElement(By.xpath("/html/body/div[6]/div[3]/div/article/div/div[2]/div/div/div[2]/div[1]/ul/div/li/div/div/div[2]/h2/div/span/a"));
                String userName = element.getText();

                element = driver.findElement(By.xpath("/html/body/div[6]/div[3]/div/article/div/div[2]/div/div/div[2]/div[1]/ul/div/li/div/div/div[2]/div[1]/span"));
                String instaContent = element.getText();
                //instaContent = instaContent.split("#")[0];

                element = driver.findElement(By.xpath("/html/body/div[6]/div[3]/div/article/div/div[1]/div/div/div[1]/img"));
                String src = element.getAttribute("src");

                System.out.println("userName = " + userName);
                System.out.println("instaContent = " + instaContent);
                System.out.println("src = " + src);

                //img save
                String fileName = fileUploadService.uploadImgUrl(src, cnt);

                reviewRegi.setUserName(userName);
                reviewRegi.setImgSrc(src);
                reviewRegi.setReviewContent(instaContent);
                reviewRegi.setFileName(fileName);

                mapper.reviewRegi(reviewRegi);

                if(cnt == 0){
                    System.out.println("왼쪽버튼");
                    element = driver.findElement(By.xpath("/html/body/div[6]/div[2]/div/div/button"));
                    element.click();
                }else{
                    System.out.println("오른쪽버튼");
                    element = driver.findElement(By.xpath("/html/body/div[6]/div[2]/div/div[2]/button"));
                    element.click();
                }
                cnt++;
            }
            catch (NoSuchElementException e){
                System.out.println("끝! 더이상버튼없음");
                break;
            }
        }
        System.out.println("while문 탈출 ");
        cnt = 0;

        // 브라우저 닫기
        if (driver != null) {
            driver.close();
            driver.quit();
        }
    }


}
