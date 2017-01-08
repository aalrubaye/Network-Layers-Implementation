import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


public class MainClass {

	public static void main(String[] args) throws Exception {
  
		Node node = new Node();
		Thread A = new Thread(node, "A");
		Thread B = new Thread(node, "B");
		Thread C = new Thread(node, "C");
		Thread R = new Thread(node, "Router");
				
		get_addresses();		
		
		if (Node.sender != Node.receiver){
			A.start();		
			C.start();
			B.start();
			R.start();
		}
		else
			System.out.println("You're sending the data to yourself :)");
		
		
 }
	
	public static void get_addresses(){
		
		Scanner in= new Scanner(System.in);
		System.out.println("enter the SENDER :");
		String s = in.next();
		String ss = valid_address(s);
		
		while (!ss.equals("A")&&!ss.equals("B")&&!ss.equals("C")){
			System.out.println("The entered address is not valid. Try another one !");
			s = in.next();
			ss = valid_address(s);
		}
		Node.sender = ss;
		System.out.println("enter the RECEIVER :");
		s = in.next();
		ss = valid_address(s);
		Node.receiver = ss;
		Node.current_node = Node.sender;
	}
	
	public static String valid_address(String s){
		
		s = s.toUpperCase();
		if (s.equals("A")||s.equals("10.10.20.1"))	
			return "A";
		else if (s.equals("B")||s.equals("192.168.25.20"))
			return "B";
		else if (s.equals("C")||s.equals("192.168.25.15"))
			return "C";
		else
			return s;
	}
	
}
