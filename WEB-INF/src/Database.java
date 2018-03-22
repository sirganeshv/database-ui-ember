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
		System.load("D:\\Database\\Database.dll");
	}
	
	private native String getTableAsJson(String tableName,int[] idList);
	
	//public static final String tableName = "customers";
		
	public JSONObject getTableJson(String tableName,int[] idList) {
	//public static void main(String[] args) {
		try {
			Database db = new Database();
			JSONParser parser = new JSONParser();
			System.out.println("Gonna enter native method");
			//int[] idList = {1111,1112};
			JSONObject json = (JSONObject) parser.parse(db.getTableAsJson(tableName,idList));
			return json;
			//System.out.println(json.toJSONString());
		}
		catch(ParseException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}