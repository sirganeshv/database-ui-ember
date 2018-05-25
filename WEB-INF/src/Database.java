import java.util.Scanner;
import java.io.*;  
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.ParseException;
//import org.json.simple.parser.JSONParser;
import net.minidev.json.*; 
import net.minidev.json.parser.*; 
//import org.json.*;
import java.util.ArrayList;
import org.elasticsearch.client.*;
import org.elasticsearch.node.Node;	
import com.google.gson.stream.JsonReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;


public class Database {
	
	static {
		System.load("D:\\Log\\Log.dll");
	}
	
	//private native String getTableAsJson(int lastInsertedRecordID);
	private native String getTableAsJson(int lastInsertedRecordID);
	
	public int updateIndex(int lastInsertedRecordID,Node node) {
		try {
			Database db = new Database();
			//JSONParser parser = new JSONParser();
			JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE); 
			System.out.println("last inserted id is ");
			String jsonStr = db.getTableAsJson(lastInsertedRecordID);
			System.out.println("DOne fetching");
			//InputStream is = new ByteArrayInputStream(jsonStr.getBytes());
			//BufferedReader br = new BufferedReader(new InputStreamReader(is));
			//InputStream in = IOUtils.toInputStream(jsonStr, "UTF-8");
			/*JsonElement jelement = new JsonParser().parse(in);
			JsonObject  jobject = jelement.getAsJsonObject();
			JSONObject json = null;*/
			//JsonReader jsonReader = new JsonReader(new StringReader(jsonStr));
			System.out.println(jsonStr);
			if(jsonStr != null) {
				JSONObject json = (JSONObject) parser.parse(jsonStr);
				System.out.println("Done parsing");
				ElasticClient elasticClient = new ElasticClient();
				System.out.println(lastInsertedRecordID);
				//lastInsertedRecordID = elasticClient.insertLog(json,lastInsertedRecordID);
				lastInsertedRecordID = elasticClient.insertLog(json,lastInsertedRecordID,node);
				System.out.println(lastInsertedRecordID);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return lastInsertedRecordID;
	}
}