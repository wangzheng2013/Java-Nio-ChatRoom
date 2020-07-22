package chatroom;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class server extends Thread{
	private static int port = 4406;
	private static int maxConn = 1024;
	public static final String sysPathUser = "e:/user.txt";
	public static final String sysPathRoom = "e:/room.txt";
	public static final String sysPathRp = "e:/redpocket.txt";
	//current user list
	static HashMap<String, User> currentServerUser;
	//current room list
	//static List<Room> currentServerRoom;
	static List<Redpocket> currentServerRedp;
	static HashMap<SelectionKey,Connection> MessageQueue;
	//static HashMap<SelectionKey,Integer> MessageLength;
	//db
	static HashMap<String, User> wholeUser;
	static HashMap<String, Room> wholeRoom;
	
	//lock
	public static ReentrantReadWriteLock lockUser = new ReentrantReadWriteLock();
	public static ReentrantReadWriteLock lockRoom = new ReentrantReadWriteLock();
	public static ReentrantReadWriteLock lockRp = new ReentrantReadWriteLock();
	
	private Selector selector;
	private ServerSocketChannel serverSocket;
	private volatile boolean stop = false;
	
	//thread pool
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	public server() {
		try {
			selector = Selector.open();
			serverSocket = ServerSocketChannel.open();
			//serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(),port));
			serverSocket.socket().bind(new InetSocketAddress(port), server.maxConn);
			//非阻塞
			serverSocket.configureBlocking(false);
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(!stop) {
			try {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();
				SelectionKey key;
				while(iter.hasNext()) {
					key = iter.next();
					iter.remove();
					try {
						handlerKey(key);
					}catch(Exception e) {
						key.cancel();
						if(key.channel() != null) {
							key.channel().close();
						}
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(selector != null) {
			try {
				selector.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handlerKey(SelectionKey key) throws IOException{
		if(key.isValid()) {
			if(key.isAcceptable()) {
				ServerSocketChannel channel = (ServerSocketChannel) key.channel();
				SocketChannel accept = channel.accept();
				accept.configureBlocking(false);
				accept.register(selector,SelectionKey.OP_READ);
				doWrite(accept, "welcome! use $login username password to login");
			}
			
			if(key.isReadable()) {
				// buffer space
				ByteBuffer readBuffer = ByteBuffer.allocate(32);
				SocketChannel socketChannel = (SocketChannel)key.channel(); 
				// 读取数据
				int readBytes = socketChannel.read(readBuffer);
				// 读取到了数据
				if (readBytes > 0){
					 //System.out.println(key);
	                 readBuffer.flip();
	                 //System.out.println(readBuffer.remaining());
	                 byte[] bytes = new byte[readBuffer.remaining()];
	                 readBuffer.get(bytes);
	                 String msg = new String(bytes, "UTF-8");
	                 String tmp;
	                 if(MessageQueue.get(key) == null) {
	                	 char[] ta = msg.toCharArray();
	                	 int len = (int)ta[0];
	                	 tmp =  msg.substring(1);
	                	 Connection c = new Connection(tmp,len);
	                	 MessageQueue.put(key, c);
	                	 //MessageLength.put(key,len);
	                 }
	                 else {
	                	 Connection c = MessageQueue.get(key);
	                	 tmp = c.getMsg() + msg;
	                	 c.setMsg(tmp);
	                	 MessageQueue.put(key, c);
	                 }
                	 if(tmp.length() >= MessageQueue.get(key).getLength()) {
                		 executor.execute(new UserHandler(tmp, key,socketChannel));
                		 MessageQueue.remove(key);
                		 //MessageLength.remove(key);
                	 }
	                 System.out.println("receive key from:"+ key.toString() + " msg: " +  msg);
	                 //TODO new thread to deal
	                 //executor.execute(new UserHandler(msg, key,socketChannel));
	             }else if(readBytes < 0){
	                  //等于-1 ，链路已经关闭，需要释放资源
	                key.cancel();
	                socketChannel.close();
	             }else {
	                //等于0，忽略
	             }
			}
		}
	}
	
	public static void doWrite(SocketChannel socketChannel, String response) throws IOException {
		//System.out.println(response);
		byte[] bytes = response.getBytes();
		ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
		byteBuffer.put(bytes);
		byteBuffer.flip();
		synchronized (socketChannel) {
			socketChannel.write(byteBuffer);
		}
	}
	
	public static void main(String[] args) {
		System.out.println("init...");
		
		wholeUser = FileUtils.fileReadHashMap(sysPathUser);
		wholeRoom = FileUtils.fileReadHashMap(sysPathRoom);
		server.currentServerRedp = FileUtils.fileReadList(sysPathRp);
		//
		if(wholeUser == null) {
			wholeUser = new HashMap<>();
		}
		if(wholeRoom == null) {
			wholeRoom = new HashMap<>();
		}
		else {
			for(Room r : server.wholeRoom.values()) {
				r.roomUser = new HashSet<User>();
			}
		}
		if(server.currentServerRedp == null) {
			server.currentServerRedp = new ArrayList<>();
		}

		//init
		server.currentServerUser = new HashMap<>();
		server.MessageQueue = new HashMap<>();
		//server.MessageLength = new HashMap<>();
		//server.currentServerRoom = new ArrayList<>();
		//server.currentServerRedp = new ArrayList<>();
		
		server nioserver = new server();
		nioserver.start();
		
		System.out.println("服务端启动");
		//保存文件计时器
		Timer timer = new Timer();
		byte flag = 0x01 | 0x02 | 0x04;
		timer.schedule(new myTimerTask(flag), 1000 * 60);
		//test();
	}
	
	public static void Save(byte flag) {
		if((flag & 0x01) == 1) {
			lockUser.readLock().lock();
			FileUtils.fileWriteHashMap(server.sysPathUser, server.wholeUser);
			lockUser.readLock().unlock();
		}
		if((flag & 0x02) == 1) {
			lockRoom.readLock().lock();
			FileUtils.fileWriteHashMap(server.sysPathRoom, server.wholeRoom);
			lockRoom.readLock().unlock();
		}
		if((flag & 0x04) == 1) {
			lockRp.readLock().lock();
			FileUtils.fileWriteList(server.sysPathRp, server.currentServerRedp);
			lockRp.readLock().unlock();
		}
		//return "ok";
	}
	
	private static void test() {
		User user = new User();
		user.setName("wang");
		user.setPassword("123");
		//user.setSocket(null);
		//user.setStatus(0);
		//server.currentServerUser.add(user);
		//FileUtils.fileWriteSet(sysPathUser, currentServerUser);
		//
		//User user2 = new User();
		//user2.setName("wansd");
		//user2.setPassword("1235");
		//user2.setSocket(null);
		//user2.setStatus(0);
		//FileUtils.fileWriteAddList(sysPathUser, user2);
		//room
		Room room = new Room("mc");
		room.roomUser.add(user);
		//server.currentServerRoom.add(room);
		//FileUtils.fileWriteList(sysPathRoom, currentServerRoom);
		
		System.out.println("write finish");
		
		//wholeUser = FileUtils.fileReadSet(sysPathUser);
		System.out.println("wholeUser size is : " + wholeUser.size());
//		for(User u : wholeUser) {
//			System.out.println(u.getName());
//			System.out.println(u.getPassword());
//		}
//		wholeRoom = FileUtils.fileReadSet(sysPathRoom);
//		System.out.println(wholeRoom.size());
//		for(Room r : wholeRoom) {
//			System.out.println(r.getName());
//			for(User u : r.roomUser) {
//				System.out.println(u.getName());
//				System.out.println(u.getPassword());
//			}
//		}
	}

}
