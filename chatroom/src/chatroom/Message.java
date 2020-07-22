package chatroom;

import java.io.Serializable;

public class Message implements Serializable{
	private String say = "";
	// hear = "" means all
	private String hear = "";
	private String info = "";
	
	public String getSay() {
		return say;
	}
	public void setSay(String say) {
		this.say = say;
	}
	public String getHear() {
		return hear;
	}
	public void setHear(String hear) {
		this.hear = hear;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
	public String getLine() {
		return say + ":" + info + "\n";
	}
}
