package caroserver;

public class User {
	private String username;
	private String password;
	private String name;
	private int highscore;
	
	public User(String username, String password, String name, int highscore) {
		super();
		this.username = username;
		this.password = password;
		this.name = name;
		this.highscore = highscore;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public int getHighscore() {
		return highscore;
	}
	
	
	
}
