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
import java.util.function.Predicate;
import java.util.stream.Collectors;
//import static org.elasticsearch.node.NodeBuilder.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;	
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.client.*;
import org.elasticsearch.transport.netty4.*;
import org.elasticsearch.transport.*;



public class DatabaseServlet extends HttpServlet{
	private static final String GET_TABLES = "/get_tables";
	private static final String GET_TABLE = "/get_table";
	private static final String NO_OF_RECORDS = "/no_of_records";
	private static final String GET_PAGE = "/get_page";
	private static JSONObject jsonobj = null;
	private static Connection conn;
	private static int lastInsertedRecordID = 0;
	static Client client;
	public static Client connect() {
		try {
			return new DatabaseServlet().elasticSearchTestNode().client();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Node elasticSearchTestNode() throws NodeValidationException {
		Node node = new MyNode(
				Settings.builder()
						.put("transport.type", "netty4")
						.put("http.type", "netty4")
						.put("http.enabled", "true")
						.put("cluster.name", "elasticsearch")
						.put("path.home", "elasticsearch-data")
						//.put("path.home", "C:\\Users\\ganesh-pt1936\\Downloads\\elasticsearch-6.2.3")
						.build(),
				Arrays.asList(Netty4Plugin.class));
		node.start();
		return node;
	}

	private static class MyNode extends Node {
		public MyNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
			super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
		}
	}
	
	static {
		client = DatabaseServlet.connect(); 
		Database db = new Database();
		lastInsertedRecordID = db.updateIndex(lastInsertedRecordID,client);
		System.out.println("done");
	}
	public void doGet(HttpServletRequest req,HttpServletResponse res)
	throws ServletException,IOException {
		doProcess(req,res);
	}

	public void doPost(HttpServletRequest req,HttpServletResponse res)
	throws ServletException,IOException  {
		doProcess(req,res);
	}

	public static void getConnection() {
		try {
			conn = DatabaseHelper.getConnection();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

    public void doProcess(HttpServletRequest req,HttpServletResponse res)
	throws ServletException,IOException {
		PrintWriter pw=res.getWriter();
		DatabaseServlet.getConnection();
        try {
			Connection conn = DatabaseHelper.getConnection();
			String path = req.getServletPath();
			res.setContentType("text/html");
			switch (path) {
				case NO_OF_RECORDS: {
					String table_name = req.getParameter("table_name");
					String sortProperties = req.getParameter("prop");
					String filterCol = req.getParameter("filterCol");
					String filterValue = req.getParameter("filterValue");
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
						ElasticClient elasticClient = new ElasticClient();
						System.out.println("let us print no of records");
						lastInsertedRecordID = new Database().updateIndex(lastInsertedRecordID,client);
						pw.println(elasticClient.getTotalNumberOfRecords(idList,filterCol,filterValue,client));
						System.out.println("printed no of records");
						/*Database db = new Database();
						if(jsonobj == null)
							jsonobj = db.getTableJson(idList);
						//System.out.println(jsonobj.toJSONString());
						JSONArray cols = (JSONArray)jsonobj.get("col");
						boolean is_col = false;
						for(int i = 0;i < cols.size();i++)
							if(String.valueOf(cols.get(i)).equalsIgnoreCase(filterCol))
								is_col = true;
						if(sortProperties == null)
							sortProperties = new String(String.valueOf(cols.get(0)));
						JSONArray rows = (JSONArray)jsonobj.get("row");
						DisplayHelper displayHelper= new DisplayHelper();
						ArrayList<JSONObject> sortedJsonRows = displayHelper.sort(rows,sortProperties);
						List<JSONObject> filteredList = sortedJsonRows;
						if(filterCol != null && filterValue != null && is_col)
							filteredList = displayHelper.filter(sortedJsonRows,filterCol,filterValue);
						System.out.println(filteredList.size());
						getServletContext().log(String.valueOf(filteredList.size()));
						pw.println(filteredList.size());*/
					}
					else {
						pw.println("");
					}
				}
				break;
				case GET_PAGE: {
					String table_name = req.getParameter("table_name");
					String sortProperties = req.getParameter("prop");
					Boolean isAscending = Boolean.parseBoolean(req.getParameter("isAscending"));
					String filterCol = req.getParameter("filterCol");
					String filterValue = req.getParameter("filterValue");
					int start = Integer.parseInt(req.getParameter("start"));
					int stop = Integer.parseInt(req.getParameter("stop"));
					int paginateBy = stop - start;
					System.out.println(isAscending);
					System.out.println("getting page");
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
						ElasticClient elasticClient = new ElasticClient();
						lastInsertedRecordID = new Database().updateIndex(lastInsertedRecordID,client);
						pw.println(elasticClient.search(idList,filterCol,filterValue,sortProperties,isAscending,paginateBy,start,client));
						System.out.println("got page");
						/*Database db = new Database();

						int start = Integer.parseInt(req.getParameter("start"));
						System.out.println(start);
						int stop = Integer.parseInt(req.getParameter("stop"));
						System.out.println(stop);
						if(jsonobj == null)
							jsonobj = db.getTableJson(idList);
						JSONArray cols = (JSONArray)jsonobj.get("col");
						boolean is_col = false;
						for(int i = 0;i < cols.size();i++)
							if(String.valueOf(cols.get(i)).equalsIgnoreCase(filterCol))
								is_col = true;
						if(sortProperties == null)
							sortProperties = new String(String.valueOf(cols.get(0)));
						JSONArray rows = (JSONArray)jsonobj.get("row");
						DisplayHelper displayHelper= new DisplayHelper();
						ArrayList<JSONObject> sortedJsonRows = displayHelper.sort(rows,sortProperties);
						List<JSONObject> filteredList = sortedJsonRows;
						if(filterCol != null && filterValue != null && is_col)
							filteredList = displayHelper.filter(sortedJsonRows,filterCol,filterValue);
						JSONObject pagedObject = new JSONObject();
						JSONArray pagedRows = new JSONArray();
						pagedRows = displayHelper.paginate(filteredList,start,stop);
						pagedObject.put("col",cols);
						pagedObject.put("row",pagedRows);
						pw.println(pagedObject);*/
					}
					else {
						pw.println("");
					}
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
}