import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Node implements Runnable{

	public static String sender;
	public static String receiver;
	public static String current_node;
	public static String next_hop;
	public static String next_hop_interface;
	public static String next_hop_2 = "";
	public static String sending_interface;
	public static String state = "sending";
	public static boolean finish = false;
	public static int num_of_fragments;
	public static String[] data = new String[20];	
	public static String[][] TCP_IP=new String[20][12];
	public static String[][] Ethernet_Frame = new String[20][5];
	public static int TTL = 4;
	public static boolean not_found= false;
	
	public void run(){
		try {
			this.check();
		} catch (Exception e) {}
	}

	public void check() throws Exception{	
		while (!Thread.currentThread().getName().equals(current_node))
		{
			if (finish)
				Thread.currentThread().stop();
			if (not_found)
				Thread.currentThread().stop();
		}
		if (state.equals("sending"))
			transport();
		else 
			datalink();
	}//end of check class
	
	public void transport() throws Exception{	
		if(state.equals("sending")){
			fragmentation();
			TCP_IP_header();
			System.out.println(" * The node ("+Thread.currentThread().getName()+") finished data Segmentation and created TCP/IP header");
			network();
		}
		else
		{
			combine_segments(current_node);
			System.out.println(" * The node ("+Thread.currentThread().getName()+") has received the data successfully");
			finish = true;
			Thread.currentThread().stop();
		}	
	}//end of transport class	
	
	public void network() throws Exception{
		
		if (state.equals("sending")){
			routing(current_node,receiver);
			if (!next_hop.equals(""))
			{
				System.out.println(" * The node ("+Thread.currentThread().getName()+") found the next hop ("+ next_hop +")");
				datalink();
			}
		}
		else
		{
			if (ip(current_node).equals(TCP_IP[0][11]))
			{
				System.out.println(" * The node ("+Thread.currentThread().getName()+") checked the IP address. The destination's IP is equal to its address.");
				transport();
			}
			else
			{
				routing(current_node,receiver);
				if (!next_hop.equals(""))
				{
					System.out.println(" * The node ("+Thread.currentThread().getName()+") found the next hop ("+ next_hop +")");
					state = "sending";
					datalink();
				}
			}	
		}
		
	}//end of network
	
	public void datalink() throws Exception{
		if (state.equals("sending"))
		{
			for (int i=0;i<num_of_fragments; i++){
				Ethernet_Frame[i][0] = "0" ;//Preamble
				Ethernet_Frame[i][1] = mac(next_hop);//Destination Mac
				Ethernet_Frame[i][2] = mac(current_node);//Source Mac
				Ethernet_Frame[i][3] = "5" ;//Type
				Ethernet_Frame[i][4] = CRC(i) ;//Type
			}			
			print_frames();
			send();

		}
		else
		{
			for (int i=0;i<num_of_fragments; i++){
				if (!mac(current_node).equals(Ethernet_Frame[i][1]))
					System.out.println("error in transmition, The MAC addresses are not match");
				if (!mac(next_hop_2).equals(Ethernet_Frame[i][1]) && !next_hop_2.equals(""))
					System.out.println("destination's Mac dismatch at "+next_hop_2+" (frame"+(i+1)+")");
				String crc = CRC(i) ;//Type
				if (!crc.equals(Ethernet_Frame[i][4]))
					{
					System.out.println("error in transmition, The CRC values are not match");
					System.out.println(crc +Ethernet_Frame[i][4]);
					}
				
			}			
			next_hop_2 = "";
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println(" * The node ("+Thread.currentThread().getName()+") received the data");
			System.out.println(" * The node ("+Thread.currentThread().getName()+") finished checking CRC and MAC");
			network();
		}
		
	}
		
	public void routing (String current, String destination){
		
		//node A routing table
		if (current.equals("A")){
			if (destination.equals("B") || destination.equals("C"))//B or C
				{
					next_hop = "Router";
					next_hop_interface = "10.10.20.2";//The 1st interface of the Router
					sending_interface = "10.10.20.1";
				}
			else
				{
					next_hop = "Router";
					next_hop_interface = "10.10.20.2";
					sending_interface = "10.10.20.1";
				}
		}
		//node B routing table
		else if(current.equals("B")){
			if (destination.equals("C"))//C
				{
					next_hop = "C";
					next_hop_2="Router";
					next_hop_interface = "192.168.25.15";
					sending_interface = "192.168.25.20";
				}
			else //A
				{
					next_hop = "Router";
					next_hop_interface = "192.168.25.10"; //The 12nd interface of the Router
					sending_interface = "192.168.25.20";
				}
		}
		//node C routing table
		else if (current.equals("C")){
			if (destination.equals("B"))//B
				{
					next_hop = "B";
					next_hop_2="Router";
					next_hop_interface = "192.168.25.20";
					sending_interface = "192.168.25.15";
				}
			else 
				{
					next_hop = "Router";
					next_hop_interface = "192.168.25.10";//The 2nd interface of the Router
					sending_interface = "192.168.25.15";
				}
		}
		//node R routing table
		else if (current.equals("Router")){
			
			if (destination.equals("C"))//C
				{	
					next_hop = "C";
					next_hop_interface = "192.168.25.15";
					next_hop_2="B";
					sending_interface = "192.168.25.10";
				}
			else if(destination.equals("B"))//B
			{	
				next_hop = "B";
				next_hop_interface = "192.168.25.20";
				next_hop_2="C";
				sending_interface = "192.168.25.10";
			}
			else if(destination.equals("A"))//A
			{	
				next_hop = "A";
				next_hop_interface = "10.10.20.1";
				sending_interface = "10.10.20.2";
			}
			else{
				System.out.println();
				System.out.println(" !!!! The Destination ("+destination.toLowerCase()+") is not found! Transmission is Faild!");
				
				not_found=true;
				next_hop="";
				next_hop_interface="";
			}
		}
		
	}//end of routing class
	
	public void fragmentation() throws FileNotFoundException{
		Scanner xx = new Scanner (new File("data.txt"));
		String a="";
		while(xx.hasNext())
			a += xx.next()+" ";
		
		int f= a.length();
		num_of_fragments = (int)(f/1354)+1;
		int k=0;
		int l=0;
		while (f>=1354){
			String pp="";
			for (int i=l; i<1354+l; i++){
				pp += a.charAt(i);
			}
			data[k] = pp;
			k++;
			l += 1354;
			f -= 1354;
		}
		String pp="";
		for (int i=l; i<a.length(); i++){
			pp += a.charAt(i);
		}
			data[k] = pp;			
			xx.close();
	}// end of fragmentation function

	public void combine_segments(String s) throws Exception{
		Formatter xx = new Formatter("data received to "+s+".txt");
		int i = 0;
		while (data[i] != null){
			xx.format(data[i]);
			xx.format("\n");
			i++;
		}
		xx.close();
	}//end of combine
	
	public void TCP_IP_header(){
		int i=0;
		for (i=0;i<num_of_fragments; i++){
			TCP_IP[i][0] = "4";//Version
			TCP_IP[i][1] = "5";//Header length
			TCP_IP[i][2] = "0";//TOS
			TCP_IP[i][3] = Integer.toString(data[i].length());//Datagram length
			TCP_IP[i][4] = "0";//Ident
			TCP_IP[i][5] = "1";//Flags
			TCP_IP[i][6] = "0";//Offset
			TCP_IP[i][7] = Integer.toString(TTL);//TTL
			TCP_IP[i][8] = "16";//Protocol

			String checksum = Integer.toString(TCP_IP_header_checksum(i));

			TCP_IP[i][9] = checksum;//Checksum

			TCP_IP[i][10] = ip(sender);//Source Address
			TCP_IP[i][11] = ip(receiver);//Destination Address
		}		
			TCP_IP[i-1][5]="0";
			TTL--;
	}//end of TCP_IP_header
	
	public int TCP_IP_header_checksum(int ii){
		String[] bin = new String [10];
		String[] ss = new String[2];
		bin[0] = "0100010100000000";//1st 16 bits (Version + HLen + TOS)
		String m= Integer.toBinaryString(data[ii].length());
		if (m.length()<16){
			for (int u=m.length(); u<16; u++)
				m = "0"+m;
		}
		bin[1] = m;//2nd 16 bits (Length)
		bin[2] = "0000000000000000";//3rd 16 bits (Ident)
		bin[3] = "0010000000000000";//4th 16 bits (Flags + Offset)
		m= Integer.toBinaryString(TTL);
		if (m.length()<8){
			for (int u=m.length(); u<8; u++)
				m = "0"+m;
		}
		bin[4] = m+"00010000";//5th 16 bits (TTL + Protocol)
		bin[5] = "0000000000000000";//6th 16 bits (Checksum)

		ss = IP_to_binary(ip(sender));

		bin[6] = ss[0];				//7th 16 bits (first 2 fields of source's address)
		bin[7] = ss[1];				//8th 16 bits (second 2 fields of source's address)
		ss = IP_to_binary(ip(receiver));

		bin[8] = ss[0];				//9th 16 bits (first 2 fields of destination's address)
		bin[9] = ss[1];				//10th 16 bits (second 2 fields of source address)
		int b= checksum(bin);
		return b;
	}//end of TCP_IP_header_checksum

	public String CRC(int rr){
		String Frame_Aggregation="";
		for (int j=0; j<4; j++)
			Frame_Aggregation += Ethernet_Frame[rr][j];
		for (int j=0;j<12; j++)
			Frame_Aggregation += TCP_IP[rr][j];
		Frame_Aggregation += data[rr];
		byte bytes[] = Frame_Aggregation.getBytes();
		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		long c = crc.getValue();
		return String.valueOf(c);
	}//end of CRC

	public String ip(String st){
		if (st.equals("A")) return "10.10.20.1";
		else if (st.equals("B")) return "192.168.25.20";
		else if (st.equals("C")) return "192.168.25.15";
		else if (st.equals("Router")) return "192.168.25.10";
		else return "not valid";
	}//end of ip	
	
	public String mac(String st){
		String m="";
		if (st.equals("A"))
			m = "8:0:2b:47:e2:a3:8";
		else if (st.equals("B"))
			m="8:0:2b:3a:3a:33:42";
			else if (st.equals("C"))
				m="8:0:2b:50:39:1d:99";
				else if(st.equals("Router"))
					m="8:0:2b:a8:d2:b5:1";
		return m;
	}//end of mac
	
	public void print_frames(){		
		System.out.println(" * The node ("+Thread.currentThread().getName()+") sent all the frames from ("+sending_interface+") to the interface ("+next_hop_interface+")");
		System.out.println();
		
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("------------------------------------------| Frames Printout |---------------------------------------");
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println();
		for (int i=0; i<num_of_fragments; i++){
			System.out.println("Preamble = "+Ethernet_Frame[i][0]+", Destination Mac = "+Ethernet_Frame[i][1]+", Source Mac = "+Ethernet_Frame[i][2]+", CRC = "+Ethernet_Frame[i][4]);
			System.out.println();
			System.out.println("		  ...................TCP/IP Header of Segment ("+(i+1)+")..................");
			System.out.println("		  Version = "+TCP_IP[i][0]+" ,      HLen = "+TCP_IP[i][1]+" ,       TOS = "+TCP_IP[i][2]+" ,       Length = "+TCP_IP[i][3]);
			System.out.println("		  Ident = "+TCP_IP[i][4]+" ,                         Flags = "+TCP_IP[i][5]+" ,        Offset = "+TCP_IP[i][6]);
			System.out.println("		  TTL = "+TCP_IP[i][7]+" ,          Protocol = "+TCP_IP[i][8]+" ,               Checksum = "+TCP_IP[i][9]);
			System.out.println("		  SourceAddress = "+TCP_IP[i][10]+" ,   DestinationAddress = "+TCP_IP[i][11]);
			//System.out.println();
			//System.out.println("CRC 32 = "+Ethernet_Frame[i][4]);
			System.out.println();
			System.out.println("----------------------------------------------------------------------------------------------------");
			System.out.println();
		}
	}
	
	public void send(){		
		state = "receiving";
		current_node = next_hop;
		Thread.currentThread().stop();
	}//end of send_to
	
	public int checksum(String[] bin){
		int f=0;	
		String s1=bin[f++];
		String b = "";
		while (f<10){
			String s2=bin[f++];
			b = xor(s1,s2);	
			if (b.length()==bin[0].length()+1){
				int[] compl = strarray(b);
				String na = "";
				for (int u=0;u<b.length()-1;u++)
					na = na+compl[u+1];
				b = xor(na,"0000000000000001");
				}
			s1= b;
		}
		int[] cc = strarray(b);
		int len= b.length();
		b = "";
		for (int i=0; i<len; i++){	
			if (cc[i]==0)
				b = b+"1";
			else
				b=b+"0";
		}
		return Integer.parseInt(b, 2);
	}//end of checksum class
	
	public static String xor(String a, String c){				
		int[] ch1 = strarray(a);
		int[] ch2 = strarray(c);
		String b = "";
		int r=0;
		for (int i=a.length()-1;i>=0; i--){
			int j = ch1[i]+ch2[i]+r;
			if (j==3){
				b = "1"+b;
				r = 1;
			}
			else if (j == 2) {
				b = "0"+b;
				r = 1;
			}
			else
				{b=j+b;
				r = 0;
				}
		}
		if (r==1){
			b = "1"+b;
		}	
		return b;
	}//end of xor class

	public static int[] strarray(String bb){
		int o[] =new int[bb.length()];
		for (int i=0; i<bb.length(); i++)
		   {
			String pp = ""+ bb.charAt(i);
			o[i]= Integer.parseInt(pp);
		   }
		return o;
	}//end of strarray
	
	public String[] IP_to_binary(String ip){
		String[] ss= new String[2];
		if (ip.equals("not valid"))
			{
			ss[0]="0000000000000000";
			ss[1]="0000000000000000";
			return ss;
			}
		String f = "";
		int[] ff = new int[40];
		int k=0;
		for (int i=0;i<ip.length();i++){
			if (ip.charAt(i)!='.')
				f += (ip.charAt(i));
			else
				{
					ff[k] = Integer.parseInt(f);
					f = "";
					k++;
				}
		}
		ff[k] = Integer.parseInt(f);
		String s3="";
	for (int j=0;j<4;j++){
		int b[] =new int[30];	
		int i=0;
			while (ff[j]>=2)
			 {
				b[i] = (ff[j] % 2);
				i+=1;
				ff[j] /= 2;
			 }
			b[i]=ff[j];
		String s2 = "";
		for (int s1=i;s1>=0;s1--)
			s2 = s2 + b[s1];
		if (s2.length()<8){
			for (int h=s2.length(); h<8; h++)
				s2 = "0"+s2;
		}
		s3 = s3+s2;
	}

	ss[0] = s3.substring(0, 16);
	ss[1] = s3.substring(16, 32);
	return ss;
	}//end of ip to binary class
}