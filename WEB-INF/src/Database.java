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
	
	private native String getTableAsJson();
	
	public void updateIndex() {
		try {
			Database db = new Database();
			JSONParser parser = new JSONParser();
			String jsonStr = db.getTableAsJson();
			JSONObject json = (JSONObject) parser.parse(jsonStr);
			ElasticClient elasticClient = new ElasticClient();
			elasticClient.insertLog(json);
		}
		catch(ParseException ex) {
			ex.printStackTrace();
		}
	}
}