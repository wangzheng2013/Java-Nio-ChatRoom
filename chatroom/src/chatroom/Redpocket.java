package chatroom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Redpocket implements Serializable{
	private int ID;
	private String roomName;
	private String sender;
	//单位  分
	private int moneyTotal;
	private int PeopleSize;
	private int PeopleCurrent = 0;
	// key 是人  int 是金额  保留领红包顺序
	public LinkedHashMap<String,Integer> rec;
	//预分配钱数
	public List<Integer> moneyLeft;
	
	public Redpocket() {
		setRec(new LinkedHashMap<String,Integer>());
		moneyLeft = new ArrayList<>();
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public int getMoneyTotal() {
		return moneyTotal;
	}
	public void setMoneyTotal(int moneyTotal) {
		this.moneyTotal = moneyTotal;
	}
	public int getPeopleSize() {
		return PeopleSize;
	}
	public void setPeopleSize(int peopleSize) {
		PeopleSize = peopleSize;
	}
	public LinkedHashMap<String,Integer> getRec() {
		return rec;
	}
	public void setRec(LinkedHashMap<String,Integer> rec) {
		this.rec = rec;
	}
	public int getPeopleCurrent() {
		return PeopleCurrent;
	}
	public void setPeopleCurrent(int peopleCurrent) {
		PeopleCurrent = peopleCurrent;
	}
	
	public void dealRP() {
		moneyLeft = new ArrayList<>();
		//double money = super.getMoneyTotal();
		int size = PeopleSize;
		int money = moneyTotal;
		//System.out.println("size"+size);
		for(int i=0;i<PeopleSize-1;i++) {
			int newe = (int) ((double) money / (double)size);
			if(newe == 0)
				newe = 1;
			money = money - newe;
			size = size - 1;
			moneyLeft.add(newe);
			System.out.println(newe + " ");
		}
		moneyLeft.add(money);
	}
} 
