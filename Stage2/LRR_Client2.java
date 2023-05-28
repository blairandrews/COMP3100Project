/*
 * Student ID: 46977880
 * Name: Blair Andrews
 */

import java.net.*;
import java.util.*;
import java.io.*;

public class LRR_Client2 {
	String serverName, status;
	int serverID, serverCores, waitingJobs, runningJobs; //initialising LRR_Client object variables

	public static int getMaxCoreCount(ArrayList<LRR_Client2> serverList, int capCores) { 
		int maxCores = Integer.MIN_VALUE; //constant min. integer value (so its not zero)
		
		for (int i = 0; i < serverList.size(); i++) {
			if (serverList.get(i).serverCores > maxCores) {
				maxCores = serverList.get(i).serverCores;
			}
		}
		return maxCores;
	}

	public static LRR_Client2 getMinJobs(ArrayList<LRR_Client2> serverList, int reqCores) { 
		LRR_Client2 serverLeastJobs = null;

		for (int i = 0; i < serverList.size(); i++) {
			if((serverList.get(i).waitingJobs == 0 && serverList.get(i).runningJobs == 0) && serverList.get(i).serverCores >= reqCores){
				serverLeastJobs = serverList.get(i);
				break;
			}
		}
		for (int i = 0; i < serverList.size(); i++) {
			if((serverList.get(i).waitingJobs == 0 || serverList.get(i).runningJobs == 0) && serverList.get(i).serverCores >= reqCores){
				serverLeastJobs = serverList.get(i);
				break;
			}
		}
		return serverLeastJobs;
	}

	public static void main(String[] args) {
		Socket s = null;
		try {
			int serverPort = 50000;
			s = new Socket("localhost", serverPort); //initialising network socket
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream out = new DataOutputStream(s.getOutputStream()); 
			//initialising input and output variables for communications

			out.write(("HELO\n").getBytes()); //writing message to server
			String data = in.readLine().toString(); //receiving from server and inputting it into a String to manipulate
			System.out.println("Message = " + data);

			String username = System.getProperty("user.name"); //getting username of system

			out.write(("AUTH " + username + "\n").getBytes());
			data = in.readLine().toString();
			System.out.println("Message = " + data);

			Boolean serverFound = false;

			String largestServer = "";

			ArrayList<LRR_Client2> servers = new ArrayList<LRR_Client2>();
			ArrayList<LRR_Client2> largestServers = new ArrayList<LRR_Client2>();

			//variable initialisations outside of while loop so they don't break
			int serverID = 0;
			int jobID = 0;
			int numCores = 0;
			int memory = 0;
			int disk = 0;
			int nRecs = 0;
			int bestFit = 0;
			int largestServerID = 0;


			while (true) {
				servers = new ArrayList<LRR_Client2>();
				largestServers = new ArrayList<LRR_Client2>();
				out.write(("REDY\n").getBytes());
				data = in.readLine().toString();
				System.out.println("Message = " + data);

				if (data.equals("NONE")) //checking for end of server requests, since while loop was breaking
					break;

				String sMessageSplit[] = data.split("\\s+"); //splitting data into useable indexes of information using whitespace as a delimiter
				String cmdCode = sMessageSplit[0]; //getting first index of string, typically JOBN, JCPL, the command code

				if (cmdCode.equals("JOBN")) {
					jobID = Integer.parseInt(sMessageSplit[2]);
					numCores = Integer.parseInt(sMessageSplit[4]);
					memory = Integer.parseInt(sMessageSplit[5]);
					disk = Integer.parseInt(sMessageSplit[6]); //if the command is a JOBN request, get the job info
				} else {
					continue; //if the command is anything else, restart the loop as we don't necessarily need to handle it
				}
				
				if (serverFound == false) {
					//get a list of the servers capable of running the jobs with the given information
					out.write(("GETS Capable " + numCores + " " + memory + " " + disk + "\n").getBytes());
					data = in.readLine().toString();
					System.out.println("Message = " + data);

					String split[] = data.split("\\s+");

					nRecs = Integer.parseInt(split[1]); // finding how many servers there are that are capable

					out.write(("OK\n").getBytes());
					
					for (int i = 0; i < nRecs; i++) {
						LRR_Client2 Server = new LRR_Client2(); //initialising new Server object to hold each server info
						split = in.readLine().toString().split("\\s+");
						Server.serverName = split[0];
						Server.serverID = Integer.parseInt(split[1]);
						Server.serverCores = Integer.parseInt(split[4]);
						Server.waitingJobs = Integer.parseInt(split[7]);
						Server.runningJobs = Integer.parseInt(split[8]);
						Server.status = split[2];
						servers.add(Server); //adding the server to an ArrayList of servers for future use
					}
					LRR_Client2 serv = getMinJobs(servers, numCores);
					if(serv == null){
						bestFit = getMaxCoreCount(servers, numCores);
						for(int i = 0; i < servers.size(); i++){
							if(servers.get(i).serverCores >= numCores){
								serv = servers.get(i);
								break;
							}
						}
						
						if(serv == null){
							for(int i = 0; i < servers.size(); i++){
								if(servers.get(i).status == "active" || servers.get(i).status == "booting"){
									serv = servers.get(i);
								}
							}
							
						}
						if(serv == null){
							serv = servers.get(0);
						}
					} 
					largestServer = serv.serverName;
					largestServerID = serv.serverID;
						
					

					for (int i = 0; i < servers.size(); i++) { //adding all the largest servers to a new ArrayList
						if (servers.get(i).serverName.equals(largestServer)) {
							largestServers.add(servers.get(i));
						}
					}

					out.write(("OK\n").getBytes());
					data = in.readLine().toString();
					System.out.println("Message = " + data);

					//making it so the largest server finding loop doesn't run again if its found the largest server
					// if (!largestServer.isEmpty()) 
					// 	serverFound = true;
					serverFound = true;
				}

				int largestServerCount = largestServers.size(); 

				if (cmdCode.equals("JOBN")) { //if the command code is equal to JOBN, then we can schedule
					out.write(("SCHD " + jobID + " " + largestServer + " " + largestServerID + "\n").getBytes()); //send job schedule
					
					data = in.readLine().toString();
					System.out.println("Message = " + data);
					serverFound = false;
				}
				
			}

			out.write(("QUIT\n").getBytes()); //quit
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
