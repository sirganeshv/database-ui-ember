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
 
public class GetTablesServlet extends HttpServlet{
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
			ResultSet rs = dbm.getTables(null, null, "%", null);
			JSONArray tables = new JSONArray();
			while (rs.next()) {
				tables.add(rs.getString(3));
				System.out.println(rs.getString(3));
			}
			pw.println(tables);
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