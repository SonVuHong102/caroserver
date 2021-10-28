package caroserver;

import java.sql.Connection;
import java.sql.DriverManager;

public class DAO {
	private static Connection con;

	public static Connection getInstance() {
		if(con == null) {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ltm?autoReconnect=true", "username1", "mydatabasepassword1");
				System.out.println("Connected to DB");
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return con;
	}
}
