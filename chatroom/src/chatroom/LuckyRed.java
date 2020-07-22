package chatroom;

import java.util.ArrayList;
import java.util.List;

public class LuckyRed extends Redpocket{
	private String best = "";
	private double maxmoney = 0;
	public LuckyRed () {
		
	}
	
	public String getBest() {
		return best;
	}
	public void setBest(String best) {
		this.best = best;
	}

	public double getMaxmoney() {
		return maxmoney;
	}

	public void setMaxmoney(double maxmoney) {
		this.maxmoney = maxmoney;
	}
	
	@Override
	public void dealRP() {
		int peopleTotal = super.getPeopleSize();
		int size = peopleTotal;
		int money = super.getMoneyTotal();
		//System.out.println("size"+size);
		for(int i=0;i<peopleTotal-1;i++) {
			int newe = (int) ((money / (double)size) * (Math.random()+0.5));
			money = money - newe;
			size = size - 1;
			moneyLeft.add(newe);
			System.out.println(newe + " ");
			if(newe > maxmoney) {
				maxmoney = newe;
			}
		}
		moneyLeft.add(money);
		//System.out.println(money);
		if(money > maxmoney) {
			maxmoney = money;
		}
	}
}
