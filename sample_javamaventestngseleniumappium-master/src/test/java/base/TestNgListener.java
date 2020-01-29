package base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.relevantcodes.extentreports.LogStatus;

import base.ExtentManager;
import base.ExtentTestManager;



public class TestNgListener extends BaseTest implements ITestListener {

    private static String getTestMethodName(ITestResult iTestResult) {
        return iTestResult.getMethod().getConstructorOrMethod().getName();
    }
    
	@Override
	public void onTestStart(ITestResult result) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		// TODO Auto-generated method stub
		String base64Screenshot="";
		if (webDriver != null) {
	        base64Screenshot = "data:image/png;base64,"+((TakesScreenshot)webDriver).
	                getScreenshotAs(OutputType.BASE64);				
		}
		if (appiumDriver != null) {
	        base64Screenshot = "data:image/png;base64,"+((TakesScreenshot)appiumDriver).
	                getScreenshotAs(OutputType.BASE64);					
		}
	
		ExtentTestManager.getTest().log(LogStatus.PASS, "Test passed",
                ExtentTestManager.getTest().addBase64ScreenShot(base64Screenshot));
	}

	@Override
	public void onTestFailure(ITestResult result) {
		// TODO Auto-generated method stub
        //Take base64Screenshot screenshot.
		String base64Screenshot="";
		if (webDriver != null) {
	        base64Screenshot = "data:image/png;base64,"+((TakesScreenshot)webDriver).
	                getScreenshotAs(OutputType.BASE64);				
		}
		if (appiumDriver != null) {
	        base64Screenshot = "data:image/png;base64,"+((TakesScreenshot)appiumDriver).
	                getScreenshotAs(OutputType.BASE64);					
		}				
		ExtentTestManager.getTest().log(LogStatus.FAIL,"Test Failed",
                ExtentTestManager.getTest().addBase64ScreenShot(base64Screenshot));
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart(ITestContext context) {
		// TODO Auto-generated method stub
		ExtentTestManager.startTest(context.getName(),"");
	}

	@Override
	public void onFinish(ITestContext context) {
		// TODO Auto-generated method stub
        ExtentTestManager.endTest();
        ExtentManager.getReporter().flush();		
	}

}
