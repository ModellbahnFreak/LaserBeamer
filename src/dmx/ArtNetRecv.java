package dmx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

import server.Server;

public class ArtNetRecv implements Runnable {
	
	byte[] recvDmx = null;
	ArtNetProcess verarb = null;
	
	public ArtNetRecv(Queue<String> recvData) {
		recvDmx = new byte[Server.einst.getChannelCount()];
		verarb = new ArtNetProcess(recvDmx, recvData);
	}

	@Override
	public void run() {
		System.out.println("Art-Net-Node started");
		Thread verarbT = new Thread(verarb);
		verarbT.setDaemon(true);
		verarbT.setName("ArtNetVerarbeitung");
		verarbT.start();
		int anzKanaele = Server.einst.getChannelCount();
		int startKanal = Server.einst.getStartAddr();
		try {
			DatagramSocket socket =	null;
			try {
				socket = new DatagramSocket(6454);
			} catch (BindException e) {
				e.printStackTrace();
				InetAddress lokal = getLocalAddr(Server.einst.getUDPinterface());
				System.out.println("Trying " + lokal.getHostAddress());
				socket = new DatagramSocket(6454, lokal);
			}
			socket.setBroadcast(true);
			System.out.println("Listen on " + socket.getLocalAddress() + " on port " + socket.getLocalPort() + " broadcast " + socket.getBroadcast());
			byte[] buf = new byte[512]; 
			DatagramPacket packet =	new DatagramPacket(buf, buf.length); 
			while (!Thread.currentThread().isInterrupted()) {
				//System.out.println("Waiting for data");
				socket.receive(packet);
				//System.out.println("Data received: ");
				byte[] daten = packet.getData();
				if (new String(daten).startsWith("Art-Net") && daten[8] == 0x00 && daten[9] == 0x50) {
					synchronized (recvDmx) {
						for (int i = 0; i < anzKanaele; i++) {
							recvDmx[i] = daten[17+startKanal+i];
							//System.out.println(Arrays.toString(recvDmx));
						}
						recvDmx.notifyAll();
					}
				}
			}
			socket.close();
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} finally {
			//verarbT.interrupt();
		}
	}
	
	private InetAddress getLocalAddr(int num) {
		try {
			ArrayList<InetAddress> pcAdressen = new ArrayList<InetAddress>();
			Enumeration<NetworkInterface> interf = NetworkInterface.getNetworkInterfaces();
			while (interf.hasMoreElements()) {
				NetworkInterface schnitt = interf.nextElement();
				String ifaceStr = "";
				ifaceStr += schnitt.getName() + " (" + schnitt.getDisplayName() + "): ";
				Enumeration<InetAddress> addressen = schnitt.getInetAddresses();
				if (addressen.hasMoreElements()) {
					boolean isLocalhost = false;
					boolean isIPV4 = false;
					InetAddress ipV4 = null;
					while (addressen.hasMoreElements()) {
						InetAddress addr = addressen.nextElement();
						if (addr.getHostAddress().startsWith("127.")) {
							isLocalhost = true;
						}
						if (addr.getAddress().length == 4) {
							isIPV4 = true;
							ipV4 = addr;
						}
						ifaceStr += addr.getHostAddress() + "/";
					}
					if (/*!isLocalhost &&*/ isIPV4) {
						System.out.println(ifaceStr);
						pcAdressen.add(ipV4);
					}
				} else {
					
				}
			}
			return pcAdressen.get(num);
		} catch (Exception e) {
			//e.printStackTrace();
			try {
				return InetAddress.getLocalHost();
			} catch (Exception e1) {
				//e1.printStackTrace();
				return null;
			}
		}
	}
}
