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
		Queue<String> recvData = new LinkedList<String>();
		Queue<String> sendData = new LinkedList<String>();
		Gui gui = new Gui();
		Thread guiT = new Thread(gui);
		guiT.setName("GuiLaunch");
		guiT.start();
		Network netw = new Network(recvData, sendData, 8082);
		Thread netwT = new Thread(netw);
		netwT.setName("NetzwerkCheckRecv");
		netwT.setDaemon(true);
		netwT.start();
		NetwProcess verarb = new NetwProcess(recvData, sendData);
		verarb.setName("NetzwerkVerarb");
		verarb.start();
		recvData.add("txt;txt1;100;100;'Hallo Welt';#ffffff");
		recvData.add("txt;txt2;100;200;'LOL';#FF0000");
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
