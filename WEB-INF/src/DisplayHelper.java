import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DisplayHelper {
	public ArrayList<JSONObject> sort(JSONArray rows,String sortProperties) {
		ArrayList<JSONObject> sortedJsonRows = new ArrayList<JSONObject>();
		for(int i = 0;i < rows.size();i++) 
			sortedJsonRows.add((JSONObject)rows.get(i));
		Collections.sort(sortedJsonRows,new MyJsonComparator(sortProperties));
		return sortedJsonRows;
	}
	public List<JSONObject> filter(ArrayList<JSONObject> sortedJsonRows,String filterCol,String filterValue) {
		return sortedJsonRows.stream().filter( c -> String.valueOf(c.get(filterCol)).toUpperCase().contains(filterValue.toUpperCase())).collect(Collectors.toList());
	}
	public JSONArray paginate(List<JSONObject> filteredList,int start,int stop) {
		JSONArray pagedRows = new JSONArray();
		for(int i = start;i < stop && i < filteredList.size();i++)
			pagedRows.add(filteredList.get(i));
		return pagedRows;
	}
}

class MyJsonComparator implements Comparator<JSONObject> {
	String sortProperties;
	public MyJsonComparator(String prop) {
		sortProperties = new String(prop);
	}
	@Override
	public int compare(JSONObject o1,JSONObject o2) {
		String v1 = (String.valueOf(o1.get(sortProperties)));
		String v3 = (String.valueOf(o2.get(sortProperties)));
		if(v1.matches("[0-9]+")) {
			int num1 = Integer.parseInt(v1);
			int num2 = Integer.parseInt(v3);
			return num1 < num2 ? -1 : num1 > num2 ? +1 : 0;
		}
		else
			return (v1.toUpperCase()).compareTo(v3.toUpperCase());
	}
}