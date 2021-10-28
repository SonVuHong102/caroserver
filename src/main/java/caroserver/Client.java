package caroserver;

public class Client {
	private String username;
	private String password;
	private int highscore;
	
	public Client(String username, String password, int highscore) {
		super();
		this.username = username;
		this.password = password;
		this.highscore = highscore;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getHighscore() {
		return highscore;
	}
	
	
	
}
