package caroserver;

public class Player {
	private int id = -1;
	private String username = "";
	
	public Player(String username) {
		this.username = username;
	}
	
	public Player(int id,String username) {
		this.id = id;
		this.username = username;
	}
	
	public int getId() {
		return id;
	}
	
	public String getUsername() {
		return username;
	}
 	
}
