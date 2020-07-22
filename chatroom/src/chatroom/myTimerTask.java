package chatroom;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.TimerTask;

public class myTimerTask extends TimerTask{
	private byte flag = 0;
	public myTimerTask(byte flag) {
		this.flag = flag;
		
	}
	public byte getFlag() {
		return flag;
	}
	public void setFlag(byte flag) {
		this.flag = flag;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		server.Save(flag);
		//关闭无响应连接
		for(Map.Entry<SelectionKey, Connection> entry: server.MessageQueue.entrySet()) {
			if(entry.getValue().isIsalive() == true) {
				entry.getValue().setIsalive(false);
			}
			else {
				entry.getKey().cancel();
				try {
					entry.getKey().channel().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
