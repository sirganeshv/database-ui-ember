
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
#define RESOURCE_DLL "C:\\Windows\\System32\\evr.dll"
#define MAX_TIMESTAMP_LEN 23 + 1   
#define MAX_RECORD_BUFFER_SIZE  0x10000  // 64K

HANDLE GetMessageResources();
DWORD DumpRecordsInBuffer(PBYTE pBuffer, DWORD dwBytesRead,jint *pid);
DWORD GetEventTypeName(DWORD EventType);
void GetTimestamp(const DWORD Time, char* DisplayString);
std::string newTimeStamp = std::string("");
std::string extension = std::string(".dat");

const char* pEventTypeNames[] = {"Error", "Warning", "Informational", "Audit Success", "Audit Failure"};
	
HANDLE g_hResources = NULL;
using namespace std;
using namespace rapidjson;

int count = 0;
jsize len = 0;



JNIEXPORT jstring JNICALL Java_Database_getTableAsJson(JNIEnv *env, jobject jobj, jintArray idList)
{
	cout<<"enterd";
	std::setlocale(LC_ALL, "en_US.utf8");
	len = env->GetArrayLength(idList);
	jint *pId = env->GetIntArrayElements(idList, 0);
	const char* json = "{}";
	Document obj;
	obj.Parse(json);
	obj.SetObject();
	Document::AllocatorType& allocator = obj.GetAllocator();
	Value col(kArrayType);
	col.PushBack("eventID",allocator);
	col.PushBack("eventProvider",allocator);
	col.PushBack("timestamp",allocator);
	obj.AddMember("col",col,allocator);
    HANDLE hEventLog = NULL;
    DWORD status = ERROR_SUCCESS;
    DWORD dwBytesToRead = 0;
    DWORD dwBytesRead = 0;
    DWORD dwMinimumBytesToRead = 0;
    PBYTE pBuffer = NULL;
    PBYTE pTemp = NULL;
	Value rows(kArrayType);
	Document rowObject;
	string providers[len] = {};
	string eventType;
	string timestamp;
	vector <string> timestamps;
	char* TimeStamp[MAX_TIMESTAMP_LEN];
	int timestamp_index = 0;
	//wstring;
    // The source name (provider) must exist as a subkey of Application.
	if(NULL == hEventLog) {
		hEventLog = OpenEventLog(NULL, (LPCSTR) PROVIDER_NAME);
		if (NULL == hEventLog)
		{
			wprintf(L"OpenEventLog failed with 0x%x.\n", GetLastError());
			//goto cleanup;
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
			//goto cleanup;
			return NULL;
		}
	}
	// Allocate an initial block of memory used to read event records. 
	dwBytesToRead = MAX_RECORD_BUFFER_SIZE;
	pBuffer = (PBYTE)malloc(dwBytesToRead);
	if (NULL == pBuffer)
	{
		wprintf(L"Failed to allocate the initial memory for the record buffer.\n");
		//goto cleanup;
		return NULL;
	}
	//cout<<"let us start doing work"<<endl;
	// Read blocks of records until you reach the end of the log or an error occurs.
	while (ERROR_SUCCESS == status)
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
					//goto cleanup;
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
			//DumpRecordsInBuffer(pBuffer, dwBytesRead,pId);
			count++;
			DWORD status = ERROR_SUCCESS;
			unsigned char* pRecord = pBuffer;
			unsigned char* pEndOfRecords = pBuffer + dwBytesRead;
			char TimeStamp[MAX_TIMESTAMP_LEN];
			LPWSTR pMessage = NULL;
			LPWSTR pFinalMessage = NULL;
			bool flag = false;
			while (pRecord < pEndOfRecords) {
				int eventID = (((PEVENTLOGRECORD)pRecord)->EventID & 0xFFFF);
				for(int i = 0;i < len;i++) {
					if(pId[i] == eventID) {
						providers[i] = (const char*)(pRecord + sizeof(EVENTLOGRECORD));
						GetTimestamp(((PEVENTLOGRECORD)pRecord)->TimeGenerated, TimeStamp);
						//timestamp[timestamp_index] = string(TimeStamp);
						timestamps.push_back(string(TimeStamp));
						rowObject.SetObject();
						rowObject.AddMember("eventID",eventID,allocator);
						cout<<StringRef(providers[i].c_str())<<endl;
						rowObject.AddMember("eventProvider",StringRef(providers[i].c_str()),allocator);
						rowObject.AddMember("timestamp",StringRef(timestamps[timestamp_index].c_str()),allocator);
						rows.PushBack(rowObject,allocator);
						StringBuffer buffer;
						Writer<StringBuffer> writer(buffer);
						rows.Accept(writer);
						std::string jsonStr = string(buffer.GetString());
						cout<<jsonStr<<endl;
						timestamp_index++;
						break;
					}
				}
				pRecord += ((PEVENTLOGRECORD)pRecord)->Length;
			}
		}
		StringBuffer buffer;
		Writer<StringBuffer> writer(buffer);
		rows.Accept(writer);
		std::string jsonStr = string(buffer.GetString());
		cout<<"Temp rows are "<<endl<<jsonStr<<endl<<endl;		
	}
	StringBuffer bufferr;
	Writer<StringBuffer> writerr(bufferr);
	rows.Accept(writerr);
	std::string jsonStrr = string(bufferr.GetString());
	cout<<"The final rows are "<<endl<<jsonStrr<<endl<<endl;
	obj.AddMember("row",rows,allocator);
	StringBuffer buffer;
	Writer<StringBuffer> writer(buffer);
	obj.Accept(writer);
	string jsonStr = string(buffer.GetString());
	cout<<"The final buffer is "<<endl<<buffer.GetString()<<endl<<endl;
	cout<<"The final string to be sent is "<<endl<<jsonStr<<endl<<endl;
	std::cout << buffer.GetString() << std::endl;
	CloseEventLog(hEventLog);
	return env->NewStringUTF(buffer.GetString());
/*cleanup:

    if (hEventLog)
        CloseEventLog(hEventLog);

    if (pBuffer)
        free(pBuffer);*/
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


// Loop through the buffer and print the contents of each record 
// in the buffer.
DWORD DumpRecordsInBuffer(PBYTE pBuffer, DWORD dwBytesRead,jint *pid)
{
	//Document::AllocatorType& allocator = obj.GetAllocator();
	count++;
    DWORD status = ERROR_SUCCESS;
    PBYTE pRecord = pBuffer;
    PBYTE pEndOfRecords = pBuffer + dwBytesRead;
	char TimeStamp[MAX_TIMESTAMP_LEN];
    LPWSTR pMessage = NULL;
    LPWSTR pFinalMessage = NULL;
	//int count = 0;
	bool flag = false;
    while (pRecord < pEndOfRecords)
    {
            /*string eventStr = "event";
            char* newEvent = (char*)eventStr.c_str();
            //cout<<"ther is some"<<endl;
			int eventID = (((PEVENTLOGRECORD)pRecord)->EventID & 0xFFFF);
			char* eventProvider = (char*)(pRecord + sizeof(EVENTLOGRECORD));
			for(int i = 0;i < len;i++) {
				if(pid[i] == eventID) {
					rowObject.SetObject();
					cout<<"pid is "<<pid[i]<<" and event id is "<<eventID<<endl;
					//cout<<"true"<<endl;
					rowObject.AddMember("eventID",eventID,allocator);
					rowObject.AddMember("eventProvider",StringRef(eventProvider),allocator);
					rows.PushBack(rowObject,allocator);
					//obj.AddMember(StringRef(newEvent),eventID,allocator);
					//cout<<"object added"<<endl;
					break;
				}
				//else
					//cout<<"false";
			}*/
			/*file<<"Time stamp: "<<TimeStamp<<endl;
			file<<"Source: "<<pRecord + sizeof(EVENTLOGRECORD)<<endl;
			file<<"record number: "<<(((PEVENTLOGRECORD)pRecord)->RecordNumber)<<endl;
			file<<"EventID "<<(((PEVENTLOGRECORD)pRecord)->EventID & 0xFFFF)<<endl;
			file<<"event type: "<<(pEventTypeNames[GetEventTypeName(((PEVENTLOGRECORD)pRecord)->EventType)])<<endl;*/
            //wprintf(L"\n");

        pRecord += ((PEVENTLOGRECORD)pRecord)->Length;
    }
	//cout<<"count is "<<count<<"\n";
    return status;
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

