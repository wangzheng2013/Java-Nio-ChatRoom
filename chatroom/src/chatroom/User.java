package chatroom;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.channels.SelectionKey;

public class User implements Serializable {
	private String name = "";
	private String password = "";
	//private Socket socket = null;
	private transient SelectionKey key = null;
	// 0 未登录  //1 已登录
	//private int status = 0;
	//单位 分
	private int balance = 0;
	private String roomName =  "";
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public SelectionKey getKey() {
		return key;
	}
	public void setKey(SelectionKey key) {
		this.key = key;
	}
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	@Override
    public int hashCode() {
        return name.hashCode();
    }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof User) {
			User user2 = (User) obj;
			return name.equals(user2.getName());
		}
		else {
	        return false;
		}
    }
}
