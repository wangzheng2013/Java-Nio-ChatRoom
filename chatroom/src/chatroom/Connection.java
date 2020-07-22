package chatroom;

public class Connection {
	private String msg;
	private int length;
	private boolean isalive;
	public Connection(String msg, int length) {
		this.msg = msg;
		this.length = length;
		this.isalive = true;
	}
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public boolean isIsalive() {
		return isalive;
	}
	public void setIsalive(boolean isalive) {
		this.isalive = isalive;
	}
}
