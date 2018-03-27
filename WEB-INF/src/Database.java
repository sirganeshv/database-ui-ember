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
	
	private native String getTableAsJson(int[] idList);
	
		
	public JSONObject getTableJson(int[] idList) {
		try {
			Database db = new Database();
			JSONParser parser = new JSONParser();
			System.out.println("Gonna enter native method");
			String jsonStr = db.getTableAsJson(idList);
			JSONObject json = (JSONObject) parser.parse(jsonStr);
			return json;
		}
		catch(ParseException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}