package tests;


import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;

import base.BasePageSelenium;
import base.BaseTest;
import utils.Common;
import base.ExtentTestManager;

public class TestWebGoogle extends BaseTest {
	
	@Test(groups = { "SELENIUM_LOCAL_FIREFOX" })
	public void TestWEB01() throws Exception {

		
        ExtentTestManager.getTest().setDescription("Test Case Google Description");
        
        /*
        ExtentTestManager.getTest().log(LogStatus.SKIP, "stepName", "details");
        ExtentTestManager.getTest().log(LogStatus.INFO, "stepName", "details");
        ExtentTestManager.getTest().log(LogStatus.WARNING, "stepName", "details");
        ExtentTestManager.getTest().log(LogStatus.ERROR, "stepName", "details");
        ExtentTestManager.getTest().log(LogStatus.FAIL, "stepName", "details");
        ExtentTestManager.getTest().log(LogStatus.FATAL, "stepName", "details");		
		*/
        
		String url = propSUT.getProperty("web.url");
		PageWebGoogle bp = new PageWebGoogle();
		bp.openUrl(url); //webDriver.get(url);
		ExtentTestManager.getTest().log(LogStatus.PASS, "Go To Google", url);
		takeScreenshot("TestWEB01_capture1", LogStatus.PASS);
		bp.search("SELENIUM HQ");		
		ExtentTestManager.getTest().log(LogStatus.PASS, "Search", "SELENIUM HQ");
		takeScreenshot("TestWEB01_capture2", LogStatus.PASS);
		String title = webDriver.getTitle();
		ExtentTestManager.getTest().log(LogStatus.PASS, "Get Title", title);	
		takeScreenshot("TestWEB01_capture3", LogStatus.PASS);
	}
	
	

}
