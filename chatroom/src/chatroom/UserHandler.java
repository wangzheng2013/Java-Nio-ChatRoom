package chatroom;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserHandler extends Thread{
	
	private String msg;
	//private User user;
	private SelectionKey key;
	private SocketChannel socketChannel;
	//private Lock lock = new ReentrantLock();
	
	public UserHandler(String msg, SelectionKey key, SocketChannel socketChannel) {
		this.msg = msg;
		//this.user = user;
		this.key = key;
		this.socketChannel = socketChannel;
	}
	@Override
	public void run() {
		
		// boolean isfind = false;
		 User m_user = null;
		 server.lockUser.readLock().lock();
		 for(User u: server.currentServerUser.values()) {
	    	 if(u.getKey().equals(key)) {
	    		 m_user = u;
	             //isfind = true;
	             break;
	    	 }
	     }
		 server.lockUser.readLock().unlock();
		 
	     String res = responseString(msg,m_user, key);
	     try {
             server.doWrite(socketChannel, res);
             }
             catch(IOException e) {
              	 e.printStackTrace();
             }
//	     
//	     if(isfind) {
//	    	 System.out.println("find");
//	    	 String res = responseString(msg,m_user, key);
//             System.out.println(res);
//             try {
//             server.doWrite(socketChannel, res);
//             }
//             catch(IOException e) {
//              	 e.printStackTrace();
//             }
//	     }
//	     else {
//	    	System.out.println("not find");
//	        String res = responseString(msg, null, key);
//		    System.out.println("res:" + res);
//		    try {
//		    	server.doWrite(socketChannel, res);
//	        }
//	        catch(IOException e) {
//	        	e.printStackTrace();
//	        }
//	     }
		 
	}
	
	private String login(String username, String password, SelectionKey m_key) {
		User u;
		server.lockUser.readLock().lock();
		u = server.wholeUser.get(username);
		server.lockUser.readLock().unlock();
		if(u != null) {
			if(u.getPassword().equals(password)) {
	    		 //判断这个账号之前是否已经登录过
	    		 //如果登录过就要踢掉
	    		 //否则没事
				server.lockUser.readLock().lock();
				User t = server.currentServerUser.get(username);
				if(t != null) {
					t.getKey().cancel();
					try {
						t.getKey().channel().close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				server.lockUser.readLock().unlock();
				
				u.setKey(m_key);
				//HashMap 保证用户一致
				server.lockUser.writeLock().lock();
				server.currentServerUser.put(username, u);
				server.lockUser.writeLock().unlock();
				return "login successfully";
			}
			else
				return "wrong password";
		}else {
			User new_user = new User();
				new_user.setName(username);
				new_user.setPassword(password);
				new_user.setKey(key);
				server.lockUser.writeLock().lock();
				server.wholeUser.put(username,new_user);
				server.currentServerUser.put(username,new_user);
				server.lockUser.writeLock().unlock();
				//TODO too many writing times
				//FileUtils.fileWriteHashMap(server.sysPathUser, server.wholeUser);
				return "registe successfully";
			}
	}
	
	private String createRoom(String roomname) {
		server.lockRoom.readLock().lock();
		Room r = server.wholeRoom.get(roomname);
		server.lockRoom.readLock().unlock();
		if(r != null) {
			return "room exists";
		}
		else {
			Room newRoom = new Room(roomname);
			newRoom.roomUser = new HashSet<>();
			//server.currentServerRoom.add(newRoom);
			server.lockRoom.writeLock().lock();
			server.wholeRoom.put(roomname,newRoom);
			server.lockRoom.writeLock().unlock();
			//TODO too much
			//FileUtils.fileWriteHashMap(server.sysPathRoom, server.wholeRoom);
			return "create room " + roomname + " successfully";
		}
	}
	
	private String enterRoom(String roomname, User m_user) {
		server.lockRoom.readLock().lock();
		Room r = server.wholeRoom.get(roomname);
		server.lockRoom.readLock().unlock();
		if(r != null) {
			server.lockUser.writeLock().lock();
			m_user.setRoomName(roomname);
			server.lockUser.writeLock().unlock();
			server.lockRoom.writeLock().lock();
			r.roomUser.add(m_user);
			server.lockRoom.writeLock().unlock();
			String res;
			res = "enter room " + roomname + " successfully";
			res += "\nroom message\n";
			for(Message m : r.mes) {
				res += m.getLine();
			}
			return res;
		}
		else {
			return  roomname + "not exist";
		}
	}
	
	private String exitRoom(User m_user) {
		server.lockRoom.readLock().lock();
		Room r = server.wholeRoom.get(m_user.getRoomName());
		server.lockRoom.readLock().unlock();
		if(r != null) {
			server.lockRoom.writeLock().lock();
			r.roomUser.remove(m_user);
			server.lockRoom.writeLock().unlock();
			server.lockUser.writeLock().lock();
			m_user.setRoomName("");
			server.lockUser.writeLock().unlock();
			
			return "exit successfully";
		}
		else {
			return "not in room";
		}
	}
	
	private String chatSingle(String to_username, User m_user, String message) {
		server.lockUser.readLock().lock();
		User u = server.currentServerUser.get(to_username);
		server.lockUser.readLock().unlock();
		if(u != null) {
			if(u.getKey() != null) {
				try {
					System.out.println(u.getKey().toString());
					server.doWrite((SocketChannel)(u.getKey().channel()), m_user.getName() + " said to you :" + message);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				return "@ send ok!";
			}
			else {
				return "send fail, user may offline";
			}
		}
		else {
			return "no user";
		}
	}
	
	private String chatAll(User m_user, String message) {
		server.lockRoom.readLock().lock();
		Room r = server.wholeRoom.get(m_user.getRoomName());
		server.lockRoom.readLock().unlock();
		String res = "send fail";
		if(r != null) {
			server.lockRoom.readLock().lock();
			for(User u : r.roomUser)
			{
				try {
					System.out.println(u.getKey().toString());
					server.doWrite((SocketChannel)(u.getKey().channel()), u.getName() + ":" + message);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			server.lockRoom.readLock().unlock();
			Message me = new Message();
			me.setSay(m_user.getName());
			me.setHear("");
			me.setInfo(message);
			server.lockRoom.writeLock().lock();
			r.mes.add(me);
			server.lockRoom.writeLock().unlock();
			//FileUtils.fileWriteHashMap(server.sysPathRoom, server.wholeRoom);
			res = "send ok!";
		}

		return res;
	}
	
	private String sendRP(String money, String number, User m_user, boolean pin) {
		server.lockRoom.readLock().lock();
		Room r = server.wholeRoom.get(m_user.getRoomName());
		server.lockRoom.readLock().unlock();
		if(r != null) {
			if(pin == false) {
				Redpocket redp = new Redpocket();
				
				server.lockRp.readLock().lock();
				int id = server.currentServerRedp.size() + 1;
				server.lockRp.readLock().unlock();
				
				redp.setID(id);
				redp.setSender(m_user.getName());
				redp.setMoneyTotal(Integer.parseInt(money));
				redp.setPeopleSize(Integer.parseInt(number));
				redp.setPeopleCurrent(0);
				redp.setRoomName(m_user.getRoomName());
				redp.dealRP();
				server.lockRp.writeLock().lock();
				server.currentServerRedp.add(redp);
				server.lockRp.writeLock().unlock();
				server.lockRoom.writeLock().lock();
				r.rd.add(id);
				server.lockRoom.writeLock().unlock();
				//
				server.lockRoom.readLock().lock();
				for(User u : r.roomUser)
				{
					try {
						if(u.getKey() != null)
						{
							System.out.println(u.getKey().toString());
							server.doWrite((SocketChannel)(u.getKey().channel()), "$rp " + id);
						}
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
				server.lockRoom.readLock().unlock();
				return "send red pocket ok!";
			}
			else {
				LuckyRed redp = new LuckyRed();
				
				server.lockRp.readLock().lock();
				int id = server.currentServerRedp.size() + 1;
				server.lockRp.readLock().lock();
				
				redp.setID(id);
				redp.setSender(m_user.getName());
				redp.setMoneyTotal(Integer.parseInt(money));
				redp.setPeopleSize(Integer.parseInt(number));
				redp.setPeopleCurrent(0);
				redp.setRoomName(m_user.getRoomName());
				redp.dealRP();
					
				server.lockRp.writeLock().lock();
				server.currentServerRedp.add(redp);
				server.lockRp.writeLock().unlock();
				server.lockRoom.writeLock().lock();
				r.rd.add(id);
				server.lockRoom.writeLock().unlock();
				//FileUtils.fileWriteHashMap(server.sysPathRoom, server.wholeRoom);
				//FileUtils.fileWriteList(server.sysPathRp, server.currentServerRedp);
				server.lockRoom.readLock().lock();
				for(User u : r.roomUser)
				{
					try {
						if(u.getKey() != null)
						{
							System.out.println(u.getKey().toString());
							server.doWrite((SocketChannel)(u.getKey().channel()), "$prp " + id);	
						}
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
				server.lockRoom.readLock().unlock();
				return "send pin red pocket ok!";
			}
		}
		else {
			return "not in room";
		}
	}
	
	private String sendQiang(User m_user, String rpId) {
		String res = "";
		Integer id = Integer.parseInt(rpId);
		System.out.println(id.toString());
		Redpocket rp;
		//不同人抢同一个红包
		server.lockRp.readLock().lock();
		rp = server.currentServerRedp.get(id-1);
		if(rp.getRoomName().equals(m_user.getRoomName()))
		{
			if(rp.getPeopleCurrent() < rp.getPeopleSize()) {
				res = qiangrp(m_user, rp);
				Room r;
				server.lockRoom.readLock().lock();
				r = server.wholeRoom.get(m_user.getRoomName());
				if(r != null) {
					for(User u : r.roomUser)
					{
						try {
							if(u.getKey() != null)
							{
								System.out.println(u.getKey().toString());
								server.doWrite((SocketChannel)(u.getKey().channel()), res);
							}
						}
						catch(IOException e) {
							e.printStackTrace();
						}
					}	
				}
				server.lockRoom.readLock().unlock();
				res = " ";
			}
			else {
				res = "no money in redpocket";
			}
		}	 
		server.lockRoom.readLock().unlock();
		return res;
	}
	

	private String responseString(String message, User user, SelectionKey key) {
		String tempString = "";
		String res = "";
		char[] ta = message.toCharArray();
		int type = (int)(ta[0]-'a');
		System.out.println("type" + type);
		System.out.println(message);
		int len,len2;
		String s1="",s2="";
		if(ta.length >= 1) {
			len = (int)ta[1];
			try {
				s1 = message.substring(2, 2+len);
			}
			catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			if(ta.length > (2+len)) {
				len2 = (int)ta[2+len];
				try {
					s2 = message.substring(3+len);
				}
				catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
//		int len = (int)ta[1];
//		System.out.println("length1:" + len);
//		int len2 = (int)ta[2+len];
//		System.out.println("length2:" + len2);
//		System.out.println(message.substring(2, 2+len));
//		System.out.println(message.substring(3+len));
		if(user == null) {
			//未登录
			if(type == 1) {
				res = login(s1,s2,key);
			}
		}
		else {
			//已登录
			switch(type) {
			case 1:
				res = "already login";
				break;
			case 2:
				res = createRoom(s1);
				break;
			case 3:
				res = enterRoom(s1,user);
				break;
			case 4:
				res = chatAll(user,s1);
				break;
			case 5:
				res = chatSingle(s1,user,s2);
				break;
			case 6:
				res = sendRP(s1,s2,user,false);
				break;
			case 7:
				res = sendRP(s1,s2,user,true);
				break;	
			case 8:
				res = sendQiang(user,s1);
				break;
			case 9:
				res = exitRoom(user);
				break;
			default:
				res = "wrong input, please retry";
				break;
			}
		}
//			else if(message.startsWith("$showAllRP")) {
//				res += "size:" + server.currentServerRedp.size() + "  ";
//				for(Redpocket rp : server.currentServerRedp) {
//					res += "id:"+ rp.getID() + "\n";
//					res += "roomName:" + rp.getRoomName() + "\n";
//					res += "MoneyTotal:" + rp.getMoneyTotal() + "\n";
//					res += "PeopleSize:" + rp.getPeopleSize() + "\n";
//					res += "PeopleCurrent:" + rp.getPeopleCurrent() + "\n";
//				}
//			}
//			else if(message.startsWith("$showAllRoom")) {
//				int i = 0;
//				for(Room u : server.wholeRoom.values()) {
//					res += "id:"+ i + u.getName() + "\n";
//					res += "PeopleNumber:" + u.roomUser.size() + "\n";
//					i = i + 1;
//				}
//			}
//			else if(message.startsWith("$showSRoomInfo")) {
//				String rn = user.getRoomName();
//				res += rn;
//				res += " ";
//				
//				Room r = server.wholeRoom.get(rn);
//				if(r != null) {
//					for(User u : r.roomUser)
//					{
//						res += u.getName() + "\n";
//					}
//					for(Integer I : r.rd)
//					{
//						res += I.toString() + "\n";
//					}
//					for(Message m : r.mes) {
//						res += m.getLine();
//					}
//				}
//			}
//			else if(message.startsWith("$showPInfo")) {
//				res += user.getName() + "\n";
//				res += user.getPassword() + "\n";
//				res += user.getRoomName() + "\n";
//				res += user.getBalance() + "\n";
//			}
//			else if(message.startsWith("$showAllUser")) {
//				int i = 0;
//				System.out.println(server.wholeUser.size());
//				try {
//					for(User u : server.wholeUser.values()) {
//						res += "id:"+ i + "\n";
//						res += u.getName() + "\n";
//						res += u.getPassword() + "\n";
//						i = i + 1;
//					}
//				}
//				catch(Exception e) {
//					e.printStackTrace();
//				}
//			}
//			else if(message.startsWith("$showACUser")) {
//				int i = 0;
//				for(User u : server.currentServerUser.values()) {
//					res += "id:"+ i + "\n";
//					res += u.getName() + "\n";
//					res += u.getPassword() + "\n";
//					i = i + 1;
//				}
//			}
		
		if(res == "") {
			res = "nothing";
		}
		
		return res;
	}
	
	private String qiangrp(User user, Redpocket rp) {
		Class<?> classrp = new Redpocket().getClass();
		//Class<?> classprp = new LuckyRed().getClass();
		String res = "";
		server.lockRp.writeLock().lock();
		if(rp.rec.get(user.getName()) == null) {
			if(rp.getClass().equals(classrp)) {
				System.out.println("rp");
				int inb = rp.moneyLeft.get(rp.getPeopleCurrent());
				int b;
				server.lockUser.writeLock().lock();
				b = user.getBalance() + inb;
				user.setBalance(b);
				server.lockUser.writeLock().unlock();

				rp.setPeopleCurrent(rp.getPeopleCurrent()+1);
				rp.rec.put(user.getName(), b);
				res += user.getName() + " get:" + inb;
			}else {
				System.out.println("prp");
				LuckyRed prp = (LuckyRed)rp; 
				int inb = prp.moneyLeft.get(prp.getPeopleCurrent());
				if(inb == prp.getMaxmoney()) {
					prp.setBest(user.getName());
				}
				int b;
				server.lockUser.writeLock().lock();
				b = user.getBalance() + inb;
				user.setBalance(b);
				server.lockUser.writeLock().unlock();
				
				prp.setPeopleCurrent(prp.getPeopleCurrent()+1);
				prp.rec.put(user.getName(), b);
				res += user.getName() + " get:" + inb;
				
				if(prp.getPeopleCurrent() == prp.getPeopleSize()) {
					res += "\nbest:" + user.getName(); 
				}
				
			}
				//FileUtils.fileWriteList(server.sysPathRp, server.currentServerRedp);
		}
		server.lockRp.writeLock().unlock();
		return res;
	}
	
}
