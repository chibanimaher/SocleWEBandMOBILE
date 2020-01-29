package base;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Common;



public class BasePageSelenium extends BaseTest {
	
	protected static Logger LOGGER = LoggerFactory.getLogger(BasePageSelenium.class);

	public static String getBrowserName() throws Exception {
	    Capabilities cap = ((RemoteWebDriver) webDriver).getCapabilities();
	    String browserName = cap.getBrowserName().toLowerCase();
	    //String os = cap.getPlatform().toString();
	    //String v = cap.getVersion().toString();
		return browserName;
	}
	
    public static void waitForLoad(WebDriver driver) throws Exception {
    	Common.sleep(500);
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }	
	
	public static String openUrl(String url) throws Exception {
		LOGGER.info("BrowserName="+getBrowserName());
		webDriver.get(url);
		Common.sleep(1000);
		return webDriver.getTitle();
	}
	
	public static void waitAndClick(By by) throws Exception {
		webDriverWait.until(ExpectedConditions.presenceOfElementLocated(by));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
		webDriverWait.until(ExpectedConditions.elementToBeClickable(by));
		webDriver.findElement(by).click();
	}
	
	public static void waitAjaxAndClick(By by) throws Exception {
		webDriverEvent.findElement(by); // Synchro ajax beforeFindBy  SeleniumEventListener.java
		webDriverWait.until(ExpectedConditions.presenceOfElementLocated(by));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
		webDriverWait.until(ExpectedConditions.elementToBeClickable(by));
		webDriver.findElement(by).click();
	}
	
	public static void waitSendText(By by, String text) throws Exception {
		webDriverWait.until(ExpectedConditions.presenceOfElementLocated(by));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
		webDriver.findElement(by).sendKeys(text);
	}		
	
	public static void waitSendTextPressEnter(By by, String text) throws Exception {
		
		if (getCapsPlatForm().contains("ANDROID")) {
			webDriverWait.until(ExpectedConditions.presenceOfElementLocated(by));
			webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by));			
		}
		

		webDriver.findElement(by).sendKeys(text);
		webDriver.findElement(by).sendKeys(Keys.RETURN);		
		if (getBrowserName().equals("firefox")) { 
			waitForLoad(webDriver);
		} 
	}	
	
}
