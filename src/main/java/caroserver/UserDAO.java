package caroserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends DAO {
	private Connection con;

	public UserDAO() {
		this.con = super.getInstance();
	}

	public boolean isExistedUsername(String username) {
		boolean result = false;
		try {
			String query = "SELECT * FROM client WHERE username = ?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if (rs.next() == true) {
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;

	}

	public boolean checkLogin(String username, String password) {
		boolean result = false;
		try {
			String query = "SELECT * FROM client WHERE username = ? AND password = ?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, username);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			if (rs.next() == true) {
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;

	}

	public boolean addUser(User user) {
		boolean result = false;
		try {
			String query = "INSERT INTO client (username,password,playername,highscore) VALUES (?,?,?,?)";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPassword());
			ps.setString(3, user.getName());
			ps.setInt(4, 0);
			int i = ps.executeUpdate();
			if (i != 0) {
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;

	}
}
