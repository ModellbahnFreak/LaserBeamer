package server;

import java.util.LinkedList;
import java.util.Queue;
import server.Gui;

public class Server {
	
	public static Settings einst = new Settings();
	//private static ImageStream imgStream = new ImageStream(8083);
	public static String homeDirectory = "./";
	
	public static void main(String[] args) {
		if (args.length >= 1) {
			homeDirectory = args[0];
		}
		Queue<String> comm = new LinkedList<String>();
		Queue<String> SendComm = new LinkedList<String>();
		Gui gui = new Gui();
		Thread guiT = new Thread(gui);
		guiT.setName("GuiLaunch");
		guiT.start();
		Network netw = new Network(comm, SendComm, 8082);
		netw.setName("NetzwerkCheckRecv");
		netw.start();
		NetwProcess verarb = new NetwProcess(comm, SendComm);
		verarb.setName("NetzwerkVerarb");
		verarb.start();
	}
	
	public static void StartLivestream() {
		/*Thread streamT = new Thread(imgStream);
		streamT.setName("LiveStream");
		streamT.setDaemon(true);
		streamT.start();*/
	}
	
	public static void StopLivestream() {
		//imgStream.cancel();
	}

}
