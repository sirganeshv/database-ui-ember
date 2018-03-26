import java.sql.*; 
import javax.servlet.http.*;  
import javax.servlet.*;  
import java.io.*;  
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.*;
import java.util.*;

public class DatabaseServlet extends HttpServlet{
	private static final String GET_TABLES = "/get_tables";
	private static final String GET_TABLE = "/get_table";
	private static final String NO_OF_RECORDS = "/no_of_records";
	private static final String GET_PAGE = "/get_page";
	private static JSONObject jsonobj = null;

	public void doGet(HttpServletRequest req,HttpServletResponse res)  
	throws ServletException,IOException {
		doProcess(req,res);
	}

	public void doPost(HttpServletRequest req,HttpServletResponse res)  
	throws ServletException,IOException  {  
		doProcess(req,res);
	}
	
    public void doProcess(HttpServletRequest req,HttpServletResponse res)  
	throws ServletException,IOException {  
		PrintWriter pw=res.getWriter();
		Connection conn = null;
        try{  
			conn = getConnection();
			String path = req.getServletPath();
			res.setContentType("text/html");
			switch (path) {
				case GET_TABLES: 
				case GET_TABLE: {
					String table_name = req.getParameter("table_name");
					getServletContext().log(table_name);
					if(table_name != null) {
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
						getServletContext().log("got count");
						if(rs.next()) {
							getServletContext().log("inside rs");
							int count = rs.getInt(1);
							int[] idList = new int[count];
							Statement statement = conn.createStatement();
							ResultSet resultId = statement.executeQuery("select * from id");
							int j = 0;
							while(resultId.next()) {
								idList[j] = resultId.getInt(1);
								j++;
							}
							Database db = new Database();
							JSONObject obj = db.getTableJson(idList);
						//System.out.println(obj.toJSONString());
						/*DatabaseMetaData dbm = conn.getMetaData();
						ResultSet rss = dbm.getTables(null, null, "%", null);
						while (rss.next()) {
						  //System.out.println(rss.getString(3));
						}
						ResultSet tables = dbm.getTables(null, null, table_name, null);
						if (tables.next()) {
							Statement stmt = conn.createStatement(); 
							ResultSet rs = stmt.executeQuery("select * from "+table_name);
							ResultSetMetaData rsmd = rs.getMetaData();              
							//System.out.println();
							JSONObject obj = new JSONObject();
							JSONArray col = new JSONArray();
							for(int i = 0;i < rsmd.getColumnCount();i++ ) {
								//System.out.print(rsmd.getColumnName(i+1)+"\t");
								col.add(i, rsmd.getColumnName(i+1));
							}
							obj.put("col",col);
							//System.out.println();
							int j = 0;
							JSONArray rows = new JSONArray();
							while(rs.next()) {
								//JSONArray rowi = new JSONArray();
								JSONObject rowObject = new JSONObject();
								for(int i = 0 ;i < rsmd.getColumnCount();i++) {
									rowObject.put(rsmd.getColumnName(i+1),rs.getString(i+1));
									//rowi.add(rs.getString(i+1));
									//System.out.print(rs.getString(i+1)+"\t");
								}
								rows.add(rowObject);
								j++;
								//System.out.println();
							}
							obj.put("row",rows);*/	
							getServletContext().log("sending objj");
							pw.println(obj);
							getServletContext().log("Sent");
						}
						/*}*/
						else
							pw.println("false");
					}
					else
						pw.println("false");
					conn.close();
				}
				break;
				case NO_OF_RECORDS: {
					String table_name = req.getParameter("table_name");
					getServletContext().log(table_name);
					if(table_name != null) {
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
						getServletContext().log("got count");
						if(rs.next()) {
							getServletContext().log("inside rs");
							int count = rs.getInt(1);
							int[] idList = new int[count];
							Statement statement = conn.createStatement();
							ResultSet resultId = statement.executeQuery("select * from id");
							int j = 0;
							while(resultId.next()) {
								idList[j] = resultId.getInt(1);
								j++;
							}
							Database db = new Database();
							JSONObject obj = db.getTableJson(idList);
							JSONParser parser = new JSONParser();
					/*String table_name = req.getParameter("table_name");
					String jsonString = req.getParameter("data");
					JSONParser parser = new JSONParser();
					//JSONObject obj = (JSONObject) parser.parse(jsonString);
					/*if(table_name != null) {
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
						if(rs.next()) {
							int count = rs.getInt(1);
							int[] idList = new int[count];
							Statement statement = conn.createStatement();
							ResultSet resultId = statement.executeQuery("select * from id");
							int j = 0;
							while(resultId.next()) {
								idList[j] = resultId.getInt(1);
								j++;
							}
							Database db = new Database();
							JSONObject obj = db.getTableJson(idList);*/
							JSONArray rows = (JSONArray) obj.get("row");
							System.out.println("Row created");
							System.out.println("The size is "+rows.size());
							pw.println(rows.size());
							System.out.println("Sent rows");
						}
						else
							pw.println(0);
					}
					else
						pw.println(0);
							/*System.out.println("table_name");
							Statement stmt = conn.createStatement(); 
							ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
							if(rs.next()) {
								System.out.println(rs.getInt(1));
								pw.println(rs.getInt(1));
							}
							else
								pw.println(0);*/
						//}
					//}
					/*else 
						pw.println(0);*/
					conn.close();
				}
				break;
				case GET_PAGE: {
					String table_name = req.getParameter("table_name");
					/*String jsonString = req.getParameter("data");
					JSONParser parser = new JSONParser();
					System.out.println("let us paginate");*/
					//JSONObject obj = (JSONObject) parser.parse(jsonString);
					if(table_name != null) {
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
						rs.next();
						int count = rs.getInt(1);
						int[] idList = new int[count];
						Statement statement = conn.createStatement();
						ResultSet resultId = statement.executeQuery("select * from id");
						int j = 0;
						while(resultId.next()) {
							idList[j] = resultId.getInt(1);
							j++;
						}
						Database db = new Database();
					
					int start = Integer.parseInt(req.getParameter("start"));
					System.out.println(start);
					int stop = Integer.parseInt(req.getParameter("stop"));
					System.out.println(stop);
					if(jsonobj == null)
						jsonobj = db.getTableJson(idList);
					System.out.println(jsonobj.toJSONString());
					JSONArray rows = (JSONArray)jsonobj.get("row");
					ArrayList<JSONObject> sortedJsonRows = new ArrayList<JSONObject>();
					for(int i = 0;i < rows.size();i++) 
						sortedJsonRows.add((JSONObject)rows.get(i));
					Collections.sort(sortedJsonRows,new MyJsonComparator());
					System.out.println();
					for (JSONObject obj : sortedJsonRows)
						System.out.println(obj.toJSONString());
					System.out.println();
					JSONArray cols = (JSONArray)jsonobj.get("col");
					System.out.println("printing rowssss");
					//System.out.println(rows.toJSONString());
					//System.out.println(cols.toJSONString());
					JSONObject pagedObject = new JSONObject();
					System.out.println("construct obj");
					JSONArray pagedRows = new JSONArray(); 
					for(int i = start;i < stop && i < rows.size();i++)
						pagedRows.add(sortedJsonRows.get(i));
					System.out.println(pagedRows.toJSONString());
					System.out.println("obj constructed");
					pagedObject.put("col",cols);
					pagedObject.put("row",pagedRows);
					pw.println(pagedObject);
					}
					else {
						pw.println("");
					}
					conn.close();
				}
				break;
			}
        }
		catch(SQLException e) {
			System.out.println(e);
			
        }  
        catch(Exception e){ 
            System.out.println(e);
        }  
		finally {
			pw.close();
		}
    }
    private static Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/banking";
        return DriverManager.getConnection(url, "root", "");
  }
}  

class MyJsonComparator implements Comparator<JSONObject> {
	@Override
	public int compare(JSONObject o1,JSONObject o2) {
		String v1 = (String)(String.valueOf(o1.get("eventID")));
		String v3 = (String)(String.valueOf(o2.get("eventID")));
		System.out.println(v1 + "   "+v3);
		if(v1.matches("[0-9]+")) {
			int num1 = Integer.parseInt(v1);
			int num2 = Integer.parseInt(v3);
			return num1 < num2 ? -1 : num1 > num2 ? +1 : 0;
		}
		else
			return (v1.toUpperCase()).compareTo(v3.toUpperCase());
	}
}