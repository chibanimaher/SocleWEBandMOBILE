package tests;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;

import base.BaseTest;
import base.ExtentTestManager;
import utils.Common;

public class TestAppSampleBitbar extends BaseTest {

	@Test(groups = { "APPIUM_ANDROID_APPLICATION" })
	public void TestAPP01() throws Exception {
 
		ExtentTestManager.getTest().setDescription("Test Case AppSampleBitbar Description");
		
		takeScreenshot("TestAPP01_testDebut", LogStatus.PASS);
		PageAppSampleBitbar bp = new PageAppSampleBitbar();
		takeScreenshot("TestAPP01_testFin", LogStatus.PASS);
		
		ExtentTestManager.getTest().log(LogStatus.PASS, "search", bp.search());
			
	}	
	
	
}
