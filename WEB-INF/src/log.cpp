#include <windows.h>
#include <stdio.h>
#include <iostream>
#define __STDC_WANT_LIB_EXT1__ 1
#include <stdlib.h>    
#include <cstring>  
#include <string>
#include <wchar.h>
#include <locale.h>
#include <fstream>
#include <jni.h>
#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/reader.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/allocators.h"
#include "rapidjson/pointer.h"
#include "rapidjson/schema.h"
#include "Database.h"
#include <vector>
#include <clocale>
#include <locale>
#include <cwchar>
#include <regex>
void print_as_wide(const char* mbstr)
{
    std::mbstate_t state = std::mbstate_t();
    std::size_t len = 1 + std::mbsrtowcs(NULL, &mbstr, 0, &state);
    std::vector<wchar_t> wstr(len);
    std::mbsrtowcs(&wstr[0], &mbstr, wstr.size(), &state);
    std::wcout << "Wide string: " << &wstr[0] << '\n'
               << "The length, including '\\0': " << wstr.size() << '\n';
}

#define PROVIDER_NAME L"Security"
//#define RESOURCE_DLL "C:\\Windows\\System32\\evr.dll"
#define RESOURCE_DLL "C:\\Windows\\System32\\adtschema.dll"
#define MAX_TIMESTAMP_LEN 23 + 1   
#define MAX_RECORD_BUFFER_SIZE  0x10000  // 64K
LPCSTR RESOURCE_DLL_LIST[] = {"C:\\Windows\\System32\\adtschema.dll","C:\\Windows\\System32\\wevtsvc.dll",
	"C:\\Windows\\System32\\MsAuditE.dll","C:\\Windows\\System32\\MsObjs.dll"};
int current_dll_position = 0;

using namespace std;


HANDLE GetMessageResources();
LPSTR GetMessageString(DWORD Id, DWORD argc, LPWSTR args);
DWORD DumpRecordsInBuffer(PBYTE pBuffer, DWORD dwBytesRead,jint *pid);
DWORD ApplyParameterStringsToMessage(CONST LPSTR pMessage, LPSTR & pFinalMessage);
DWORD GetEventTypeName(DWORD EventType);
void GetTimestamp(const DWORD Time, char* DisplayString);
LPSTR* GetParameters(CONST LPSTR pMessage);
int GetParameterCount(CONST LPSTR pMessage);
std::string newTimeStamp = std::string("");
std::string extension = std::string(".dat");

const char* pEventTypeNames[] = {"Error", "Warning", "Informational", "Audit Success", "Audit Failure"};
	
HANDLE g_hResources = NULL;
using namespace rapidjson;

int count = 0;
jsize len = 0;



JNIEXPORT jstring JNICALL Java_Database_getTableAsJson(JNIEnv *env, jobject jobj, jint lastInsertedRecordID)
{
	cout<<"enterd";
	std::setlocale(LC_ALL, "en_US.utf8");
	//len = env->GetArrayLength(idList);
	//jint *pId = env->GetIntArrayElements(idList, 0);
	const char* json = "{}";
	Document obj;
	obj.Parse(json);
	obj.SetObject();
	Document::AllocatorType& allocator = obj.GetAllocator();
	Value col(kArrayType);
	col.PushBack("eventID",allocator);
	col.PushBack("eventProvider",allocator);
	col.PushBack("eventType", allocator);
	col.PushBack("timestamp",allocator);
	col.PushBack("recordID",allocator);
	col.PushBack("SecurityID",allocator);
	col.PushBack("accountName",allocator);
	col.PushBack("accountDomain",allocator);
	col.PushBack("logonID",allocator);
	col.PushBack("message",allocator);
	//col.PushBack("Session ID",allocator);
	//obj.AddMember("col",col,allocator);
    HANDLE hEventLog = NULL;
    DWORD status = ERROR_SUCCESS;
    DWORD dwBytesToRead = 0;
    DWORD dwBytesRead = 0;
    DWORD dwMinimumBytesToRead = 0;
    PBYTE pBuffer = NULL;
    PBYTE pTemp = NULL;
	Value rows(kArrayType);
	Document rowObject;
	//string providers[len] = {};
	vector <string> providers;
	vector <string> eventType;
	vector <string> timestamps;
	vector <int> recordID;
	vector <string> messages;
	vector <string> securityIDs;
	vector <string> accountNames;
	vector <string> accountDomains;
	vector <string> logonIDs;
	vector <string> sessionIDs;
	char* TimeStamp[MAX_TIMESTAMP_LEN];
	int timestamp_index = 0;
	//wstring;
    // The source name (provider) must exist as a subkey of Application.
	if(NULL == hEventLog) {
		//hEventLog = OpenEventLog(NULL, (LPCSTR) PROVIDER_NAME);
		hEventLog = OpenEventLog(NULL, "Security");
		if (NULL == hEventLog)
		{
			wprintf(L"OpenEventLog failed with 0x%x.\n", GetLastError());
			return NULL;
		}
		else {
			cout<<"opened log\n";
		}
		// Get the DLL that contains the string resources for the provider.
		if (NULL == g_hResources)
			g_hResources = GetMessageResources();
		if (NULL == g_hResources)
		{
			wprintf(L"GetMessageResources failed.\n");
			return NULL;
		}
	}
	// Allocate an initial block of memory used to read event records. 
	dwBytesToRead = MAX_RECORD_BUFFER_SIZE;
	pBuffer = (PBYTE)malloc(dwBytesToRead);
	if (NULL == pBuffer)
	{
		wprintf(L"Failed to allocate the initial memory for the record buffer.\n");
		return NULL;
	}
	bool lastRecordReached = false;
	// Read blocks of records until you reach the end of the log or an error occurs.
	while (ERROR_SUCCESS == status && !lastRecordReached)
	{
		if (!ReadEventLog(hEventLog, 
			EVENTLOG_SEQUENTIAL_READ | EVENTLOG_BACKWARDS_READ,
			0, 
			pBuffer,
			dwBytesToRead,
			&dwBytesRead,
			&dwMinimumBytesToRead))
		{
			status = GetLastError();
			if (ERROR_INSUFFICIENT_BUFFER == status)
			{
				status = ERROR_SUCCESS;

				pTemp = (PBYTE)realloc(pBuffer, MAX_RECORD_BUFFER_SIZE);
				if (NULL == pTemp)
				{
					wprintf(L"Failed to reallocate the memory for the record buffer (%d bytes).\n", dwMinimumBytesToRead);
					return NULL;
				}

				pBuffer = pTemp;
				dwBytesToRead = MAX_RECORD_BUFFER_SIZE;
			}
			else 
			{
				if (ERROR_HANDLE_EOF != status)
				{
					wprintf(L"ReadEventLog failed with %lu.\n", status);
					return NULL;
				}
			}
		}
		else
		{
			// Print the contents of each record in the buffer.
			//count++;
			DWORD status = ERROR_SUCCESS;
			unsigned char* pRecord = pBuffer;
			unsigned char* pEndOfRecords = pBuffer + dwBytesRead;
			char TimeStamp[MAX_TIMESTAMP_LEN];
			LPSTR pMess = NULL;
			char* pMessage = NULL;
			LPSTR pFinalMessage = NULL;
			bool flag = false;
			string message;
			regex securityIDRegex("Security ID:\\s*[-a-zA-z0-9]+");
			smatch securityIDMatch;
			string securityID;
			string trimmedSecurityID;
			regex accountNameRegex("Account Name:\\s*[-a-zA-z0-9]+");
			smatch accountNameMatch;
			string accountName;
			string trimmedAccountName;
			regex accountDomainRegex("Account Domain:\\s*[-a-zA-z0-9]+");
			smatch accountDomainMatch;
			string accountDomain;
			string trimmedAccountDomain;
			regex logonIDRegex("Logon ID:\\s*[-a-zA-z0-9]+");
			smatch logonIDMatch;
			string logonID;
			string trimmedLogonID;
			regex colon_whitespace(":\\s+");
			LPSTR* pParameters;
			int parameterCount;
			//cout<<"Record id is "<<((PEVENTLOGRECORD)pRecord)->RecordNumber <<" and lastInsertedRecordID is "<<lastInsertedRecordID<<"\n";
			/*if(((PEVENTLOGRECORD)pRecord)->RecordNumber <= lastInsertedRecordID) {
				lastRecordReached = true;
				break;
			}*/
			while (pRecord < pEndOfRecords) {
				LPSTR pMess = NULL;
				char* pMessage = NULL;
				char* finalMessage = NULL;
				LPSTR pFinalMessage = NULL;
				pParameters = NULL;
				parameterCount = 0;
				if(((PEVENTLOGRECORD)pRecord)->RecordNumber <= lastInsertedRecordID) {
					cout<<"true\n";
					lastRecordReached = true;
					break;
				}
				int eventID = (((PEVENTLOGRECORD)pRecord)->EventID & 0xFFFF);
				if(eventID == 4800 || eventID == 4801 || eventID == 4624 || eventID == 4634 || eventID == 4647) 
				{
					pMessage = (char*)GetMessageString(((PEVENTLOGRECORD)pRecord)->EventID, 
						((PEVENTLOGRECORD)pRecord)->NumStrings, (LPWSTR)(pRecord + ((PEVENTLOGRECORD)pRecord)->StringOffset));
					pMess = GetMessageString(((PEVENTLOGRECORD)pRecord)->EventID, 
						((PEVENTLOGRECORD)pRecord)->NumStrings, (LPWSTR)(pRecord + ((PEVENTLOGRECORD)pRecord)->StringOffset));
					//cout<<"pmess is "<<pMess<<endl;
					if (pMess)
					{
						//parameterCount = GetParameterCount(pMess);
						//pParameters = GetParameters(pMess);
						status = ApplyParameterStringsToMessage(pMess, pFinalMessage);
						cout<<"event message is "<<((pFinalMessage) ? pFinalMessage : pMess)<<endl;
						//pMessage = (char*)pFinalMessage;
						//finalMessage = (char*)pFinalMessage;
						//message = string(pFinalMessage);
						pMess = NULL;

						if (pFinalMessage)
						{
							pFinalMessage = NULL;
						}
					}
					//cout<<"Total parameters is "<<parameterCount<<endl<<"And they are "<<endl;
					/*for(int i = 0;i < parameterCount;i++) {
						cout<<pParameters[i]<<endl;
					}*/
					message = string(pMessage);
					messages.push_back(message);
					//Parse the security ID and store it in vector
					regex_search(message, securityIDMatch, securityIDRegex);
					for (auto x : securityIDMatch) {
						securityID = x;
					}
					trimmedSecurityID = std::regex_replace(securityID, colon_whitespace, ":");
					securityID = trimmedSecurityID.substr(trimmedSecurityID.find(":")+1);
					//securityIDs.push_back(securityID);
					//cout<<securityID<<endl;
					//Parse the Account Name and store it in vector
					regex_search(message, accountNameMatch, accountNameRegex);
					for (auto x : accountNameMatch) {
						accountName = x;
					}
					trimmedAccountName = std::regex_replace(accountName, colon_whitespace, ":");
					accountName = trimmedAccountName.substr(trimmedAccountName.find(":")+1);
					//accountNames.push_back(accountName);
					//Parse the Account Domain and store it in vector
					regex_search(message, accountDomainMatch, accountDomainRegex);
					for (auto x : accountDomainMatch) {
						accountDomain = x;
					}
					trimmedAccountDomain = std::regex_replace(accountDomain, colon_whitespace, ":");
					accountDomain = trimmedAccountDomain.substr(trimmedAccountDomain.find(":")+1);
					//accountDomains.push_back(accountDomain);
					//Parse the Logon ID and store it in vector
					regex_search(message, logonIDMatch, logonIDRegex);
					for (auto x : logonIDMatch) {
						logonID = x;
					}
					trimmedLogonID = std::regex_replace(logonID, colon_whitespace, ":");
					logonID = trimmedLogonID.substr(trimmedLogonID.find(":")+1);
					//logonIDs.push_back(logonID);
					//Store other values as json
					providers.push_back(string((const char*)(pRecord + sizeof(EVENTLOGRECORD))));
					int rID = ((PEVENTLOGRECORD)pRecord)->RecordNumber;
					recordID.push_back(rID);
					GetTimestamp(((PEVENTLOGRECORD)pRecord)->TimeGenerated, TimeStamp);
					timestamps.push_back(string(TimeStamp));
					rowObject.SetObject();
					rowObject.AddMember("eventID",eventID,allocator);
					rowObject.AddMember("eventProvider",StringRef(providers[timestamp_index].c_str()),allocator);
					rowObject.AddMember("eventType",StringRef(pEventTypeNames[GetEventTypeName(((PEVENTLOGRECORD)pRecord)->EventType)]),allocator);
					rowObject.AddMember("timestamp",StringRef(timestamps[timestamp_index].c_str()),allocator);
					rowObject.AddMember("recordID",recordID[timestamp_index],allocator);
					/*rowObject.AddMember("securityID",StringRef(securityIDs[timestamp_index].c_str()),allocator);
					rowObject.AddMember("accountName",StringRef(accountNames[timestamp_index].c_str()),allocator);
					rowObject.AddMember("accountDomain",StringRef(accountDomains[timestamp_index].c_str()),allocator);
					rowObject.AddMember("logonID",StringRef(logonIDs[timestamp_index].c_str()),allocator);*/
					rowObject.AddMember("message",StringRef(messages[timestamp_index].c_str()),allocator);
					rows.PushBack(rowObject,allocator);
					timestamp_index++;
				}
				/*else {
					providers.push_back(string((const char*)(pRecord + sizeof(EVENTLOGRECORD))));
					int rID = ((PEVENTLOGRECORD)pRecord)->RecordNumber;
					recordID.push_back(rID);
					GetTimestamp(((PEVENTLOGRECORD)pRecord)->TimeGenerated, TimeStamp);
					timestamps.push_back(string(TimeStamp));
					rowObject.SetObject();
					rowObject.AddMember("eventID",eventID,allocator);
					rowObject.AddMember("eventProvider",StringRef(providers[timestamp_index].c_str()),allocator);
					rowObject.AddMember("eventType",StringRef(pEventTypeNames[GetEventTypeName(((PEVENTLOGRECORD)pRecord)->EventType)]),allocator);
					rowObject.AddMember("timestamp",StringRef(timestamps[timestamp_index].c_str()),allocator);
					rowObject.AddMember("recordID",recordID[timestamp_index],allocator);
					rows.PushBack(rowObject,allocator);
					timestamp_index++;
				}*/
				pRecord += ((PEVENTLOGRECORD)pRecord)->Length;
			}
		}		
	}
	obj.AddMember("row",rows,allocator);
	StringBuffer buffer;
	Writer<StringBuffer> writer(buffer);
	obj.Accept(writer);
	//cout<<"The final buffer is "<<endl<<buffer.GetString()<<endl<<endl;
	CloseEventLog(hEventLog);
	providers.clear();
	timestamps.clear();
	recordID.clear();
	securityIDs.clear();
	return env->NewStringUTF(buffer.GetString());
}


// Get the provider DLL 
HANDLE GetMessageResources()
{
    HANDLE hResources = NULL;

    hResources = LoadLibraryEx(RESOURCE_DLL_LIST[current_dll_position], NULL, LOAD_LIBRARY_AS_IMAGE_RESOURCE | LOAD_LIBRARY_AS_DATAFILE);
    if (NULL == hResources)
    {
        wprintf(L"LoadLibrary failed with %lu.\n", GetLastError());
    }

    return hResources;
}

// Get an index value to the pEventTypeNames array based on the event type value.
DWORD GetEventTypeName(DWORD EventType)
{
    DWORD index = 0;

    switch (EventType)
    {
        case EVENTLOG_ERROR_TYPE:
            index = 0;
            break;
        case EVENTLOG_WARNING_TYPE:
            index = 1;
            break;
        case EVENTLOG_INFORMATION_TYPE:
            index = 2;
            break;
        case EVENTLOG_AUDIT_SUCCESS:
            index = 3;
            break;
        case EVENTLOG_AUDIT_FAILURE:
            index = 4;
            break;
    }

    return index;
}


LPSTR GetMessageString(DWORD MessageId, DWORD argc, LPWSTR argv)
{
    LPSTR pMessage = NULL;
    DWORD dwFormatFlags = FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_FROM_HMODULE | FORMAT_MESSAGE_ALLOCATE_BUFFER;
    DWORD_PTR* pArgs = NULL;
    LPSTR pString = (LPSTR)argv;
	cout<<"argc is "<<argc<<endl;

    // The insertion strings appended to the end of the event record
    // are an array of strings; however, FormatMessage requires
    // an array of addresses. Create an array of DWORD_PTRs based on
    // the count of strings. Assign the address of each string
    // to an element in the array (maintaining the same order).
    if (argc > 0)
    {
        pArgs = (DWORD_PTR*)malloc(sizeof(DWORD_PTR) * argc);
        if (pArgs)
        {
            dwFormatFlags |= FORMAT_MESSAGE_ARGUMENT_ARRAY;
			//cout<<"pargs is "<<endl;
            for (DWORD i = 0; i < argc; i++)
            {
                pArgs[i] = (DWORD_PTR)pString;
				//cout<<pArgs[i]<<endl;
                pString += strlen((pString)) + 1;
				cout<<pString<<"\t";
            }
			//cout<<endl;
        }
        else
        {
            dwFormatFlags |= FORMAT_MESSAGE_IGNORE_INSERTS;
            wprintf(L"Failed to allocate memory for the insert string array.\n");
        }
    }
	//cout<<"MessageId is "<<MessageId<<endl;
	int j = 0;
	current_dll_position = 0;
	g_hResources = GetMessageResources();
	while (!FormatMessage(dwFormatFlags,
                       (LPCVOID)g_hResources,
                       MessageId,
                       0,  
                       (LPSTR)&pMessage, 
                       0, 
                       (va_list*)pArgs) && j < 4)
    {
        current_dll_position = (current_dll_position+1)%4;
		g_hResources = GetMessageResources();
		j++;
    }
	if(!FormatMessage(dwFormatFlags,
                       (LPCVOID)g_hResources,
                       MessageId,
                       0,  
                       (LPSTR)&pMessage, 
                       0, 
                       (va_list*)pArgs))
		wprintf(L"Format message failed with %lu\n", GetLastError());
    if (pArgs)
        free(pArgs);

    return pMessage;
}


// If the message string contains parameter insertion strings (for example, %%4096),
// you must perform the parameter substitution yourself. To get the parameter message 
// string, call FormatMessage with the message identifier found in the parameter insertion 
// string (for example, 4096 is the message identifier if the parameter insertion string
// is %%4096). You then substitute the parameter insertion string in the message 
// string with the actual parameter message string. 
DWORD ApplyParameterStringsToMessage(CONST LPSTR pMessage, LPSTR & pFinalMessage)
{
    DWORD status = ERROR_SUCCESS;
    DWORD dwParameterCount = 0;  // Number of insertion strings found in pMessage
    size_t cbBuffer = 0;         // Size of the buffer in bytes
    size_t cchBuffer = 0;        // Size of the buffer in characters
    size_t cchParameters = 0;    // Number of characters in all the parameter strings
    size_t cch = 0;
    DWORD i = 0;
    LPSTR* pStartingAddresses = NULL;  // Array of pointers to the beginning of each parameter string in pMessage
    LPSTR* pEndingAddresses = NULL;    // Array of pointers to the end of each parameter string in pMessage
    DWORD* pParameterIDs = NULL;        // Array of parameter identifiers found in pMessage
    LPSTR* pParameters = NULL;         // Array of the actual parameter strings
    LPSTR pTempMessage = (LPSTR)pMessage;
    LPSTR pTempFinalMessage = NULL;
    // Determine the number of parameter insertion strings in pMessage.
    while (pTempMessage = strchr(pTempMessage, '%'))
    {
        dwParameterCount++;
        pTempMessage++;
		pTempMessage++;
    }
	
    // If there are no parameter insertion strings in pMessage, return.
    if (0 == dwParameterCount)
    {
        pFinalMessage = NULL;
        goto cleanup;
    }

    // Allocate an array of pointers that will contain the beginning address 
    // of each parameter insertion string.
    cbBuffer = sizeof(LPSTR) * dwParameterCount;
    pStartingAddresses = (LPSTR*)malloc(cbBuffer);
    if (NULL == pStartingAddresses)
    {
        wprintf(L"Failed to allocate memory for pStartingAddresses.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pStartingAddresses, cbBuffer);

    // Allocate an array of pointers that will contain the ending address (one
    // character past the of the identifier) of the each parameter insertion string.
    pEndingAddresses = (LPSTR*)malloc(cbBuffer);
    if (NULL == pEndingAddresses)
    {
        wprintf(L"Failed to allocate memory for pEndingAddresses.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pEndingAddresses, cbBuffer);
	
    // Allocate an array of pointers that will contain pointers to the actual
    // parameter strings.
    pParameters = (LPSTR*)malloc(cbBuffer);
    if (NULL == pParameters)
    {
        wprintf(L"Failed to allocate memory for pEndingAddresses.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pParameters, cbBuffer);

    // Allocate an array of DWORDs that will contain the message identifier
    // for each parameter.
    pParameterIDs = (DWORD*)malloc(cbBuffer);
    if (NULL == pParameterIDs)
    {
        wprintf(L"Failed to allocate memory for pParameterIDs.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pParameterIDs, cbBuffer);

    // Find each parameter in pMessage and get the pointer to the
    // beginning of the insertion string, the end of the insertion string,
    // and the message identifier of the parameter.
    pTempMessage = (LPSTR)pMessage;
		//cout<<endl<<endl<<endl<<"Apply parameters"<<endl<<endl<<endl;
    while (pTempMessage = strchr(pTempMessage, '%'))
    {
		//cout<<pTempMessage+1<<endl;
        if (isdigit(*(pTempMessage+2)))
        {
            pStartingAddresses[i] = pTempMessage;

            pTempMessage++;
			pTempMessage++;
            pParameterIDs[i] = (DWORD)atoi(pTempMessage);
			//cout<<pParameterIDs[i]<<endl;
            while (isdigit(*++pTempMessage))
                ;

            pEndingAddresses[i] = pTempMessage;

            i++;
        }
		else
			pTempMessage++;
    }
	
    // For each parameter, use the message identifier to get the
    // actual parameter string.
	//cout<<"Parameters are "<<endl;
    for (DWORD i = 0; i < dwParameterCount; i++)
    {
        //pParameters[i] = (LPSTR)GetMessageString(pParameterIDs[i], 0, NULL);
		//cout<<"For parameter :"<<pParameterIDs[i]<<endl;
        pParameters[i] = (LPSTR)GetMessageString(pParameterIDs[i], 0, NULL);
		//cout<<"para :"<<pParameters[i]<<endl;
        if (NULL == pParameters[i])
        {
            wprintf(L"GetMessageString could not find parameter string for insert %lu.\n", i);
            status = ERROR_INVALID_PARAMETER;
            goto cleanup;
        }

        cchParameters += strlen(pParameters[i]);
    }
	//cout<<"Operation success"<<endl;
    // Allocate enough memory for pFinalMessage based on the length of pMessage
    // and the length of each parameter string. The pFinalMessage buffer will contain 
    // the completed parameter substitution.
    pTempMessage = (LPSTR)pMessage;
    cbBuffer = (strlen(pMessage) + cchParameters + 1) * sizeof(CHAR);
    pFinalMessage = (LPSTR)malloc(cbBuffer);
    if (NULL == pFinalMessage)
    {
        wprintf(L"Failed to allocate memory for pFinalMessage.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }
	//cout<<"Operation success"<<endl;
    RtlZeroMemory(pFinalMessage, cbBuffer);
    cchBuffer = cbBuffer / sizeof(CHAR);
    pTempFinalMessage = pFinalMessage;
	//cout<<"Operation success"<<endl;
    // Build the final message string.
    for (DWORD i = 0; i < dwParameterCount; i++)
    {
		//cout<<"test op"<<endl;
        // Append the segment from pMessage. In the first iteration, this is "8 " and in the
        // second iteration, this is " = 2 ".
		/*#ifdef __STDC_LIB_EXT1__
			cout<<"test success"<<endl; 
			strcpy_s(pTempFinalMessage, cchBuffer, pTempMessage, cch = (pStartingAddresses[i] - pTempMessage));
			pTempMessage = pEndingAddresses[i];
			cchBuffer -= cch;
			cout<<"Temp final is :     "<<pTempFinalMessage<<endl;
			// Append the parameter string. In the first iteration, this is "quarts" and in the
			// second iteration, this is "gallons"
			pTempFinalMessage += cch;
			strcpy_s(pTempFinalMessage, cchBuffer, pParameters[i]);
			//strcpy_s(pTempFinalMessage, cchBuffer, pParameters[i]);
			cchBuffer -= cch = strlen(pParameters[i]);

			pTempFinalMessage += cch;
		#endif
    }
	cout<<"ptempfinal message is "<<pTempFinalMessage<<endl;
	#ifdef __STDC_LIB_EXT1__
		// Append the last segment from pMessage, which is ".".
		strcpy_s(pTempFinalMessage, cchBuffer, pTempMessage);
	#endif*/
	
			//cout<<"test success"<<endl; 
			cch = (pStartingAddresses[i] - pTempMessage);
			strcat(pTempFinalMessage, (string(pTempMessage).substr(0,cch)).c_str());
			pTempMessage = pEndingAddresses[i];
			cchBuffer -= cch;
			//cout<<"Temp final is :     "<<pTempFinalMessage<<endl;
			// Append the parameter string. In the first iteration, this is "quarts" and in the
			// second iteration, this is "gallons"
			//pTempFinalMessage += cch;
			strcat(pTempFinalMessage,pParameters[i]);
			//strcpy_s(pTempFinalMessage, cchBuffer, pParameters[i]);
			//strcpy_s(pTempFinalMessage, cchBuffer, pParameters[i]);
			cchBuffer -= (cch = strlen(pParameters[i]));
			//cout<<"after param is "<<pTempFinalMessage<<endl;
			//pTempFinalMessage += cch;
		//#endif
    }
	//#ifdef __STDC_LIB_EXT1__
		// Append the last segment from pMessage, which is ".".
		strcat(pTempFinalMessage, string(pTempMessage).c_str());
		pFinalMessage = pTempFinalMessage;
		//cout<<"ptempfinal message is "<<pTempFinalMessage<<endl;
		//cout<<"pFinalMessage is   "<<pFinalMessage<<endl;
	//#endif

cleanup:

    if (ERROR_SUCCESS != status)
        pFinalMessage = (LPSTR)pMessage;

    if (pStartingAddresses)
        free(pStartingAddresses);

    if (pEndingAddresses)
        free(pEndingAddresses);

    if (pParameterIDs)
        free(pParameterIDs);

    for (DWORD i = 0; i < dwParameterCount; i++)
    {
        if (pParameters[i])
            LocalFree(pParameters[i]);
    }

    return status;
}


int GetParameterCount(CONST LPSTR pMessage) {
    int dwParameterCount = 0;  // Number of insertion strings found in pMessage
    LPSTR pTempMessage = (LPSTR)pMessage;
	cout<<"Inside get parameter "<<endl<<pTempMessage<<endl<<"over   "<<endl;
    // Determine the number of parameter insertion strings in pMessage.
    while (pTempMessage = strchr(pTempMessage, '%'))
    {
        dwParameterCount++;
        pTempMessage++;
		pTempMessage++;
    }
	return dwParameterCount;
}



LPSTR* GetParameters(CONST LPSTR pMessage)
{
    DWORD status = ERROR_SUCCESS;
    DWORD dwParameterCount = 0;  // Number of insertion strings found in pMessage
    size_t cbBuffer = 0;         // Size of the buffer in bytes
    size_t cchParameters = 0;    // Number of characters in all the parameter strings
    DWORD i = 0;
    LPSTR* pStartingAddresses = NULL;  // Array of pointers to the beginning of each parameter string in pMessage
    LPSTR* pEndingAddresses = NULL;    // Array of pointers to the end of each parameter string in pMessage
    DWORD* pParameterIDs = NULL;        // Array of parameter identifiers found in pMessage
    LPSTR* pParameters = NULL;         // Array of the actual parameter strings
    LPSTR pTempMessage = (LPSTR)pMessage;
    // Determine the number of parameter insertion strings in pMessage.
    while (pTempMessage = strchr(pTempMessage, '%'))
    {
        dwParameterCount++;
        pTempMessage++;
		pTempMessage++;
    }
	
    // Allocate an array of pointers that will contain the beginning address 
    // of each parameter insertion string.
    cbBuffer = sizeof(LPSTR) * dwParameterCount;
    pStartingAddresses = (LPSTR*)malloc(cbBuffer);
    if (NULL == pStartingAddresses)
    {
        wprintf(L"Failed to allocate memory for pStartingAddresses.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pStartingAddresses, cbBuffer);

    // Allocate an array of pointers that will contain the ending address (one
    // character past the of the identifier) of the each parameter insertion string.
    pEndingAddresses = (LPSTR*)malloc(cbBuffer);
    if (NULL == pEndingAddresses)
    {
        wprintf(L"Failed to allocate memory for pEndingAddresses.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pEndingAddresses, cbBuffer);
	
    // Allocate an array of pointers that will contain pointers to the actual
    // parameter strings.
    pParameters = (LPSTR*)malloc(cbBuffer);
    if (NULL == pParameters)
    {
        wprintf(L"Failed to allocate memory for pEndingAddresses.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pParameters, cbBuffer);

    // Allocate an array of DWORDs that will contain the message identifier
    // for each parameter.
    pParameterIDs = (DWORD*)malloc(cbBuffer);
    if (NULL == pParameterIDs)
    {
        wprintf(L"Failed to allocate memory for pParameterIDs.\n");
        status = ERROR_OUTOFMEMORY;
        goto cleanup;
    }

    RtlZeroMemory(pParameterIDs, cbBuffer);

    // Find each parameter in pMessage and get the pointer to the
    // beginning of the insertion string, the end of the insertion string,
    // and the message identifier of the parameter.
    pTempMessage = (LPSTR)pMessage;
    while (pTempMessage = strchr(pTempMessage, '%'))
    {
        if (isdigit(*(pTempMessage+2)))
        {
            pStartingAddresses[i] = pTempMessage;

            pTempMessage++;
			pTempMessage++;
            pParameterIDs[i] = (DWORD)atoi(pTempMessage);
            while (isdigit(*++pTempMessage))
                ;

            pEndingAddresses[i] = pTempMessage;

            i++;
        }
		else
			pTempMessage++;
    }
	
    // For each parameter, use the message identifier to get the
    // actual parameter string.
    for (DWORD i = 0; i < dwParameterCount; i++)
    {
        //pParameters[i] = (LPSTR)GetMessageString(pParameterIDs[i], 0, NULL);
        pParameters[i] = (LPSTR)GetMessageString(pParameterIDs[i], 0, NULL);
        if (NULL == pParameters[i])
        {
            wprintf(L"GetMessageString could not find parameter string for insert %lu.\n", i);
            status = ERROR_INVALID_PARAMETER;
            goto cleanup;
        }

        cchParameters += strlen(pParameters[i]);
    }
	cleanup:

    if (pStartingAddresses)
        free(pStartingAddresses);

    if (pEndingAddresses)
        free(pEndingAddresses);

    if (pParameterIDs)
        free(pParameterIDs);

    for (DWORD i = 0; i < dwParameterCount; i++)
    {
        if (pParameters[i])
            LocalFree(pParameters[i]);
    }
	return pParameters;
}


// Get a string that contains the time stamp of when the event 
// was generated.
void GetTimestamp(const DWORD Time, char* DisplayString)
{
    ULONGLONG ullTimeStamp = 0;
    ULONGLONG SecsTo1970 = 116444736000000000;
    SYSTEMTIME st;
    FILETIME ft, ftLocal;
	LPWSTR format = (LPWSTR)"%d/%d/%d %d:%d:%d";
    ullTimeStamp = Int32x32To64(Time, 10000000) + SecsTo1970;
    ft.dwHighDateTime = (DWORD)((ullTimeStamp >> 32) & 0xFFFFFFFF);
    ft.dwLowDateTime = (DWORD)(ullTimeStamp & 0xFFFFFFFF);
    
    FileTimeToLocalFileTime(&ft, &ftLocal);
    FileTimeToSystemTime(&ftLocal, &st);
	sprintf(DisplayString,"%.2d-%.2d-%d %.2d:%.2d:%.2d",st.wMonth,st.wDay,st.wYear,st.wHour,st.wMinute, st.wSecond);
}

