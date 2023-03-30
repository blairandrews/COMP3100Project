import java.net.*;
import java.util.*;
import java.io.*;
public class TCPClient {
	String serverName;
	int serverID, serverCores;
	public static void main(String[] args){
		Socket s = null;
		try{
			int serverPort = 50000;
			s = new Socket("localhost", serverPort);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			
			out.write(("HELO\n").getBytes());
			String data = in.readLine().toString();
			System.out.println("Message = " + data);
			
			String username = System.getProperty("user.name");
			out.write(("AUTH " + username + "\n").getBytes());
			data = in.readLine().toString();
			System.out.println("Message = " + data);
			
			while(data != "NONE"){
				out.write(("REDY\n").getBytes());
				data = in.readLine().toString();
				System.out.println("Message = " + data);
			
				out.write(("GETS All\n").getBytes());
				data = in.readLine().toString();
				System.out.println("Message = " + data);

				String split[] = data.split("\\s+");
				int nRecs = Integer.parseInt(split[1]);
				int recSize = Integer.parseInt(split[2]); //finding how many records are in the config

				out.write(("OK\n").getBytes());

				ArrayList<TCPClient> splits = new ArrayList<TCPClient>();
				for(int i = 0; i < nRecs; i++){
					TCPClient obj = new TCPClient();
					split = in.readLine().toString().split("\\s+");
					obj.serverName = split[0];
					obj.serverID = Integer.parseInt(split[1]);
					obj.serverCores = Integer.parseInt(split[4]);
					splits.add(obj);
				}
				
				break;
			}
		} catch(UnknownHostException e){
			System.out.println("Socket: " + e.getMessage());
		} catch(EOFException e) {
			System.out.println("EOF: " + e.getMessage());
		} catch(IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(s != null) try { s.close();
				} catch (IOException e) {
					System.out.println("Close: " + e.getMessage());}
					}
	}
}
