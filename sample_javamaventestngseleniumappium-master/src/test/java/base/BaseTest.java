package base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import com.google.common.base.Enums;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import base.PlatForm;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import utils.Common;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

@Listeners(base.TestNgListener.class)

public class BaseTest {
	
	protected static Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

	public static Map<String, String> mapLocalSuiteData;	
	public static Map<String, String> mapLocalTestData;
	public static Map<String, String> mapTestNgParamSuite;
	public static Map<String, String> mapTestNgParamTest;
	public static List<String> listTestNgErrors;
	public static AppiumDriver<MobileElement> appiumDriver;
	public static AndroidDriver<MobileElement> androidDriver;
	public static IOSDriver<MobileElement> iosDriver;
	public static WebDriver webDriver;
	public static EventFiringWebDriver webDriverEvent;
	public static WebDriverWait webDriverWait;
	public static RemoteWebDriver driver;
	public static String EnvSUT = "SUT_DEFAULT";
	public static Properties propSUT;
	public static String resourcePath;
	public static String targetPath;
	public static String currentPlatForm;
	public static ExtentReports reports;
	public static ExtentTest test;	
	public static final long waitStartAppium = 10000;
	private static String platform;
	private static String browser;
	private static boolean skipTest;	
	public static String strGetPlatform;
	public static String strGetDeviceName="DESKTOP";
	public static String restartDriverOn;
	public static long implicitWait;	
	private static String testName = null;	
	public static StackTraceElement[] stackTrace;
	
	@BeforeSuite(alwaysRun = true)
	public void beforeSuite(ITestContext context) throws Exception {
		skipTest = false;
		stackTrace = Thread.currentThread().getStackTrace();
		LOGGER.info("BEFORESUITE");
		String suiteName = context.getSuite().getName();
		LOGGER.info("SuiteName=" +suiteName);
		resourcePath = new File("./src/test/resources").getCanonicalPath();
		LOGGER.info("resourcePath=" +resourcePath);
		targetPath = new File("./target").getCanonicalPath();
		LOGGER.info("targetPath=" +targetPath);
		mapLocalSuiteData = new HashMap<String, String>();
		mapTestNgParamSuite = context.getSuite().getXmlSuite().getParameters();
		for (Map.Entry<String, String> entry : mapTestNgParamSuite.entrySet()) {
			LOGGER.info("Suite Param : " + entry.getKey() + "=" + entry.getValue());
		}
		String tmpSUT = context.getSuite().getXmlSuite().getParameter("SUT");

		String fileSUTname = "envSUT/"+tmpSUT+".properties";
		if(tmpSUT != null && !tmpSUT.isEmpty() && getClass().getClassLoader().getResourceAsStream(fileSUTname) != null) {
			EnvSUT = tmpSUT;
		} else {
			LOGGER.warn("Use default SUT because patrameter SUT is wrong");
		}
		propSUT = fetchProperties("envSUT/"+EnvSUT+".properties");
		
		setCurrentPlatform(context);
		
		if (restartDriverOn.equals("SUITE")) {
			setUpDriver(PlatForm.valueOf(currentPlatForm));
		}
	}
	
	@AfterSuite(alwaysRun = true)
	public void afterSuite(ITestContext context) throws Exception {
		LOGGER.info("AFTERSUITE");
		mapLocalSuiteData.clear();
		if (restartDriverOn.equals("SUITE")) {
			tearDownDriver(PlatForm.valueOf(currentPlatForm));				
		}
	}	
	
	@BeforeTest(alwaysRun = true)
	public void beforeTest(ITestContext context) throws Exception {
		testName = context.getCurrentXmlTest().getName();
		LOGGER.info("BEFORETEST");
		LOGGER.info("testName="+testName);
		if (skipTest)
			throw new SkipException("Skipping Test: ");
		listTestNgErrors = new ArrayList<String>();		
		mapLocalTestData = new HashMap<String, String>();
		mapTestNgParamTest = context.getCurrentXmlTest().getAllParameters();
		for (Map.Entry<String, String> entry : mapTestNgParamTest.entrySet()) {
			LOGGER.info("Test Param : " + entry.getKey() + "=" + entry.getValue());
		}
		setCurrentPlatform(context);		
		if (restartDriverOn.equals("TEST")) {
			setUpDriver(PlatForm.valueOf(currentPlatForm));
		}
    }	
		
	@AfterTest(alwaysRun = true)
	public void afterTest(ITestContext context) throws Exception {
		LOGGER.info("AFTERTEST");
		mapLocalTestData.clear();
		if (Enums.getIfPresent(PlatForm.class, currentPlatForm).isPresent()) {
			if (restartDriverOn.equals("TEST")) {
				tearDownDriver(PlatForm.valueOf(currentPlatForm));				
			}
		}		
		if (!listTestNgErrors.isEmpty()) {
			for (String error : listTestNgErrors) {
				LOGGER.info("Erreur : " + error);
			}
			Assert.fail("ERROR VERIF : see LOG");
		}				
    }
	
    @BeforeMethod
    public void beforeMethod(ITestContext context,Method m) throws Exception {
    	LOGGER.info("BEFOREMETHOD");
        String methodName = m.getName();
        LOGGER.info("methodName="+methodName);      
    }	
    
    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestContext context,ITestResult result) throws Exception {
    	LOGGER.info("AFTERMETHOD");
    	testName = context.getCurrentXmlTest().getName();
    	String methodName = result.getMethod().getMethodName();
    	int methodStatus = result.getStatus();
    	String testResult;
    	switch(methodStatus){
    	case 1:
    		testResult = "Passed";
    		break;
    	case 2:
    		testResult = "Failed";
    		skipTest = true;
    		break;
    	case 3:
    		testResult = "Skipped";
    		break;
    	default:
    		testResult = "Success_Percentage_Failure";
    	}    	
    	LOGGER.info("#####RESULT##### "+testName +" "+ methodName +" "+testResult.toUpperCase());
    	takeScreenshot("lastCaptureTest_"+testName+"_"+methodName, LogStatus.PASS);
    }

    private void setCurrentPlatform(ITestContext context) throws Exception {
    	
		if (context.getSuite().getXmlSuite().getParameter("PlatForm") != null) {
			currentPlatForm = context.getSuite().getXmlSuite().getParameter("PlatForm");
			LOGGER.info("currentPlatForm in xml suite="+currentPlatForm);
			restartDriverOn = "SUITE";
		} else {
			restartDriverOn = "TEST";
			if (context.getCurrentXmlTest().getParameter("PlatForm") == null) {
				try {
					currentPlatForm = context.getAllTestMethods()[0].getGroups()[0].toString();
					LOGGER.info("currentPlatForm in test method="+currentPlatForm);
				}
				catch (Exception e) {
					String msgError = "PlatForm is not defined in group annotation for this test "+context.getName();
					LOGGER.error(msgError);
					throw new SkipException(msgError);
				}			
			} else {
				currentPlatForm = context.getCurrentXmlTest().getParameter("PlatForm");
				LOGGER.info("currentPlatForm in xml test="+currentPlatForm);
				if (!Enums.getIfPresent(PlatForm.class, currentPlatForm).isPresent()) {
					String msgError = "PlatForm is not defined in testNgXml file for this test "+context.getName();
					LOGGER.error(msgError);
					throw new SkipException(msgError);
				}			
			}  			
		}
    	
  	
    }

	private void setUpDriver(PlatForm expectedPlatform) throws Exception {
		
		try {
			SeleniumEventListener handler;
			Properties propAppium = fetchProperties("config/appium.properties");
			Properties propSelenium = fetchProperties("config/selenium.properties");
			ChromeOptions optionsCH;
			FirefoxOptions optionsFF;
			DesiredCapabilities capabilities;
			String serverAppiumUrl = "";
			String newPort = "";
			switch (expectedPlatform) {
				case SELENIUM_LOCAL_CHROME:
					LOGGER.info("setUpDriver for "+expectedPlatform);
					optionsCH = new ChromeOptions();
					optionsCH.addArguments("start-maximized");
					webDriver = new ChromeDriver(optionsCH);
					implicitWait = Long.parseLong(propSelenium.getProperty("selenium.chrome.implicitlyWait"));
					webDriverWait = new WebDriverWait(webDriver, Long.parseLong(propSelenium.getProperty("selenium.chrome.wait"))); 
					webDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriver.manage().timeouts().pageLoadTimeout(Long.parseLong(propSelenium.getProperty("selenium.chrome.pageLoadTimeout")), TimeUnit.SECONDS);
					webDriverEvent = new EventFiringWebDriver(webDriver);
					handler = new SeleniumEventListener();
					webDriverEvent.register(handler);
					driver = (RemoteWebDriver) webDriver;
					platform = "SELENIUM";
					browser = "CHROME";				
					break;
				case SELENIUM_REMOTE_CHROME:
					LOGGER.info("setUpDriver for "+expectedPlatform);
					checkServerStatus(propSelenium.getProperty("selenium.server.url")+"/status");
					optionsCH = new ChromeOptions();
					optionsCH.addArguments("start-maximized");
					implicitWait = Long.parseLong(propSelenium.getProperty("selenium.chrome.implicitlyWait"));
					webDriver = new RemoteWebDriver(new URL(propSelenium.getProperty("selenium.server.url")),optionsCH);	
					webDriverWait = new WebDriverWait(webDriver, Long.parseLong(propSelenium.getProperty("selenium.chrome.wait"))); 
					webDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriver.manage().timeouts().pageLoadTimeout(Long.parseLong(propSelenium.getProperty("selenium.chrome.pageLoadTimeout")), TimeUnit.SECONDS);
					webDriverEvent = new EventFiringWebDriver(webDriver);
					handler = new SeleniumEventListener();
					webDriverEvent.register(handler);	
					driver = (RemoteWebDriver) webDriver;
					platform = "SELENIUM";
					browser = "CHROME";				
					break;
				case SELENIUM_LOCAL_FIREFOX:
					LOGGER.info("setUpDriver for "+expectedPlatform);
					optionsFF = new FirefoxOptions();
					implicitWait = Long.parseLong(propSelenium.getProperty("selenium.firefox.implicitlyWait"));
					webDriver = new FirefoxDriver(optionsFF);
					webDriverWait = new WebDriverWait(webDriver, Long.parseLong(propSelenium.getProperty("selenium.firefox.wait"))); 
					webDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriver.manage().timeouts().pageLoadTimeout(Long.parseLong(propSelenium.getProperty("selenium.firefox.pageLoadTimeout")), TimeUnit.SECONDS);
					webDriverEvent = new EventFiringWebDriver(webDriver);
					handler = new SeleniumEventListener();
					webDriverEvent.register(handler);	
					driver = (RemoteWebDriver) webDriver;
					platform = "SELENIUM";
					browser = "FIREFOX";					
					break;
				case SELENIUM_REMOTE_FIREFOX:	
					LOGGER.info("setUpDriver for "+expectedPlatform);
					checkServerStatus(propSelenium.getProperty("selenium.server.url")+"/status");
					optionsFF = new FirefoxOptions();
					implicitWait = Long.parseLong(propSelenium.getProperty("selenium.firefox.implicitlyWait"));
					webDriver = new RemoteWebDriver(new URL(propSelenium.getProperty("selenium.server.url")),optionsFF);
					webDriverWait = new WebDriverWait(webDriver, Long.parseLong(propSelenium.getProperty("selenium.firefox.wait"))); 
					webDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriver.manage().timeouts().pageLoadTimeout(Long.parseLong(propSelenium.getProperty("selenium.firefox.pageLoadTimeout")), TimeUnit.SECONDS);				
					webDriverEvent = new EventFiringWebDriver(webDriver);
					handler = new SeleniumEventListener();
					webDriverEvent.register(handler);	
					driver = (RemoteWebDriver) webDriver;
					platform = "SELENIUM";
					browser = "FIREFOX";				
					break;
				case APPIUM_ANDROID_CHROME:
					Common.sleep(waitStartAppium);
					LOGGER.info("setUpDriver for "+expectedPlatform);
					implicitWait = Long.parseLong(propAppium.getProperty("appium.chrome.implicitlyWait"));
					checkServerStatus(propAppium.getProperty("appium.server.url")+"/status");
					capabilities = getDesiredCapabilitiesFromProperties("config/appium.android.web.desiredCapabilities.properties");
					androidDriver = new AndroidDriver<MobileElement>(new URL(propAppium.getProperty("appium.server.url")), capabilities);
					webDriver = androidDriver;
					webDriverWait = new WebDriverWait(webDriver, Long.parseLong(propAppium.getProperty("appium.chrome.wait"))); 
					webDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriver.manage().timeouts().pageLoadTimeout(Long.parseLong(propAppium.getProperty("appium.chrome.pageLoadTimeout")), TimeUnit.SECONDS);				
					getDeviceBatteryLevel(androidDriver);
					driver = (RemoteWebDriver) webDriver;
					platform = "ANDROID";
					browser = "CHROME";				
					break;
				case APPIUM_ANDROID_APPLICATION:
					Common.sleep(waitStartAppium);
					LOGGER.info("setUpDriver for "+expectedPlatform);
					implicitWait = Long.parseLong(propAppium.getProperty("appium.android.implicitlyWait"));
					checkServerStatus(propAppium.getProperty("appium.server.url")+"/status");
					capabilities = getDesiredCapabilitiesFromProperties("config/appium.android.app.desiredCapabilities.properties");
					if (propSUT.getProperty("android.app") != null) {
						capabilities = setCapabilities(capabilities, "app",resourcePath + "/app/" + propSUT.getProperty("android.app"));
					} else {
						capabilities = setCapabilities(capabilities, "app",null);
					}
					capabilities = setCapabilities(capabilities,"appPackage", propSUT.getProperty("android.appPackage"));
					capabilities = setCapabilities(capabilities,"appActivity", propSUT.getProperty("android.appActivity"));
					capabilities = setCapabilities(capabilities,"appWaitActivity", propSUT.getProperty("android.appWaitActivity"));
					androidDriver = new AndroidDriver<MobileElement>(new URL(propAppium.getProperty("appium.server.url")), capabilities);
					appiumDriver = androidDriver;
					androidDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriverWait = new WebDriverWait(appiumDriver, Long.parseLong(propAppium.getProperty("appium.android.wait")));				
					getDeviceBatteryLevel(androidDriver);
					driver = (RemoteWebDriver) appiumDriver;
					strGetDeviceName = appiumDriver.getCapabilities().getCapability("deviceUDID").toString();
					strGetPlatform = appiumDriver.getCapabilities().getCapability("platform").toString();
					LOGGER.info("Platform Android : " + strGetPlatform + " DeviceUDID Android : " + strGetDeviceName);
					platform = "ANDROID";
					browser = "";
					break;				
				case APPIUM_IOS_SAFARI:
					Common.sleep(waitStartAppium);
					LOGGER.info("setUpDriver for "+expectedPlatform);
					implicitWait = Long.parseLong(propAppium.getProperty("appium.safari.implicitlyWait"));
					serverAppiumUrl = propAppium.getProperty("appium.server.url");
					newPort = System.getenv("appiumPort");
					String UDID = System.getenv("UDID");
					String webdriveragentPort = System.getenv("webdriveragentPort");
					String iwdpPort = System.getenv("iwdpPort");
					String IWDP_PORT = System.getenv("IWDP_PORT");
					LOGGER.info("UDID="+UDID);
					LOGGER.info("webdriveragentPort="+webdriveragentPort);
					LOGGER.info("iwdpPort="+iwdpPort);
					LOGGER.info("IWDP_PORT="+IWDP_PORT);
					
					if (newPort != null) {
						serverAppiumUrl = serverAppiumUrl.replace("4723", newPort);
					}
					checkServerStatus(serverAppiumUrl+"/status");
					LOGGER.info("setUpDriver for "+expectedPlatform);
					capabilities = getDesiredCapabilitiesFromProperties("config/appium.ios.web.desiredCapabilities.properties");
					LOGGER.info("setUpDriver on "+serverAppiumUrl);
					iosDriver = new IOSDriver<MobileElement>(new URL(serverAppiumUrl), capabilities);
					webDriver = iosDriver;
					webDriverWait = new WebDriverWait(webDriver, Long.parseLong(propAppium.getProperty("appium.safari.wait"))); 
					webDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriver.manage().timeouts().pageLoadTimeout(Long.parseLong(propAppium.getProperty("appium.safari.pageLoadTimeout")), TimeUnit.SECONDS);								
					driver = (RemoteWebDriver) webDriver;
					platform = "IOS";
					browser = "SAFARI";				
					break;					
				case APPIUM_IOS_APPLICATION:
					Common.sleep(waitStartAppium);
					LOGGER.info("setUpDriver for "+expectedPlatform);					
					implicitWait = Long.parseLong(propAppium.getProperty("appium.ios.implicitlyWait"));
					serverAppiumUrl = propAppium.getProperty("appium.server.url");
					newPort = System.getenv("appiumPort");
					if (newPort != null) {
						serverAppiumUrl = serverAppiumUrl.replace("4723", newPort);
					}				
					checkServerStatus(serverAppiumUrl+"/status");
					capabilities = getDesiredCapabilitiesFromProperties("config/appium.ios.app.desiredCapabilities.properties");
					capabilities = setCapabilities(capabilities, "bundleId",propSUT.getProperty("ios.bundleId"));
					
					/*
					if (propSUT.getProperty("ios.app") != null) {
						LOGGER.info("app="+resourcePath + "/app/" + propSUT.getProperty("ios.app"));
						File dir = new File(resourcePath + "/app/");
						File[] filesList = dir.listFiles();
						for (File file : filesList) {
						    if (file.isFile()) {
						    	LOGGER.info(file.getName());
						    }
						}					
						capabilities = setCapabilities(capabilities, "app",resourcePath + "/app/" + propSUT.getProperty("ios.app"));
					} else {
						capabilities = setCapabilities(capabilities, "app",null);
					}
					*/
					LOGGER.info("setUpDriver on "+serverAppiumUrl);
					iosDriver = new IOSDriver<MobileElement>(new URL(serverAppiumUrl), capabilities);
					appiumDriver = iosDriver;
					strGetDeviceName = appiumDriver.getCapabilities().getCapability("udid").toString();
					strGetPlatform = appiumDriver.getCapabilities().getCapability("platform").toString();
					appiumDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
					webDriverWait = new WebDriverWait(appiumDriver, Long.parseLong(propAppium.getProperty("appium.ios.wait")));
					driver = (RemoteWebDriver) appiumDriver;
					platform = "IOS";
					browser = "";
					break;							
			}
			Common.sleep(1000);
			LOGGER.info("driver session ID "+driver.getSessionId());
			Map<String, Object> capabilitiesList = driver.getCapabilities().asMap();
			long i = 0;
			for (String key : capabilitiesList.keySet()) {
				LOGGER.info("driver capabilities " + key +"="+ capabilitiesList.get(key));
			}		
			
		} catch(Exception e) {
			skipTest = true;
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
	}

	
	private void tearDownDriver(PlatForm expectedSUT) throws Exception {
		LOGGER.info("closeDriver for "+expectedSUT);
		try {
			if (driver != null) {
				switch (expectedSUT) {
				case SELENIUM_LOCAL_CHROME:
					webDriver.close();
					break;
				case SELENIUM_REMOTE_CHROME:
					webDriver.quit();
					break;
				case SELENIUM_LOCAL_FIREFOX:
					webDriver.close();
					break;
				case SELENIUM_REMOTE_FIREFOX:	
					webDriver.quit();
					break;
				case APPIUM_ANDROID_CHROME:
					androidDriver.quit();
					break;
				case APPIUM_ANDROID_APPLICATION:
					androidDriver.quit();
					break;			
				case APPIUM_IOS_SAFARI:
					iosDriver.close();
					Common.sleep(1000);
					iosDriver.quit();
					break;				
				case APPIUM_IOS_APPLICATION:
					iosDriver.quit();
					break;							
				}			
			}			
		} catch(Exception e) {
			LOGGER.warn(e.getMessage());
		}


		appiumDriver = null;
		androidDriver = null;
		iosDriver = null;
		webDriver = null;
		webDriverEvent = null;
		webDriverWait = null;		
	}
	
	private DesiredCapabilities setCapabilities(DesiredCapabilities caps, String capsKey, String capsValue) throws Exception {	
		if (capsValue != null) {
			caps.setCapability(capsKey, capsValue);
			LOGGER.info("setCapability for "+capsKey+"="+capsValue);
		} else {
			LOGGER.warn("setCapability value is null for key "+capsKey);
		}
		return caps;	
	}
	
    private DesiredCapabilities getDesiredCapabilitiesFromProperties(String getDesiredCapabilitiesPropertiesFileName) throws Exception {
    	LOGGER.info("Setting desiredCapabilities defined in " + getDesiredCapabilitiesPropertiesFileName);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        Properties desiredCapabilitiesProperties = fetchProperties(getDesiredCapabilitiesPropertiesFileName);
        Set<String> keys = desiredCapabilitiesProperties.stringPropertyNames();
        for (String key : keys) {
            String value = desiredCapabilitiesProperties.getProperty(key);
            LOGGER.info("setCapability for "+key+"="+value);
            desiredCapabilities.setCapability(key, value);
        }
        return desiredCapabilities;
    }
    
    private Properties fetchProperties(String filename) throws Exception {
        Properties properties = new Properties();
        InputStream input = null;
        try {
            input = getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null) {
            	String msgError = "Unable to find/open file: " + filename;
            	LOGGER.error(msgError);
                throw new FileNotFoundException(msgError);
            }
            properties.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    public static void logStep(String stepName, LogStatus status, String stepDesc) throws Exception {
    	LOGGER.info("STEP="+stepName + " / " + stepDesc);
    	ExtentTestManager.getTest().log(status, stepName, stepDesc);
    }
    
    private static String convertScreenshotFileToBase64(File screenshot) throws IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(screenshot);
        String encodeString = java.util.Base64.getEncoder().encodeToString(fileContent);
        
        return encodeString;
    }
 
    public static void takeScreenshot(String stepName, LogStatus status) throws IOException {
        String encodeString;
    	String fullFileName = targetPath + "/screenshots/" + 
        		testName.replace(' ', '_') + "_" +
        		new Date().getTime() + ".png";
        //LOGGER.info("Taking screenshot...");
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File screenshot = new File(fullFileName);
        FileUtils.copyFile(scrFile, screenshot);
        LOGGER.info("STEP="+stepName + screenshot.getAbsolutePath());
        
        encodeString = convertScreenshotFileToBase64(screenshot);

        ExtentTestManager.getTest().log(status, stepName,
        		ExtentTestManager.getTest().
        		addBase64ScreenShot("data:image/png;base64," + encodeString));
    }    
    
    
    private void getDeviceBatteryLevel(AndroidDriver driver) throws Exception {
    	/*    	
        List<String> args = Arrays.asList("battery");
        Map<String, Object> cmd = ImmutableMap.of("command", "dumpsys", "args", args);
        Object output = driver.executeScript("mobile:shell", cmd);   
        */
    	/*
    	Object output = driver.executeScript("mobile:batteryInfo");   
    	*/

    	/*
    	Runtime runtime = Runtime.getRuntime();
    	String[] args = { "adb shell", "dumpsys battery | grep level"};    	
    	Process process = runtime.exec(args);
    	process.waitFor();
    	
    	OutputStream rtm = process.getOutputStream();
    	PrintStream prtStrm = new PrintStream(rtm);
        LOGGER.info("get Value of battery level"+prtStrm);
        */  
    	
    	Runtime runtime = Runtime.getRuntime();
    	String[] command = { "adb","shell", "dumpsys", "battery", "| grep level"};    	
    	Process process;
		try {
			process = runtime.exec(command);
	    	process.waitFor();
	    	String output = IOUtils.toString(process.getInputStream(),"UTF-8");
	    	String errorOutput = IOUtils.toString(process.getErrorStream(),"UTF-8");    	
	    	LOGGER.info("get Value of battery level"+output);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    }
    
	public static String getCapsPlatForm() throws Exception {
	    Capabilities cap = driver.getCapabilities();
	    String platformName = cap.getCapability("platformName").toString().toUpperCase();
		return platformName;
	}  
    
    public static String getPlatform() {
    	return platform;
    }
    
    public String getBrower() {
    	return browser;
    }
		
    private void checkServerStatus(String serverUrl) throws Exception {
		HttpResponse<JsonNode> res;
		String gridResponseBody = "";
		String gridStatus = "";
		String gridReady = "";
		String msgError = "";
		int getStatus;
		long connectionTimeout = 1000;
		long socketTimeout = 2000;
		Unirest.setTimeouts(connectionTimeout, socketTimeout);
		boolean activeServer = false;		
		int loopNumber = 1; 		
        while (loopNumber <= 10 && activeServer == false) { 
    		try {
    			res = Unirest.get(serverUrl).asJson();
    			getStatus = res.getStatus();
    			if (getStatus == 200) {
    				gridResponseBody = res.getBody().toString();
    				JSONObject jsonObject = new JSONObject(gridResponseBody);
    				if (jsonObject.has("status")) {
    					gridStatus = jsonObject.get("status").toString();
    					LOGGER.info("Get Server status : " +serverUrl + " " + gridResponseBody);	
    					if (!gridStatus.equals("0")) {
    						msgError = "Server " +serverUrl+ " is down, service status is " + gridStatus;
    						LOGGER.error(msgError);		
    					} else {
    						activeServer = true;
    					}
    				} else {
    					// bug server Appium : the field status disappears after several calls.
    					LOGGER.warn("Server " +serverUrl+ " : the field status is not visible");
    					activeServer = true;
    				}    			
    			} else {
    				msgError = "Server " +serverUrl+ " is down, server status is " + getStatus;
    				LOGGER.error(msgError);					
    			}
    		} catch (Exception e) {
    			msgError = "Server " +serverUrl+ " is down " + e.getMessage();
    			LOGGER.error(msgError);
    		}       	
        }		
        if (activeServer == false) {
        	
        }
    }
}
