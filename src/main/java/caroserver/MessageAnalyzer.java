/*
 * CODE,
 * CODE NUA,
 * CODE MAI...
 */
package caroserver;


/**
 *
 * @author Son Vu
 */
public class MessageAnalyzer {
	public MessageAnalyzer () {
	}
	
	
	public String analyse(String msg) {
		String result = "none";
		if(msg.trim().equalsIgnoreCase("connect"))
			result = "accept";
		return result;
	}
	
}
