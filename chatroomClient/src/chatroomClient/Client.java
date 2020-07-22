package chatroomClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Client extends Thread{
	//private static final String ip = "127.0.0.1";
	private static final int port = 4406;
	private static String userName = "";
	private static String userPassword = "";
	
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop = false;
	
	public Client() {
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public SocketChannel getSocketChannel() {
		return this.socketChannel;
	}
	
	@Override
	public void run() {
		try {
			if(socketChannel.connect(new InetSocketAddress("localhost",port))) {
				//System.out.println("op_read");
				socketChannel.register(selector,SelectionKey.OP_READ);
				//doWrite(socketChannel);
		    }
			else{
				//注册到多路复用器上，监听连接事件
				socketChannel.register(selector,SelectionKey.OP_CONNECT);
		   }
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("client start");
	    while (!stop){
	    	try{
            	//System.out.println("select");
	    		selector.select();
	            Set<SelectionKey> selectionKeys = selector.selectedKeys();
	            Iterator<SelectionKey> iterator = selectionKeys.iterator();
	            SelectionKey key;
	            while (iterator.hasNext()){
	            	key = iterator.next();
	                iterator.remove();
	                try{
	                	//System.out.println("handinput");
	                	handInput(key);
	                }catch (Exception e){
	                	key.cancel();
	                    if (key.channel() != null)
	                    	key.channel().close();
	                    }
	                }

	        }catch (Exception e){
	        	e.printStackTrace();
	            System.exit(-1);
	        }
	    }

	    if (selector != null) {
	    	try {
	    		System.out.println("selector.close()");
	    		selector.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	private void handInput(SelectionKey key) throws IOException {
        if (key.isValid()){
            SocketChannel channel = (SocketChannel)key.channel();
            //判断是否连接成功
            if (key.isConnectable()){
            	//System.out.println("key_is_connectable");
                //完成了连接
                if (channel.finishConnect()){
    				//System.out.println("finish connect");
                    channel.register(selector,SelectionKey.OP_READ /*| SelectionKey.OP_WRITE*/);
                    //doWrite(socketChannel);
                }else {
                    System.exit(-1);
                }
            }

            //判断是否可读状态
            if (key.isReadable()){
            	//System.out.println("is_readable");
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readbytes = channel.read(readBuffer);
                if (readbytes > 0){
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String msg = new String(bytes, "UTF-8");
                    //System.out.println("client get msg:"+msg);
                    System.out.println(msg);
                    //this.stop = true ;
                }else if (readbytes < 0){
                	System.out.println("与服务器断开了连接！");
                    key.cancel();
                    channel.close();
                }
            }
            
//            if(key.isWritable()) {
//            	//System.out.println("is_writable");
//            	if(i.isGet) {
//                	doWrite(socketChannel);
//            	}
//            }
        }
    }

	public void doWrite(SocketChannel socketChannel, String mm) throws IOException {
        byte[] bytes = mm.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        try {
            socketChannel.write(writeBuffer);
        }
        catch(ClosedChannelException e) {
        	socketChannel.close();
        	System.out.println("channel closed!");
        	stop = true;
        }

        //i.isGet = false;
    }
	//1len 2 type 3canshu1 4canshu2
	
	public static String checkInput(String input) {
		//1 login 2 create 3 enter 4 chatall 5 chat single
		//6 hongbao 7 showAllRP 8 AllRoom 9 SRoomInfo 10 AllUser 11 ACUser
		String pattern1 = "^\\$login\\s(\\S{1,})\\s(\\S{1,})$";
		String pattern2 = "^\\$(create|enter)\\s(\\S{1,})$";
		String pattern3 = "^\\$(\\S{1,})$";
		String pattern4 = "^\\$@(\\S{1,})\\s(\\S{1,})$";
		String pattern5 = "^\\$hongbao\\s(\\d{1,}),(\\d{1,})(,pin){0,1}$";
		String pattern6 = "^\\$qiang\\s(\\d{1,})$";
		String pattern7 = "^\\$exit$";
		//String pattern7 = "^\\$show(RP|Room|SRoom|Person|User)\\s(info)$";
		//boolean isMatch = Pattern.matches(pattern1, input);
		//System.out.println(isMatch);
//		Pattern loginPattern = Pattern.compile(pattern1);
//		Matcher m = loginPattern.matcher(input);
//		if (m.find( )) {
//	        System.out.println("Found value: " + m.group(0) );
//	        System.out.println("Found value: " + m.group(1) );
//	        System.out.println("Found value: " + m.group(2) );
//	        System.out.println("Found value: " + m.groupCount() );
//	        int type = 1 + 'a';
//	        int len1 = m.group(1).length();
//	        System.out.println(len1);
//	        int len2 = m.group(2).length();
//	        System.out.println(len2);
//	        String send =  "" + (char)type + (char)len1 + m.group(1) + (char)len2 + m.group(2);
//	        System.out.println(send);
//	        //return send;
//	    } else {
//	    	System.out.println("NO MATCH");
//	    }
		
		Pattern loginPattern = Pattern.compile(pattern1);
		Pattern createEnterPattern = Pattern.compile(pattern2);
		Pattern chatAllPattern = Pattern.compile(pattern3);
		Pattern chatSinglePattern = Pattern.compile(pattern4);
		Pattern hongbaoPattern = Pattern.compile(pattern5);
		Pattern qiangPattern = Pattern.compile(pattern6);
		Pattern exitPattern = Pattern.compile(pattern7);
		
		String send = "";

		int type = 0;
		//
		Matcher m1 = loginPattern.matcher(input);
		if (m1.find()) {
			type = 1 + 'a';
	        int len1 = m1.group(1).length();
	        int len2 = m1.group(2).length();
	        send =  "" + (char)type + (char)len1 + m1.group(1) + (char)len2 + m1.group(2);
	        return send;
	    } 
		//
		Matcher m2 = createEnterPattern.matcher(input);
		if(m2.find()) {
	    	if(m2.group(1).equals("create")) {
		    	type = 2 + 'a';
	    	}
	    	else{
	    		type = 3 + 'a';
	    	}
		    int len2 = m2.group(2).length();
		    send =  "" + (char)type + (char)len2 + m2.group(2);
		    return send;
    	}
		Matcher me = exitPattern.matcher(input);
		if(me.find()) {
	    	type = 9 + 'a';
		    send =  "" + (char)type;
		    return send;
    	}
		//
		Matcher m3 = chatAllPattern.matcher(input);
		if(m3.find()) {
			type = 4 + 'a';
		    int len1 = m3.group(1).length();
		    send =  "" + (char)type + (char)len1 + m3.group(1);
		    return send;
		}
		Matcher m4 = chatSinglePattern.matcher(input);
		if(m4.find()) {
			type = 5 + 'a';
	        int len1 = m4.group(1).length();
	        int len2 = m4.group(2).length();
	        send =  "" + (char)type + (char)len1 + m4.group(1) + (char)len2 + m4.group(2);
	        return send;
		}
		Matcher m5 = hongbaoPattern.matcher(input);
		if(m5.find()) {
//			System.out.println("Found value: " + m5.group(3) );
//	        System.out.println("Found value: " + m5.group(1) );
//	        System.out.println("Found value: " + m5.group(2) );
//	        System.out.println("Found value: " + m5.groupCount() );
			if(m5.group(3) == null) {
	    		type = 6 + 'a';
	    	}else {
	    		type = 7 + 'a';
	    	}
	        int len1 = m5.group(1).length();
	        int len2 = m5.group(2).length();
	        send =  "" + (char)type + (char)len1 + m5.group(1) + (char)len2 + m5.group(2);
	        return send;
		}
		Matcher m6 = qiangPattern.matcher(input); 
		if(m6.find()) {
	    	type = 8 + 'a';
	        int len1 = m6.group(1).length();
	        send =  "" + (char)type + (char)len1 + m6.group(1);
	        return send;
		}
		System.out.println("NO MATCH");
//	    } else if(sysPattern.matcher(input).find()){
//	    	m = sysPattern.matcher(input);
//	    	if()
//	    	type = 8 + 'a';
//	        int len1 = m.group(1).length();
//	        send =  "" + (char)type;
//	    } 
		return "";
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		client.start();
		while(true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
				String buffer = br.readLine();
				String s = checkInput(buffer);
				if(!s.equals("")) {
					int len = s.length();
					s = "" + (char)len + s;
					client.doWrite(client.socketChannel,s);
				    System.out.println(s);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
