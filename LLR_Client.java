/*
 * Student ID: 46977880
 * Name: Blair Andrews
 */

import java.net.*;
import java.util.*;
import java.io.*;

public class LLR_Client {
	String serverName;
	int serverID, serverCores;

	public static int getMaxCoreCount(ArrayList<LLR_Client> serverList) {
		int maxCores = Integer.MIN_VALUE;
		
		for (int i = 0; i < serverList.size(); i++) {
			if (serverList.get(i).serverCores > maxCores) {
				maxCores = serverList.get(i).serverCores;
			}
		}
		return maxCores;
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

			ArrayList<LLR_Client> servers = new ArrayList<LLR_Client>();
			ArrayList<LLR_Client> largestServers = new ArrayList<LLR_Client>();

			int serverID = 0;
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

				String sMessageSplit[] = data.split("\\s+");
				String cmdCode = sMessageSplit[0];

				if (cmdCode.equals("JOBN")) {
					jobID = Integer.parseInt(sMessageSplit[2]);
					numCores = Integer.parseInt(sMessageSplit[4]);
					memory = Integer.parseInt(sMessageSplit[5]);
					disk = Integer.parseInt(sMessageSplit[6]);
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
						LLR_Client obj = new LLR_Client();
						split = in.readLine().toString().split("\\s+");
						obj.serverName = split[0];
						obj.serverID = Integer.parseInt(split[1]);
						obj.serverCores = Integer.parseInt(split[4]);
						servers.add(obj);
					}

					for (int i = 0; i < servers.size(); i++) {
						if (servers.size() == 1) {
							largestServer = servers.get(i).serverName;
							break;
						}
						largestCoreCount = getMaxCoreCount(servers);
						if (servers.get(i).serverCores == largestCoreCount) {
							largestServer = servers.get(i).serverName;
							break;
						}
					}

					for (int i = 0; i < servers.size(); i++) {
						if (servers.get(i).serverName.equals(largestServer)) {
							largestServers.add(servers.get(i));
						}
					}

					out.write(("OK\n").getBytes());
					data = in.readLine().toString();
					System.out.println("Message = " + data);

					if (!largestServer.isEmpty())
						serverFound = true;
				}

				int largestServerCount = largestServers.size();

				if (cmdCode.equals("JOBN")) {
					if (serverID >= largestServerCount)
						serverID = 0;
					out.write(("SCHD " + jobID + " " + largestServer + " " + serverID + "\n").getBytes());
					data = in.readLine().toString();
					System.out.println("Message = " + data);
					serverID++;

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
