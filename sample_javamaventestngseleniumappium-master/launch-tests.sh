#!/bin/bash

#
# Script for uploading the test project to Bitbar cloud as a Server side Appium test
#
# Requires the user's API key, device group name and test type (android or ios).
# Creates the test zip, uploads it to the set project and launches the test.
#
# @author lasse.hall@bitbar.com
#

set -x

echo "###################################################################"
echo "# START LAUNCH-TEST.SH"
echo "###################################################################"

function help() {
	echo
	echo "$0 - create and upload test project to Bitbar cloud and run it"
	echo 
	echo "Mandatory -k <API_KEY>"
	echo "Mandatory -e <API_ENDPOINT>"
	echo "Mandatory -o <OS_TYPE>"
	echo "Mandatory -f <FRAMEWORK_NAME>"
	echo "Mandatory -g <DEVICE_GROUP_ID>"
	echo "Mandatory -p <PROJECT_NAME>"
	echo "Optional -a <APP_FILE>"
	echo "Optional -s <TESTNG_SUITE>"
	echo "Optional -t <TIMER_ABORT>"
	exit
}

while getopts "k:e:o:f:g:p:a:s:t:h" opt; do
	case $opt in
		k) API_KEY=${OPTARG} ;;
		e) API_ENDPOINT=${OPTARG} ;;		  
		o) OS_TYPE=${OPTARG} ;;
		f) FRAMEWORK_NAME=${OPTARG} ;;
		g) DEVICE_GROUP_ID=${OPTARG} ;;
		p) PROJECT_NAME=${OPTARG} ;;
		a) APP_FILE=${OPTARG} ;;
		s) TESTNG_SUITE=${OPTARG} ;;
		t) TIMER_ABORT=${OPTARG} ;;
		h) help ;;
		\?) echo "Invalid option: -${OPTARG}" >&2
		  ;;
		:) echo "Option -${OPTARG} requires an argument." >&2
		  exit 1
		  ;;
	esac
done


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP01 : Check that mandatory parameters were given"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

if [ -z ${API_KEY} ] ; then
	echo "API_KEY is null" >&2
	help
fi
if [ -z ${API_ENDPOINT} ] ; then
	echo "API_ENDPOINT is null"
	help
fi
if [ -z ${OS_TYPE} ] ; then
	echo "OS_TYPE is null"
	help
fi
if [ -z ${FRAMEWORK_NAME} ] ; then
	echo "FRAMEWORK_NAME is null"
	help
fi
if [ -z ${DEVICE_GROUP_ID} ] ; then
	echo "DEVICE_GROUP_ID is null"
	help
fi

if [ -z ${PROJECT_NAME} ] ; then
	echo "PROJECT_NAME is null"
	help
fi



echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP02 : Check that OS_TYPE given is valid"
echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "${OS_TYPE}"
if ! [[ "${OS_TYPE}" == "ANDROID" || "${OS_TYPE}" == "IOS" ]] ; then
	echo "OS_TYPE is invalid (ANDROID or IOS is expected)" >&2
	help
fi


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP03 : Check framework existence and get FRAMEWORK_ID"
echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo ${FRAMEWORK_NAME}
curl -G -s -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/admin/frameworks" --data-urlencode "filter=s_name_eq_${FRAMEWORK_NAME}" | python -m json.tool > jsonFramework.json
FRAMEWORK_ID="$(cat jsonFramework.json | sed -n -e '/"id": / s/^.*"id": \(.*\)"*,/\1/p')"
echo "FRAMEWORK_ID: ${FRAMEWORK_ID}"
if [ -z ${FRAMEWORK_ID} ] ; then
	echo "Framework \"${FRAMEWORK_NAME}\" not found"
	help
fi


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP04 : Check user ID"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

curl -s -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/me" | python -m json.tool > jsonUser.json
MAIN_USER_ID="$(cat jsonUser.json | sed -n -e '/"mainUserId": / s/^.*"mainUserId": \(.*\)"*,/\1/p')"
echo "MAIN_USER_ID: ${MAIN_USER_ID}"
if [ -z ${MAIN_USER_ID} ] ; then
	echo "Authentication failed, check apikey given in -k: "${API_KEY}""
	help
else
	echo "Authentication succeeded"
fi


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP05 : Check if project exist, if not create project"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

echo "Checking if project with name ${PROJECT_NAME} exists"
curl -G -s -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/me/projects" --data-urlencode "filter=s_name_eq_${PROJECT_NAME}" | python -m json.tool > jsonProjectExist.json
PROJECT_ID="$(cat jsonProjectExist.json | sed -n -e '/"id":/ s/^.* \(.*\),.*/\1/p')"
echo "Project ID is: ${PROJECT_ID}"

if [ -z ${PROJECT_ID} ] ; then
	echo "Project not found, creating new project with name ${PROJECT_NAME}"
	curl -H "Accept: application/json" -u ${API_KEY}: -X POST -F "name=${PROJECT_NAME}" "${API_ENDPOINT}/api/v2/me/projects" | python -m json.tool > jsonProjectCreate.json
	PROJECT_ID="$(cat jsonProjectCreate.json | sed -n -e '/"id":/ s/^.* \(.*\),.*/\1/p')"
	echo "Project created with ID: ${PROJECT_ID} and name: ${PROJECT_NAME}"	
else
	PROJECT_OS_TYPE="$(cat jsonProjectExist.json | sed -n -e '/"osType":/ s/^.* \(.*\),.*/\1/p')"
	echo ${PROJECT_OS_TYPE}
	if [[ ( "\"${OS_TYPE}\"" != "${PROJECT_OS_TYPE}" ) ]] ; then
		echo "Mismatch: OS_TYPE: ${OS_TYPE} but PROJECT_TYPE: ${PROJECT_OS_TYPE}"
		help
	fi
fi


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP06 : Check Device Group Exists"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

echo "DEVICE_GROUP_ID: ${DEVICE_GROUP_ID}"
curl -G -s -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/me/device-groups?withPublic=true" --data-urlencode "filter=n_id_eq_${DEVICE_GROUP_ID}" | python -m json.tool > jsonDeviceGroupExist.json
DEVICE_GROUP_ID_EXISTS="$(cat jsonDeviceGroupExist.json | sed -n -e '/"id":/ s/^.* \(.*\),.*/\1/p')"
if [ -z ${DEVICE_GROUP_ID_EXISTS} ] ; then
	echo "No DEVICE_GROUP_ID with value \"${DEVICE_GROUP_ID}\" found"
	help
fi
echo "DEVICE_GROUP_ID: ${DEVICE_GROUP_ID}"


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP07 : Check that Device Group is of correct type"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

curl -s -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/me/device-groups/${DEVICE_GROUP_ID}" | python -m json.tool > jsonDeviceGroupType.json
DEVICE_GROUP_TYPE="$(cat jsonDeviceGroupType.json | sed -n -e '/"osType":/ s/^.*"osType": "\(.*\)".*/\1/p')"
echo "DEVICE_GROUP_TYPE: ${DEVICE_GROUP_TYPE}"
if [[ ( "${OS_TYPE}" != "${DEVICE_GROUP_TYPE}" ) ]] ; then
	echo "Mismatch: OS_TYPE:${OS_TYPE} DEVICE_GROUP_TYPE:${DEVICE_GROUP_TYPE}"
	help
fi


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP08 : Delete old zip file / Create new zip file / Upload new zip file"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

curl -G -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/users/${MAIN_USER_ID}/files?limit=1" --data-urlencode "filter=s_name_eq_${PROJECT_NAME}.zip" | python -m json.tool > jsonFilesZipExist.json
FILES_ID="$(cat jsonFilesZipExist.json | sed -n -e '/"directUrl":/ s/^.* "\(.*\)\/file",.*/\1/p')"
for FILE_ID in $FILES_ID; do
	find="/files"
	replace="/users/${MAIN_USER_ID}/files"	
	URL_DELETE=${FILE_ID//$find/$replace}
	URL_DELETE=${URL_DELETE//"https"/"http"}
    echo "DELETE URL_DELETE: ${URL_DELETE}"
	curl -H "Accept: application/json" -u ${API_KEY}: -X DELETE "${URL_DELETE}"
done

if [ -f "${PROJECT_NAME}.zip" ]; then
	rm "${PROJECT_NAME}.zip"
fi
zip -rq "${PROJECT_NAME}.zip" pom.xml run-tests.sh src

curl -H "Accept: application/json" -u ${API_KEY}: -X POST -F "file=@${PROJECT_NAME}.zip" "${API_ENDPOINT}/api/v2/users/${MAIN_USER_ID}/files" | python -m json.tool > jsonFileZipUpload.json
ZIP_FILES_ID="$(cat jsonFileZipUpload.json | sed -n -e '/"id":/ s/^.* \(.*\),.*/\1/p')"
echo "POST ZIP_FILES_ID: ${ZIP_FILES_ID}"


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP09 : Delete old app file / Upload new app file"
echo "# ----------------------------------------------------------------------------------------------------------------------------"
if [ -e ${APP_FILE} ] ; then
	if [[ -z ${APP_FILE} ]]; then
		echo "No app file given in -a "
		APP_FILES_ID=null
	else
		echo "-a was given, uploading the file given: ${APP_FILE}"
		APP_NAME=`basename ${APP_FILE}`
		echo "basename:" ${APP_NAME}
		curl -G -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/users/${MAIN_USER_ID}/files?limit=1" --data-urlencode "filter=s_name_eq_${APP_NAME}" | python -m json.tool > jsonFilesAppExist.json
		FILES_ID="$(cat jsonFilesAppExist.json | sed -n -e '/"directUrl":/ s/^.* "\(.*\)\/file",.*/\1/p')"
		for FILE_ID in $FILES_ID; do
			find="/files"
			replace="/users/${MAIN_USER_ID}/files"	
			URL_DELETE=${FILE_ID//$find/$replace}
			URL_DELETE=${URL_DELETE//"https"/"http"}
			echo "DELETE URL_DELETE: ${URL_DELETE}"
			curl -H "Accept: application/json" -u ${API_KEY}: -X DELETE "${URL_DELETE}"
		done
		
		curl -H "Accept: application/json" -u ${API_KEY}: -X POST -F "file=@${APP_FILE}" "${API_ENDPOINT}/api/v2/users/${MAIN_USER_ID}/files" | python -m json.tool > jsonFileAppUpload.json
		APP_FILES_ID="$(cat jsonFileAppUpload.json | python -c 'import sys,json;data=json.loads(sys.stdin.read()); print data["id"]')"
		echo "POST APP_FILES_ID: ${APP_FILES_ID}"	
	fi
fi

echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "# STEP10 : Create run"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

deviceLanguageCode=fr_FR
timeout=1200
echo "deviceLanguageCode: ${deviceLanguageCode}"
echo "timeout: ${timeout}"

RUN_DATA='{"osType":"'${OS_TYPE}'","projectId":'${PROJECT_ID}',"loadedPrevious":true,"files":[{"id":'${ZIP_FILES_ID}',"action":"RUN_TEST"},{"id":'${APP_FILES_ID}',"action":"INSTALL"}],"frameworkId":'${FRAMEWORK_ID}',"deviceGroupId":'${DEVICE_GROUP_ID}',"applicationUsername":null,"applicationPassword":null,"deviceLanguageCode":"'${deviceLanguageCode}'","hookURL":null,"instrumentationRunner":null,"limitationType":null,"limitationValue":null,"screenshotDir":null,"withAnnotation":null,"withoutAnnotation":null,"timeout":'${timeout}',"projectName":null,"testRunName":null,"testRunParameters":[{"key": "testSuite","value": "'${TESTNG_SUITE}'"}]}'

curl -H 'Content-Type: application/json' -u ${API_KEY}: -d "${RUN_DATA}" "${API_ENDPOINT}/api/v2/runs" | python -m json.tool > jsonRun.json
TESTRUN_ID="$(cat jsonRun.json | python -c 'import sys,json;data=json.loads(sys.stdin.read()); print data["id"]')"
echo "TESTRUN_ID: ${TESTRUN_ID}"


echo "# ----------------------------------------------------------------------------------------------------------------------------"
echo "#"
echo "# STEP11 : Get run status / Abort if TIMER_ABORT given"
echo "#"
echo "# ----------------------------------------------------------------------------------------------------------------------------"

if [ -z ${TESTRUN_ID} ] ; then
	echo "TESTRUN_ID not gotten, the test probably wasn't launched properly.. exiting."
	exit
else
	echo "Testrun ID: ${TESTRUN_ID}"
	TEST_STATE="WAITING"
	TIMER=0
	while [ ${TEST_STATE} != "FINISHED" ] ; do
		sleep 10
		TIMER=$(( TIMER + 10 ))
		TEST_STATE="$(curl -s -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/me/projects/${PROJECT_ID}/runs/${TESTRUN_ID}" | python -c 'import sys,json;data=json.loads(sys.stdin.read()); print data["state"]')"
		echo "TEST_STATE = ${TEST_STATE}"
		
		if ! [[ -z ${TIMER_ABORT} ]] ; then
			if [ "$TIMER" -ge "$TIMER_ABORT" ] ; then
				curl -s -H "Accept: application/json" -u ${API_KEY}: -X POST "${API_ENDPOINT}/api/v2/me/projects/${PROJECT_ID}/runs/${TESTRUN_ID}/abort"
				TEST_STATE="$(curl -s -H "Accept: application/json" -u ${API_KEY}: "${API_ENDPOINT}/api/v2/me/projects/${PROJECT_ID}/runs/${TESTRUN_ID}" | python -c 'import sys,json;data=json.loads(sys.stdin.read()); print data["state"]')"
				echo "TEST_STATE = ${TEST_STATE}"
				if [ ${TEST_STATE} == "FINISHED" ] ; then
					echo "Abort after ${TIMER_ABORT}s is successful"
				else
					echo "Abort failed"
				fi
			fi
		fi
	done
fi

echo "###################################################################"
echo "# END LAUNCH-TEST.SH"
echo "###################################################################"