package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import chatroom.User;

public class testJ {
	//static HashSet<User> ServerUser;
	static HashMap<String,String> serverUser;
	public static void main(String[] args) {
		//ServerUser = new HashSet<>();
		//ServerUser.
		serverUser = new HashMap<>();
		//User newuser = new User();
		//newuser.setName("bb");
		serverUser.put("wang", "abb");
		serverUser.remove("wang");
		String u = serverUser.get("wang");
		//serverUser.put("wang", u + "acccd");
		//u = serverUser.get("wang");
		System.out.println(u);
		//User u2 = serverUser.get("waf");
		//System.out.println("2" + u2);
	}
}
