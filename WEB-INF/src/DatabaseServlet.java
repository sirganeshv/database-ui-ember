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
        try{  
			String table_name = req.getParameter("table_name");
            Connection conn = getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, table_name, null);
			if (tables.next()) {
				Statement stmt = conn.createStatement(); 
				ResultSet rs = stmt.executeQuery("select * from "+table_name);
				ResultSetMetaData rsmd = rs.getMetaData();              
				System.out.println();
				JSONObject obj = new JSONObject();
				JSONArray col = new JSONArray();
				for(int i = 0;i < rsmd.getColumnCount();i++ ) {
					System.out.print(rsmd.getColumnName(i+1)+"\t");
					col.add(i, rsmd.getColumnName(i+1));
				}
				obj.put("col",col);
				System.out.println();
				int j = 0;
				JSONArray rows = new JSONArray();
				while(rs.next()) {
					//JSONArray rowi = new JSONArray();
					JSONObject rowObject = new JSONObject();
					for(int i = 0 ;i < rsmd.getColumnCount();i++) {
						rowObject.put(rsmd.getColumnName(i+1),rs.getString(i+1));
						//rowi.add(rs.getString(i+1));
						System.out.print(rs.getString(i+1)+"\t");
					}
					rows.add(rowObject);
					j++;
					System.out.println();
				}
				obj.put("row",rows);
				pw.println(obj);
			}
			else
				pw.println("false");
            conn.close();
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