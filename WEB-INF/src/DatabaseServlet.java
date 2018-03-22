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

public class DatabaseServlet extends HttpServlet{
	private static final String GET_TABLES = "/get_tables";
	private static final String GET_TABLE = "/get_table";
	private static final String NO_OF_RECORDS = "/no_of_records";
	private static final String GET_PAGE = "/get_page";
	
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
					if(table_name != null) {
						Statement stmt = conn.createStatement();
						getServletContext().log("Enter table");
						getServletContext().log(table_name);
						ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
						getServletContext().log("Done table");
						if(rs.next()) {
							getServletContext().log("There are ids");
							int count = rs.getInt(1);
							int[] idList = new int[count];
							getServletContext().log("gonna create statemnet");
							Statement statement = conn.createStatement();
							getServletContext().log("letus query");
							ResultSet resultId = statement.executeQuery("select * from id");
							int j = 0;
							while(resultId.next()) {
								idList[j] = resultId.getInt(1);
								getServletContext().log(String.valueOf(resultId.getInt(1)));
								j++;
							}
							getServletContext().log("pass it to native");
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
							getServletContext().log("Going to return whole obj");
							pw.println(obj);
						}
						getServletContext().log("Done");
						/*}
						else
							pw.println("false");*/
					}
					getServletContext().log("closing connection");
					conn.close();
				}
				break;
				case NO_OF_RECORDS: {
					String table_name = req.getParameter("table_name");
					if(table_name != null) {
						Statement stmt = conn.createStatement();
						getServletContext().log("Enter table n");
						getServletContext().log(table_name+" n ");
						ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
						getServletContext().log("Done table n");
						if(rs.next()) {
							getServletContext().log("There are ids n");
							int count = rs.getInt(1);
							int[] idList = new int[count];
							getServletContext().log("gonna create statemnet n");
							Statement statement = conn.createStatement();
							getServletContext().log("letus query n ");
							ResultSet resultId = statement.executeQuery("select * from id");
							int j = 0;
							while(resultId.next()) {
								idList[j] = resultId.getInt(1);
								getServletContext().log(String.valueOf(resultId.getInt(1)));
								j++;
							}
							getServletContext().log("pass it to native n");
							Database db = new Database();
							JSONObject obj = db.getTableJson(idList);
							getServletContext().log("Got object n");
							JSONArray rows = (JSONArray) obj.get("row");
							getServletContext().log("No of records are "+rows.size());
							pw.println(rows.size());
							/*System.out.println("table_name");
							Statement stmt = conn.createStatement(); 
							ResultSet rs = stmt.executeQuery("select count(*) from "+table_name);
							if(rs.next()) {
								System.out.println(rs.getInt(1));
								pw.println(rs.getInt(1));
							}
							else
								pw.println(0);*/
						}
					}
					else 
						pw.println(0);
					conn.close();
				}
				break;
				case GET_PAGE: {
					String table_name = req.getParameter("table_name");
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
						JSONObject obj = db.getTableJson(idList);
						//getServletContext().log(obj.toJSONString());
						int start = Integer.parseInt(req.getParameter("start"));
						System.out.println(start);
						int stop = Integer.parseInt(req.getParameter("stop"));
						System.out.println(stop);
						System.out.println(obj.toJSONString());
						//JSONArray cols = (JSONArray) cols.get("col");
						JSONArray rows = (JSONArray) obj.get("row");
						getServletContext().log("got row");
						JSONObject pagedObject = new JSONObject();
						JSONArray pagedRows = new JSONArray();
						for(int i = start;i < stop && i < rows.size();i++)
							pagedRows.add(rows.get(i));
						getServletContext().log("got separate rows");
						pagedObject.put("row",pagedRows);
						getServletContext().log("put row");
						//for(int i = 
						//DatabaseMetaData dbm = conn.getMetaData();
						//Statement stmt = conn.createStatement(); 
						//ResultSet rs = stmt.executeQuery("select * from "+table_name+" limit "+start+","+stop);
						//ResultSetMetaData rsmd = rs.getMetaData();
						//JSONObject obj = new JSONObject();
						//JSONArray rows = new JSONArray();
						/*while(rs.next()) {
							JSONObject rowObject = new JSONObject();
							for(int i = 0 ;i < rsmd.getColumnCount();i++) {
								rowObject.put(rsmd.getColumnName(i+1),rs.getString(i+1));
							}
							rows.add(rowObject);
						}
						obj.put("row",rows);*/
						
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