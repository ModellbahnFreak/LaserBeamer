package de.modellbahnfreak.laserbeamer.webserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import de.modellbahnfreak.laserbeamer.webserver.ws.SocketHandler;
import de.modellbahnfreak.laserbeamer.webserver.ws.SocketSendReceive;

public class HttpListener implements Runnable {
	
	private ServerSocket server = null;
	private ArrayList<HttpRequest> openCons = new ArrayList<HttpRequest>();
	private ArrayList<SocketHandler> openWs = new ArrayList<SocketHandler>();
	private final int _port;
	
	public final SocketSendReceive sendRecv = new SocketSendReceive() {
		
		@Override
		public void send(String text) {
			synchronized (openWs) {
				for (SocketHandler s : openWs) {
					s.sendRecv.send(text);
					synchronized (s.sendRecv) {
						s.sendRecv.notifyAll();
					}
				}
			}
		}
		
		@Override
		public String popReceive() {
			for (SocketHandler s : openWs) {
				if (s.sendRecv.hasRecv()) {
					return s.sendRecv.popReceive();
				}
			}
			return null;
		}

		@Override
		public boolean hasRecv() {
			for (SocketHandler s : openWs) {
				if (s.sendRecv.hasRecv()) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String popRecvBlocking() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	private final ProcessCallback procCallb = new ProcessCallback() {

		@Override
		public void sendAll(String cmd) {
			for (SocketHandler s : openWs) {
				s.sendRecv.send(cmd);
			}
		}
		
	};

	public HttpListener(int port) {
		_port = port;
	}

	@Override
	public void run() {
		try {
			System.out.println("Starting");
			server = new ServerSocket(_port);
			while (!Thread.currentThread().isInterrupted()) {
				Iterator<HttpRequest> it = openCons.iterator();
				while (it.hasNext()) {
					if (it.next().isFinished()) {
						//System.out.println("Deleting connection");
						it.remove();
					}
				}
				synchronized (openWs) {
					Iterator<SocketHandler> wsIt = openWs.iterator();
					while (wsIt.hasNext()) {
						if (wsIt.next().isFinished()) {
							wsIt.remove();
						}
					}
				}
				Socket con = server.accept();
				//System.out.println("New connection");
				HttpRequest req = new HttpRequest(con, wsCallb);
				Thread tReq = new Thread(req);
				tReq.setDaemon(true);
				tReq.start();
				tReq.setName("RequestHandler:" + con.getInetAddress().getHostAddress());
				openCons.add(req);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SocketCallback wsCallb = new SocketCallback() {
		@Override
		public void startWsHandler(Socket _con, BufferedReader _in, PrintStream _out) {
			synchronized (openWs) {
				SocketHandler wsHandle = new SocketHandler(_con, _in, _out, procCallb);
				wsHandle.handle();
				openWs.add(wsHandle);
			}
		}
	}; 

}
