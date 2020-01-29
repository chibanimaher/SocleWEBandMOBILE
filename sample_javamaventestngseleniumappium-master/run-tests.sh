#!/bin/bash

startAppium(){
	if [ "$(uname)" == "Darwin" ]; then
		startAppiumOSX
	elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
		startAppiumLinux
	else
		echo "Unknown OS system, exiting..."
		exit 1
	fi
}

startAppiumOSX(){
	sudo chown -R _usbmuxd:_usbmuxd /var/db/lockdown
	sudo chmod -R 777 /var/db/lockdown
	if [ -z ${UDID} ] ; then
		export UDID=${IOS_UDID}
	fi
		echo "UDID is ${UDID}"
	# Create the screenshots directory, if it doesn't exist'
	mkdir -p .screenshots
	echo "Starting Appium on Mac..."
	export AUTOMATION_NAME="XCUITest"
	echo "Port IWDP is ${iwdpPort}"
	appium-1.15 -U ${UDID} -p ${appiumPort} --webdriveragent-port ${webdriveragentPort} --webkit-debug-proxy-port ${iwdpPort} --log-no-colors --log-timestamp --tmp /tmp/${IOS_UDID}/
}

startAppiumLinux(){
	# Create the screenshots directory, if it doesn't exist'
	mkdir -p .screenshots
	echo "Starting Appium on Linux..."
	set AUTOMATION_NAME=UiAutomator2
	appium --log-no-colors --log-timestamp
}

executeTests(){
	echo "Extracting tests.zip..."
	unzip tests.zip
	echo "EXTRA_PARAMETERS_ESCAPED : ${EXTRA_PARAMETERS_ESCAPED}"
	echo "EXTRA_PARAMETERS : ${EXTRA_PARAMETERS}"
	TestSuiteXML="$(echo ${EXTRA_PARAMETERS} | python -c 'import sys,json;data=json.loads(sys.stdin.read()); print data["testSuite"]')"
	echo "TestSuiteXML : ${TestSuiteXML}" 
	if [ "$(uname)" == "Darwin" ]; then
	   	echo "Running iOS Tests..."
		if [ -f "${TestSuiteXML}" ]
		then
			echo "$TestSuiteXML found."
		else
			echo "$TestSuiteXML not found. use default test suite file"
			TestSuiteXML="src/test/resources/testSuite/SuiteTestNgIos.xml"
		fi
		echo "TestSuiteXML : ${TestSuiteXML}" 		
		mvn clean test -DexecutionType=serverside -Denv=SUT_DEFAULT -DsuiteXmlFile=${TestSuiteXML}
	elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
	    echo "Running Android Tests..."
		if [ -f "${TestSuiteXML}" ]
		then
			echo "$TestSuiteXML found."
		else
			echo "$TestSuiteXML not found. use default test suite file"
			TestSuiteXML="src/test/resources/testSuite/SuiteTestNgAndroidAppiumSelenium.xml"
		fi
		echo "TestSuiteXML : ${TestSuiteXML}" 				
		mvn clean test -DexecutionType=serverside -Denv=SUT_DEFAULT -DsuiteXmlFile=${TestSuiteXML}
	fi
	echo "Finished Running Tests!"
	#cp target/surefire-reports/junitreports/TEST-*.xml TEST-all.xml
	cp target/surefire-reports/TEST-TestSuite.xml TEST-all.xml
	cp target/ExtentReports/ExtentReportResults.html ExtentReportResults.html
	cp target/screenshots/*.png screenshots/
	echo "Finished copy result!"
	
}

echo "###################################################################"
echo "# START RUN-TEST.SH"
echo "###################################################################"

startAppium
executeTests

echo "###################################################################"
echo "# END RUN-TEST.SH"
echo "###################################################################"
