package de.modellbahnfreak.laserbeamer.server;

import java.io.*;
import java.net.*;
import java.util.Queue;

public class Network implements Runnable {
	private final Queue<String> _recvData;
	private final Queue<String> _sendData;
	private ServerSocket server = null;
	private int port = 8082;
	Socket socket = null;

	
	public Network(Queue<String> recvData, Queue<String> sendData, int ServerPort) {
		this._recvData = recvData;
		this._sendData = sendData;
		port = ServerPort;
	}
	
	@Override
	public void run() {
		if (serverSetup()) {
			while(!Thread.currentThread().isInterrupted()) {
				System.out.println("Waiting");
				if (connect()) {
					System.out.println("Connected");
					Thread sendT = new Thread(senden);
					Thread recvT = new Thread(empfangen);
					sendT.setDaemon(true);
					recvT.setDaemon(true);
					sendT.start();
					recvT.start();
					System.out.println("Daemons started - Working");
					while (sendT.getState()!=Thread.State.TERMINATED && recvT.getState()!=Thread.State.TERMINATED && !Thread.currentThread().isInterrupted()) {
						try {
							Thread.sleep(40);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					sendT.interrupt();
					recvT.interrupt();
					System.out.println("Closing");
					if (!closeConn()) {
						Thread.currentThread().interrupt();
						System.out.println("Closing error - Exiting");
					} else {
						System.out.println("Completed");
					}
				} else {
					System.out.println("Connecting unsuccessful");
				}
			}
		} else {
			System.out.println("Socket creation Fehler");
		}
	}
	
	private boolean serverSetup() {
		try {
			server = new ServerSocket(port);
			return true;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			//System.exit(1);
			return false;
		}
	}
	
	private boolean connect() {
		try {
			socket = server.accept();
			return true;
			//System.out.println("Finished");
		} catch (IOException e) {
			//System.out.println("IOFehler (Vermutl. Verbindung beendet)");
			return false;
			// e.printStackTrace();
		}
	}
	
	private Runnable senden = new Runnable() {
		@Override
		public void run() {
			PrintStream raus = null;
			try {
				synchronized (socket) {
					raus = new PrintStream(socket.getOutputStream());
				}
				String toSend;
				while (!Thread.currentThread().isInterrupted() && socket != null && socket.isConnected()) { 
					toSend = null;
					synchronized (_sendData) {
						toSend = _sendData.poll();
					}
					if (toSend != null) {
						System.out.println("NotNull");
						raus.print(toSend + "\r\n");
					} else {
						synchronized (_sendData) {
							try {
								_sendData.wait(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (IOException e) {
				
			} finally {
				if (raus != null) {
					raus.close();
				}
			}
		}
	};
	
	private Runnable empfangen = new Runnable() {
		@Override
		public void run() {
			BufferedReader rein = null;
			try {
				synchronized (socket) {
					rein = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				}
				String s;
				boolean cancel = false;
				while (!Thread.currentThread().isInterrupted() && !cancel && (s = rein.readLine()) != null) {
					if ("exit".equals(s) || "connection;close".equals(s)) {
						cancel = true;
					} else {
						System.out.println("Received cmd: " + s);
						synchronized (_recvData) {
							_recvData.add(s);
							_recvData.notifyAll();
						}
					}
				}
			} catch (IOException e) {
				
			} finally {
				if (rein != null) {
					try {
						rein.close();
					} catch (IOException e) {
						
					}
				}
			}
		}
	};
	
	private boolean closeConn() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
			synchronized (_recvData) {
				_recvData.add("connection;close");
				_recvData.notifyAll();
			}
			return true;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
