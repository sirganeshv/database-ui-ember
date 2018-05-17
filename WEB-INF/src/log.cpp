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
using namespace std;


HANDLE GetMessageResources();
LPSTR GetMessageString(DWORD Id, DWORD argc, LPWSTR args);
DWORD DumpRecordsInBuffer(PBYTE pBuffer, DWORD dwBytesRead,jint *pid);
DWORD GetEventTypeName(DWORD EventType);
void GetTimestamp(const DWORD Time, char* DisplayString);
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
	col.PushBack("Account Name",allocator);
	//col.PushBack("Account Domain",allocator);
	//col.PushBack("Logon ID",allocator);
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
			char* pMessage = NULL;
			LPWSTR pFinalMessage = NULL;
			bool flag = false;
			//cout<<"Record id is "<<((PEVENTLOGRECORD)pRecord)->RecordNumber <<" and lastInsertedRecordID is "<<lastInsertedRecordID<<"\n";
			/*if(((PEVENTLOGRECORD)pRecord)->RecordNumber <= lastInsertedRecordID) {
				lastRecordReached = true;
				break;
			}*/
			while (pRecord < pEndOfRecords) {
				if(((PEVENTLOGRECORD)pRecord)->RecordNumber <= lastInsertedRecordID) {
					cout<<"true\n";
					lastRecordReached = true;
					break;
				}
				int eventID = (((PEVENTLOGRECORD)pRecord)->EventID & 0xFFFF);
				//for(int i = 0;i < len;i++) {
					//if(pId[i] == eventID) {
						//int i = 0;
					regex securityRegex("Security ID:\\s*[-a-zA-Z0-9]+");
					smatch securityMatch;
					if(eventID == 4800 || eventID == 4801) 
					{
						pMessage = (char*)GetMessageString(((PEVENTLOGRECORD)pRecord)->EventID, 
							((PEVENTLOGRECORD)pRecord)->NumStrings, (LPWSTR)(pRecord + ((PEVENTLOGRECORD)pRecord)->StringOffset));
						regex_search(string(pMessage), securityMatch, securityRegex);
						string securityID;
						for (auto x : securityMatch) {
							securityID = x;
						}
						regex colon_whitespace(":\\s+");
						string trimmedID = std::regex_replace(securityID, colon_whitespace, ":");
						securityID = trimmedID.substr(trimmedID.find(":")+1);
						securityIDs.push_back(securityID);
						const char *rec;
						providers.push_back(string((const char*)(pRecord + sizeof(EVENTLOGRECORD))));
						int rID = ((PEVENTLOGRECORD)pRecord)->RecordNumber;
						rec = (const char *) (intptr_t) (((PEVENTLOGRECORD)pRecord)->RecordNumber);
						recordID.push_back(rID);
						GetTimestamp(((PEVENTLOGRECORD)pRecord)->TimeGenerated, TimeStamp);
						timestamps.push_back(string(TimeStamp));
						rowObject.SetObject();
						rowObject.AddMember("eventID",eventID,allocator);
						rowObject.AddMember("eventProvider",StringRef(providers[timestamp_index].c_str()),allocator);
						rowObject.AddMember("eventType",StringRef(pEventTypeNames[GetEventTypeName(((PEVENTLOGRECORD)pRecord)->EventType)]),allocator);
						rowObject.AddMember("timestamp",StringRef(timestamps[timestamp_index].c_str()),allocator);
						rowObject.AddMember("recordID",recordID[timestamp_index],allocator);
						rowObject.AddMember("securityID",StringRef(securityIDs[timestamp_index].c_str()),allocator);
						rows.PushBack(rowObject,allocator);
						timestamp_index++;
					}
						//break;
					//}
				//}
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

    hResources = LoadLibraryEx(RESOURCE_DLL, NULL, LOAD_LIBRARY_AS_IMAGE_RESOURCE | LOAD_LIBRARY_AS_DATAFILE);
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
    LPCTSTR pString = (LPCTSTR)argv;
	//cout<<argc<<endl;

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
				//cout<<pString<<"\t";
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
	if (!FormatMessage(dwFormatFlags,
                       (LPCVOID)g_hResources,
                       MessageId,
                       0,  
                       (LPSTR)&pMessage, 
                       0, 
                       (va_list*)pArgs))
    {
        wprintf(L"Format message failed with %lu\n", GetLastError());
    }
    if (pArgs)
        free(pArgs);

    return pMessage;
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

