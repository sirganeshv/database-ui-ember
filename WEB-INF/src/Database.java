import java.util.Scanner;
import java.io.*;  
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.*;
import java.util.ArrayList;

public class Database {
	
	static {
		System.load("D:\\Log\\Log.dll");
	}
	
	//private native String getTableAsJson(int lastInsertedRecordID);
	private native String getTableAsJson(int lastInsertedRecordID);
	
	public int updateIndex(int lastInsertedRecordID) {
		try {
			Database db = new Database();
			JSONParser parser = new JSONParser();
			System.out.println("last inserted id is ");
			String jsonStr = db.getTableAsJson(lastInsertedRecordID);
			JSONObject json = (JSONObject) parser.parse(jsonStr);
			ElasticClient elasticClient = new ElasticClient();
			System.out.println(lastInsertedRecordID);
			//lastInsertedRecordID = elasticClient.insertLog(json,lastInsertedRecordID);
			lastInsertedRecordID = elasticClient.insertLog(json,lastInsertedRecordID);
			System.out.println(lastInsertedRecordID);
		}
		catch(ParseException ex) {
			ex.printStackTrace();
		}
		return lastInsertedRecordID;
	}
}