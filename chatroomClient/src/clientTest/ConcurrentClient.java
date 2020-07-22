package clientTest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import chatroomClient.Client;

public class ConcurrentClient {
	String[] ts = {"wang","zhang","xiu","ka"};
	String[] tp = {"12345678","abcdefghijk","43343df"};
	String[] ltp = {"1234567888888888888888888888888888888","abcdefghijkkkkkkkkkkkkkkkkkkkkkkkkk","43343dffffffffffffffffffffffffffffffff"};
	private class T1 extends Thread{
		Client client;
		public T1(Client client) {
			this.client = client;
		}
		@Override
		public void run() {
			String s1 = "$login "+ ts[(int)(Math.random()*4)] + " " + ltp[(int)(Math.random()*3)];
			String input = Client.checkInput(s1);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			try {
				client.doWrite(client.getSocketChannel(),input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		ConcurrentClient t = new ConcurrentClient();
		Client client1 = new Client();
		//Client client2 = new Client();
		//Client client3 = new Client();
		client1.start();
		//client2.start();
		//client3.start();
		while(true) {
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			T1 t1 = t.new T1(client1);
			//T1 t2 = t.new T1(client2);
			//T1 t3 = t.new T1(client3);
			t1.start();
			//t2.start();
			//t3.start();
		}
	}
}
