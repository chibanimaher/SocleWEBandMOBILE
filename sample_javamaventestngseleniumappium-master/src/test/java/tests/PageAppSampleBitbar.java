package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import base.BasePageAppium;
import io.appium.java_client.remote.HideKeyboardStrategy;
import utils.Common;

public class PageAppSampleBitbar extends BasePageAppium{
	
	private static By radioButton;
	private static By editText;
	private static By button;
	private static By textView;
	

	public static String search() throws Exception {
		if (getCapsPlatForm().contains("ANDROID")) {
			radioButton = By.xpath("//android.widget.RadioButton[@text='Use Testdroid Cloud']");
			editText = By.xpath("//android.widget.EditText[@resource-id='com.bitbar.testdroid:id/editText1']");
			button = By.xpath("//android.widget.Button[@resource-id='com.bitbar.testdroid:id/button1']");
			textView = By.xpath("//android.widget.TextView[@resource-id='com.bitbar.testdroid:id/textView1']");
			
			androidDriver.findElement(radioButton).click();
			androidDriver.findElement(editText).sendKeys("John Doe");
			androidDriver.findElement(button).click();
			Common.sleep(1000);
			return androidDriver.findElement(textView).getText();
			
		} else {
			radioButton = By.xpath("//*[contains(@name, 'answer1')]");
			editText = By.xpath("//*[contains(@name, 'userName')]");
			button = By.xpath("//*[contains(@name, 'sendAnswer')]");
			
			iosDriver.findElement(radioButton).click();
	        WebElement element = appiumDriver.findElement(editText);
	        element.sendKeys("Testdroid");
	        iosDriver.findElement(button).click();	
	        return "";
		}	
	}

	
	
}
