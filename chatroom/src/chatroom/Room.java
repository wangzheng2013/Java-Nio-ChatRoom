package chatroom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Room implements Serializable{
	private String name = "";
	public transient HashSet<User> roomUser;
	//public List<HashMap<String,HashMap<Integer,String>>> mes;
	public List<Message> mes;
	public List<Integer> rd;
	public Room(String name) {
		this.name = name;
		roomUser = new HashSet<User>();
		//System.out.println(roomUser.size());
		mes = new ArrayList<Message>();
		rd = new ArrayList<>();
	}
	public String getName() {
		return name;
	}
	
	@Override
    public int hashCode() {
        return name.hashCode();
    }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Room) {
			Room room2 = (Room) obj;
			return name.equals(room2.getName());
		}
		else {
	        return false;
		}
    }
}
