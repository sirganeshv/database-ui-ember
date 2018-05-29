import java.sql.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/*import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.*;*/
import net.minidev.json.*; 
import net.minidev.json.parser.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
//import static org.elasticsearch.node.NodeBuilder.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;	
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.client.Client;
//import org.elasticsearch.transport.netty4.*;
//import org.elasticsearch.transport.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JsonDataSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.text.SimpleDateFormat;



public class DatabaseServlet extends HttpServlet{
	private static final String GET_TABLES = "/get_tables";
	private static final String GET_TABLE = "/get_table";
	private static final String NO_OF_RECORDS = "/no_of_records";
	private static final String GET_PAGE = "/get_page";
	private static final String EXPORT_PDF = "/exportPdf";
	private static final String EXPORT_EMAIL = "/exportEmail";
	private static final String GET_PROGRESS = "/getProgress";
	private static final String DELETE = "/delete";
	private static JSONObject jsonobj = null;
	private static Connection conn;
	private static int lastInsertedRecordID = 0;
	static Node node;
	private static Connection postgresConnection;
	//private static Timer timer = new Timer();
	/*public static Client connect() {
		try {
			return new DatabaseServlet().elasticSearchTestNode().client();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}*/
	
	public static Node getNode() throws NodeValidationException {
		/*Arrays.asList(Netty4Plugin.class));*/
		node = new Node(
			Settings.builder()
			.put("transport.type", "netty4")
			.put("http.type", "netty4")
			//.put("http.enabled", "false")
			.put("cluster.name", "elasticsearch")
			//.put("path.home", "elasticsearch-data")
			.put("path.home", "C:\\Users\\ganesh-pt1936\\Downloads\\elasticsearch-6.2.3\\")
			.build());
		node.start();
		return node;
	}

	/*private static class MyNode extends Node {
		public MyNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
			super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
		}
	}*/
	
	static {
		try {
			node = DatabaseServlet.getNode(); 
		}
		catch(NodeValidationException e) {
			e.printStackTrace();
		}
		Database db = new Database();
		lastInsertedRecordID = db.updateIndex(lastInsertedRecordID,node);
		System.out.println("done");
		try {
			Class.forName("org.postgresql.Driver");
			postgresConnection	= DriverManager.getConnection("jdbc:postgresql://localhost:5432/schedule","postgres", "hello");
			Statement postgresStatement = postgresConnection.createStatement();
			ResultSet resultSet = postgresStatement.executeQuery("select * from schedules");
			ElasticClient elasticClient = new ElasticClient();
			Connection conn = DatabaseHelper.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select count(*) from id");
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
			JSONArray events = (JSONArray)(elasticClient.searchEvents(idList,null,null,null,true,0,5,node)).get("row");
			while ( resultSet.next() ) {
				String frequency = resultSet.getString("frequency");
				Calendar calendar = Calendar.getInstance();
				if(frequency.equalsIgnoreCase("hourly")) {
					int currentMinute = calendar.get(Calendar.MINUTE);
					long time = Long.parseLong(resultSet.getString("timestamp"));
					Calendar newCalendar = Calendar.getInstance();
					newCalendar.setTimeInMillis(time);
					int minute = newCalendar.get(Calendar.MINUTE);
					int delayMinutes = minute - currentMinute;
					if(delayMinutes < 0)
						delayMinutes += 60;
					System.out.println("The delayMinutes is "+delayMinutes);
					String receiverMailID = resultSet.getString("mailid");
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							new Export().exportEmail(events.toJSONString(),receiverMailID);
						}
					}, delayMinutes*60*1000, 60*60*1000);
				}
				if(frequency.equalsIgnoreCase("daily")) {
					int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
					int currentMinute = calendar.get(Calendar.MINUTE);
					long time = Long.parseLong(resultSet.getString("timestamp"));
					Calendar newCalendar = Calendar.getInstance();
					newCalendar.setTimeInMillis(time);
					int minute = newCalendar.get(Calendar.MINUTE);
					int hour = newCalendar.get(Calendar.HOUR_OF_DAY);
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.MINUTE,minute);
					cal.set(Calendar.HOUR_OF_DAY,hour);
					long delay = cal.getTimeInMillis() - calendar.getTimeInMillis();
					if(delay < 0)
						delay += 24*3600*1000;
					System.out.println("The delay is "+delay);
					String receiverMailID = resultSet.getString("mailid");
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							new Export().exportEmail(events.toJSONString(),receiverMailID);
						}
					}, delay, 24*60*60*1000);
				}
				if(frequency.equalsIgnoreCase("weekly")) {
					int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
					int currentMinute = calendar.get(Calendar.MINUTE);
					int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
					long time = Long.parseLong(resultSet.getString("timestamp"));
					Calendar newCalendar = Calendar.getInstance();
					newCalendar.setTimeInMillis(time);
					int minute = newCalendar.get(Calendar.MINUTE);
					int hour = newCalendar.get(Calendar.HOUR_OF_DAY);
					int day = newCalendar.get(Calendar.DAY_OF_WEEK);
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.MINUTE,minute);
					cal.set(Calendar.HOUR_OF_DAY,hour);
					cal.set(Calendar.DAY_OF_WEEK,day);
					long delay = cal.getTimeInMillis() - calendar.getTimeInMillis();
					if(delay < 0)
						delay += 7*24*3600*1000;
					System.out.println("The delay is "+delay);
					String receiverMailID = resultSet.getString("mailid");
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							new Export().exportEmail(events.toJSONString(),receiverMailID);
						}
					}, delay, 7*24*60*60*1000);
				}
				if(frequency.equalsIgnoreCase("monthly")) {
					int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
					int currentMinute = calendar.get(Calendar.MINUTE);
					int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
					long time = Long.parseLong(resultSet.getString("timestamp"));
					Calendar newCalendar = Calendar.getInstance();
					newCalendar.setTimeInMillis(time);
					int minute = newCalendar.get(Calendar.MINUTE);
					int hour = newCalendar.get(Calendar.HOUR_OF_DAY);
					int day = newCalendar.get(Calendar.DAY_OF_MONTH);
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.MINUTE,minute);
					cal.set(Calendar.HOUR_OF_DAY,hour);
					cal.set(Calendar.DAY_OF_MONTH,day);
					long delay = cal.getTimeInMillis() - calendar.getTimeInMillis();
					if(delay < 0)
						delay += (long)cal.getActualMaximum(Calendar.DAY_OF_MONTH)*24*3600*1000;
					System.out.println("The delay is "+delay);
					String receiverMailID = resultSet.getString("mailid");
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							new Export().exportEmail(events.toJSONString(),receiverMailID);
						}
					}, delay, (long)30*24*60*60*1000);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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
						lastInsertedRecordID = new Database().updateIndex(lastInsertedRecordID,node);
						pw.println(elasticClient.getTotalNumberOfRecords(idList,filterCol,filterValue,node));
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
				case EXPORT_PDF: {
					System.out.println("Export entered");
					String table_name = req.getParameter("table_name");
					String sortProperties = req.getParameter("prop");
					Boolean isAscending = Boolean.parseBoolean(req.getParameter("isAscending"));
					String filterCol = req.getParameter("filterCol");
					String filterValue = req.getParameter("filterValue");
					System.out.println("Before start");
					int start = Integer.parseInt(req.getParameter("start"));
					int stop = Integer.parseInt(req.getParameter("stop"));
					pw.println(exportPDF(table_name,sortProperties,isAscending,filterCol,filterValue,start,stop));
				}
				break;
				case EXPORT_EMAIL: {
					System.out.println("Export entered");
					String table_name = req.getParameter("table_name");
					//String sortProperties = req.getParameter("prop");
					//Boolean isAscending = Boolean.parseBoolean(req.getParameter("isAscending"));
					//String filterCol = req.getParameter("filterCol");
					//String filterValue = req.getParameter("filterValue");
					System.out.println("Before start");
					int start = Integer.parseInt(req.getParameter("start"));
					int stop = Integer.parseInt(req.getParameter("stop"));
					//int minute = Integer.parseInt(req.getParameter("minute"));
					//exportPDF("id",null,true,null,null,start,stop);
					String receiverMailID = req.getParameter("receiverMailID");
					String frequency = req.getParameter("frequency");
					//System.out.println(isAscending);
					System.out.println("getting page");
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("select count(*) from id");
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
					//lastInsertedRecordID = new Database().updateIndex(lastInsertedRecordID,node);
					//JSONArray events = (JSONArray)(elasticClient.searchEvents(idList,filterCol,filterValue,sortProperties,isAscending,paginateBy,start,stop,node)).get("row");
					System.out.println("fetched");
					//Export export = new Export();
					/*if(events == null) {
						pw.println("No data to send");
						break;
					}*/
					//java.util.Date date = new java.util.Date();
					Calendar calendar = Calendar.getInstance();
					long time = calendar.getTimeInMillis();
					Statement postgresStatement = null;
					postgresStatement = postgresConnection.createStatement();
					System.out.println("let us insert");
					if(frequency.equalsIgnoreCase("hourly")) {
						//postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"+calendar.get(calendar.MINUTE)+"','"+receiverMailID+"','hourly')");
						postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"+time+"','"+receiverMailID+"','hourly')");
						
						System.out.println("inserted");
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								ElasticClient elasticClient = new ElasticClient();
								JSONArray events = (JSONArray)(elasticClient.searchEvents(idList,null,null,null,true,start,stop,node)).get("row");
								Export export = new Export();
								export.exportEmail(events.toJSONString(),receiverMailID);
							}
						}, 0, 60*60*1000);
						System.out.println("Mail sent");
						pw.println("Email sent to "+receiverMailID);
					}
					else if(frequency.equalsIgnoreCase("daily")) {
						//postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"+calendar.get(calendar.HOUR_OF_DAY)+":"+calendar.get(calendar.MINUTE)+"','"+receiverMailID+"','weekly')");
						postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"+time+"','"+receiverMailID+"','daily')");
						System.out.println("inserted");
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								ElasticClient elasticClient = new ElasticClient();
								JSONArray events = (JSONArray)(elasticClient.searchEvents(idList,null,null,null,true,start,stop,node)).get("row");
								Export export = new Export();
								export.exportEmail(events.toJSONString(),receiverMailID);
							}
						}, 0, 24*60*60*1000);
						System.out.println("Mail sent");
						pw.println("Email sent to "+receiverMailID);
					}
					else if(frequency.equalsIgnoreCase("weekly")) {
						//postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"+calendar.get(calendar.DAY_OF_WEEK)+"','"+receiverMailID+"','weekly')");
						postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"+time+"','"+receiverMailID+"','weekly')");
						System.out.println("inserted");
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								ElasticClient elasticClient = new ElasticClient();
								JSONArray events = (JSONArray)(elasticClient.searchEvents(idList,null,null,null,true,start,stop,node)).get("row");
								Export export = new Export();
								export.exportEmail(events.toJSONString(),receiverMailID);
							}
						}, 0, 7*24*60*60*1000);
						System.out.println("Mail sent");
						pw.println("Email sent to "+receiverMailID);
					}
					else if(frequency.equalsIgnoreCase("monthly")) {
						//postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"calendar.get(calendar.MONTH)+":"+calendar.get(calendar.HOUR_OF_DAY)+":"+calendar.get(calendar.MINUTE)+"','"+receiverMailID+"','monthly')");
						postgresStatement.executeUpdate("insert into schedules(timestamp,mailid,frequency) values('"+time+"','"+receiverMailID+"','monthly')");
						System.out.println("inserted");
						Timer timer = new Timer();
						/*Calendar nextTime = Calendar.getInstance();
						nextTime.set(Calendar.MONTH,(Calendar.MONTH+1)%12);
						if(Calendar.MONTH == 0)
							calendar.set(Calendar.YEAR,Calendar.YEAR+1);*/
						long period = (long)30*24*60*60*1000;
						System.out.println(period);
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								ElasticClient elasticClient = new ElasticClient();
								JSONArray events = (JSONArray)(elasticClient.searchEvents(idList,null,null,null,true,start,stop,node)).get("row");
								Export export = new Export();
								export.exportEmail(events.toJSONString(),receiverMailID);
							}
						}, 0, period);
						System.out.println("Mail sent");
						pw.println("Email sent to "+receiverMailID);
					}
				}
				break;
				case GET_PROGRESS: {
					pw.println(new Export().getProgress());
					//System.out.println("The progress is "+new Export().getProgress());
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
						//lastInsertedRecordID = new Database().updateIndex(lastInsertedRecordID,node);
						pw.println(elasticClient.search(idList,filterCol,filterValue,sortProperties,isAscending,paginateBy,start,node));
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
				case DELETE: {
					String table_name = req.getParameter("table_name");
					int eventID = Integer.parseInt(req.getParameter("eventID"));
					if(table_name != null) {
						int idList[] = {eventID};
						Statement stmt = conn.createStatement();
						stmt.executeUpdate("delete from "+table_name+" where acc_no = "+eventID);
						ElasticClient elasticClient = new ElasticClient();
						pw.println(elasticClient.searchEvents(idList,null,null,null,true,eventID,eventID,node));
						elasticClient.delete(eventID,node);
						//pw.println(elasticClient.search(idList,filterCol,filterValue,sortProperties,isAscending,paginateBy,start,node));
						//System.out.println("got page");
					}
				}
				break;
			}
        }
		catch(SQLException e) {
			e.printStackTrace();

        }
        catch(Exception e){
            e.printStackTrace();
        }
		finally {
			pw.close();
		}
    }
	
	String exportPDF(String table_name,String sortProperties,boolean isAscending,String filterCol,String filterValue,int start,int stop) {
		int paginateBy = stop - start;
		System.out.println(isAscending);
		System.out.println("getting page");
		try {
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
				//lastInsertedRecordID = new Database().updateIndex(lastInsertedRecordID,node);
				JSONArray events = (JSONArray)(elasticClient.searchEvents(idList,filterCol,filterValue,sortProperties,isAscending,start,stop,node)).get("row");
				System.out.println("fetched");
				Export export = new Export();
				if(events == null) {
					return "No data to export";
				}
				export.exportToPdf(events.toJSONString());
				return "Exported successfully at D:\\";
			}
			else {
				return "Failed";
			}
		}
		catch(SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}