#include <mysql.h>
#include <stdio.h>
#include <iostream>
#include <time.h>
#include <jni.h>
#include <sstream>
// rapidjson/example/simpledom/simpledom.cpp`
#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/reader.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/allocators.h"
#include "rapidjson/pointer.h"
#include "rapidjson/schema.h"
#include "Database.h"

using namespace rapidjson;
using namespace std;
MYSQL_RES *result;
MYSQL *con;

//Display the error message and exit
void finish_with_error(MYSQL *con)
{
  fprintf(stderr, "%s\n", mysql_error(con));
  mysql_close(con);
  exit(1);        
}

void init_db() {
	con = mysql_init(NULL);
	if (con == NULL) {
      fprintf(stderr, "%s\n", mysql_error(con));
      exit(1);
	}
	if (mysql_real_connect(con, "localhost", "root", "","banking", 0, NULL, 0) == NULL) {
      finish_with_error(con);
  }  
}

void close_db() {
	mysql_close(con);
}

JNIEXPORT jstring JNICALL Java_Database_getTableAsJson(JNIEnv *env, jobject jobj, jstring tableName, jintArray idList) {
	cout<<"Entered native method\n";
	const char* table_name = env->GetStringUTFChars(tableName, 0);
	jsize len = env->GetArrayLength(idList);
	jint *ids = env->GetIntArrayElements(idList, 0);
	char* idString;
	stringstream str;
	for(int i = 0;i < len;i++) {
		str << ids[i];
		if(i != len-1)
			str << ",";
	}
	cout<<str.str();
	env->ReleaseIntArrayElements(idList,ids, 0);
	const char* json = "{}";
    Document obj;
	Document e;
	Document::AllocatorType& allocator = obj.GetAllocator();
    obj.Parse(json);
	init_db();
	cout<<idString<<endl;
	char query[150];
	sprintf(query,"SELECT * FROM %s where acc_no in (%s)",table_name,str.str().c_str());
	if (mysql_query(con,query)) {
      finish_with_error(con);
	}
	result = mysql_store_result(con);
	if (result == NULL) {
		finish_with_error(con);
	}
	int num_fields = mysql_num_fields(result);
	MYSQL_FIELD *field;
	unsigned int name_field;
	const char* headers[num_fields];
	obj.SetObject();
	Value col(kArrayType);
	for(unsigned int i = 0; (field = mysql_fetch_field(result)); i++) {
		headers[i] = field->name;
		//cout<<i<<endl;
		col.PushBack(StringRef(headers[i]),allocator);
	}
	cout<<num_fields<<endl;
	obj.AddMember("col",col,allocator);
	MYSQL_ROW row;
	Value rows(kArrayType);
	Document rowObject;
	while((row = mysql_fetch_row(result))) {
		rowObject.SetObject(); 
		for(unsigned int i = 0; i < num_fields; i++) {
			rowObject.AddMember(StringRef(headers[i]),StringRef(row[i]),allocator); 
		}
		//cout<<endl;
		rows.PushBack(rowObject,allocator);
	}
	//cout<<"Entered col";
	obj.AddMember("row",rows,allocator);
	StringBuffer buffer;
    Writer<StringBuffer> writer(buffer);
    obj.Accept(writer);
    //std::cout << buffer.GetString() << std::endl;
	close_db();
	return env->NewStringUTF(buffer.GetString());
	
}
