package base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.relevantcodes.extentreports.ExtentReports;

//OB: ExtentReports extent instance created here. That instance can be reachable by getReporter() method.

public class ExtentManager {

	protected static Logger LOGGER = LoggerFactory.getLogger(ExtentManager.class);
    private static ExtentReports extent;

    public synchronized static ExtentReports getReporter(){
        if(extent == null){
            //Set HTML reporting file location
            String workingDir = System.getProperty("user.dir");
            LOGGER.info("workingDir="+workingDir);
            String extentReportResultsFile = workingDir+"/target/ExtentReports/ExtentReportResults.html";
            LOGGER.info("reportDir="+extentReportResultsFile);
            extent = new ExtentReports(extentReportResultsFile, true);
        }
        return extent;
    }
}
