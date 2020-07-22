package chatroom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class FileUtils {
	public static <T> void fileWriteList(String path, List<T> list) {
		try {
			FileOutputStream outputStream = new FileOutputStream(path);
			ObjectOutputStream stream = new ObjectOutputStream(outputStream);
			stream.writeObject(list);
			stream.close();
			outputStream.close();
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	public static <T,P> void fileWriteHashMap(String path, HashMap<T,P> HashMap) {
		try {
			FileOutputStream outputStream = new FileOutputStream(path);
			ObjectOutputStream stream = new ObjectOutputStream(outputStream);
			stream.writeObject(HashMap);
			stream.close();
			outputStream.close();
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	//public static <T> void fileWriteAddList(String path, T data) {
	//	try {
	//		FileOutputStream outputStream = new FileOutputStream(path, true);
	//		ObjectOutputStream stream = new ObjectOutputStream(outputStream);
	//		stream.writeObject(data);
	//		stream.close();
	//		outputStream.close();
	//	}
	//	catch(FileNotFoundException e) {
	//		e.printStackTrace();
	//	}
	//	catch(IOException e) {
	//		e.printStackTrace();
	//	}
	//}
	@SuppressWarnings("unchecked")
	public static <T> List<T> fileReadList(String path) {
		File file = new File(path);
		if(!file.exists()) {
			return null;
		}
		List<T> list = null;
		try {
			FileInputStream inputStream = new FileInputStream(path);
			ObjectInputStream stream = new ObjectInputStream(inputStream);
			list = (List<T>)stream.readObject();
			inputStream.close();
			stream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	@SuppressWarnings("unchecked")
	public static <T,P> HashMap<T,P> fileReadHashMap(String path) {
		File file = new File(path);
		if(!file.exists()) {
			return null;
		}
		HashMap<T,P> hashmap = null;
		try {
			FileInputStream inputStream = new FileInputStream(path);
			ObjectInputStream stream = new ObjectInputStream(inputStream);
			hashmap = (HashMap<T,P>)stream.readObject();
			inputStream.close();
			stream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return hashmap;
	}
}
