import java.net.*;
import java.util.*;
import java.io.*;

public class TCPClient {
	String serverName;
	int serverID, serverCores;

	public static int getMax(ArrayList<TCPClient> list) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).serverCores > max) {
				max = list.get(i).serverCores;
			}
		}
		return max;
	}

	public static void main(String[] args) {
		Socket s = null;
		try {
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

			boolean serverFound = false;

			String largestServer = "";

			ArrayList<TCPClient> splits = new ArrayList<TCPClient>();
			ArrayList<TCPClient> splitty = new ArrayList<TCPClient>();

			int count = 0;
			int largestCoreCount = 0;
			int jobID = 0;
			int numCores = 0;
			int memory = 0;
			int disk = 0;
			int nRecs = 0;

			while (true) {
				out.write(("REDY\n").getBytes());
				data = in.readLine().toString();
				System.out.println("Message = " + data);

				if (data.equals("NONE"))
					break;

				String splitted[] = data.split("\\s+");
				String cmdCode = splitted[0];

				if (cmdCode.equals("JOBN")) {
					jobID = Integer.parseInt(splitted[2]);
					numCores = Integer.parseInt(splitted[4]);
					memory = Integer.parseInt(splitted[5]);
					disk = Integer.parseInt(splitted[6]);
				} else {

					continue;
				}

				if (serverFound == false) {
					out.write(("GETS Capable " + numCores + " " + memory + " " + disk + "\n").getBytes());
					data = in.readLine().toString();
					System.out.println("Message = " + data);

					String split[] = data.split("\\s+");

					nRecs = Integer.parseInt(split[1]); // finding how many servers there are that are capable

					out.write(("OK\n").getBytes());

					for (int i = 0; i < nRecs; i++) {
						TCPClient obj = new TCPClient();
						split = in.readLine().toString().split("\\s+");
						obj.serverName = split[0];
						obj.serverID = Integer.parseInt(split[1]);
						obj.serverCores = Integer.parseInt(split[4]);
						splits.add(obj);
					}

					for (int i = 0; i < splits.size(); i++) {
						if (splits.size() == 1) {
							largestServer = splits.get(i).serverName;
							break;
						}
						largestCoreCount = getMax(splits);
						if (splits.get(i).serverCores == largestCoreCount) {
							largestServer = splits.get(i).serverName;
							break;
						}
					}

					for (int i = 0; i < splits.size(); i++) {
						if (splits.get(i).serverName.equals(largestServer)) {
							splitty.add(splits.get(i));
						}
					}

					out.write(("OK\n").getBytes());
					data = in.readLine().toString();
					System.out.println("Message = " + data);

					if (!largestServer.isEmpty())
						serverFound = true;
				}

				int largestServerCount = splitty.size();

				if (cmdCode.equals("JOBN")) {
					if (count >= largestServerCount)
						count = 0;
					out.write(("SCHD " + jobID + " " + largestServer + " " + count + "\n").getBytes());
					data = in.readLine().toString();
					System.out.println("Message = " + data);
					count++;

				}
			}

			out.write(("QUIT\n").getBytes());
			data = in.readLine().toString();
			System.out.println("Message = " + data);

		} catch (UnknownHostException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (EOFException e) {
			System.out.println("EOF: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (IOException e) {
					System.out.println("Close: " + e.getMessage());
				}
		}
	}

}
