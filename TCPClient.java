import java.net.*;
import java.io.*;
public class TCPClient {
	public static void main(String[] args){
		Socket s = null;
		try{
			int serverPort = 52000;
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
			
			out.write(("REDY\n").getBytes());
			data = in.readLine().toString();
			System.out.println("Message = " + data);
			
			out.write(("QUIT\n").getBytes());
			data = in.readLine().toString();
			System.out.println("Message = " + data);
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
