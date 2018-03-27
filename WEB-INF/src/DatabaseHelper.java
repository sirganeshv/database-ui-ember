import java.sql.*; 

public class DatabaseHelper {
	
	private static Connection connection;
	
	private DatabaseHelper() {
	}
	
	public static Connection getConnection() throws Exception {
		if(connection == null) {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/banking";
			return DriverManager.getConnection(url, "root", "");
		}
		else
			return connection;
	}
}