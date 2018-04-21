package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Queue;

import dmx.ArtNetRecv;
import server.Gui;

public class Server {

	public static Settings einst = new Settings();
	//private static ImageStream imgStream = new ImageStream(8083);
	//public static String homeDirectory = "./";
	
	public static void main(String[] args) {
		String settingsFile = "settings.ini";
		if (args.length >= 1) {
			settingsFile = args[0];
		}
		Queue<String> recvData = new LinkedList<String>();
		Queue<String> sendData = new LinkedList<String>();
		if (new File(settingsFile).exists()) {
			parseSettings(settingsFile);
		}
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
		if (einst.getArtNetState()) {
			ArtNetRecv dmxEmpf = new ArtNetRecv(recvData);
			Thread dmxT = new Thread(dmxEmpf);
			dmxT.setDaemon(true);
			dmxT.setName("DmxEmpfang");
			dmxT.start();
		}
		if (einst.getSeqLoad() != null) {
			for (String seq : einst.getSeqLoad()) {
				synchronized (recvData) {
					recvData.add("loadSeq;" + seq);
					recvData.notifyAll();
				}
			}
		}
		/*synchronized (recvData) {
			recvData.add("txt;txt1;100;100;'Hallo Welt';#ffffff");
			recvData.add("txt;txt2;100;200;'LOL';#FF0000");
			recvData.notifyAll();
		}*/
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
	
	public static void parseSettings(String path) {
		if (new File(path).exists()) {
			try {
				BufferedReader read = new BufferedReader(new FileReader(path));
				String zeile = null;
				while((zeile=read.readLine()) != null) {
					String[] split = zeile.split("=");
					switch(split[0]) {
					case "homeDir":
						einst.setHomeDir(zeile.substring("homeDir=".length()));
						break;
					case "UDPinterface":
						try {
							einst.setUDPinterface(Integer.parseInt(zeile.substring("UDPinterface=".length())));
						} catch (NumberFormatException e) {
							einst.setUDPinterface(0);
						}
						break;
					case "DMXEnable":
						if ("1".equals(zeile.substring("DMXEnable=".length()))) {
							einst.setArtNetState(true);
						} else {
							einst.setArtNetState(false);
						}
						break;
					case "DMXUniverse":
						try {
							einst.setUniverse(Integer.parseInt(zeile.substring("DMXUniverse=".length())));
						} catch (NumberFormatException e) {
							einst.setUniverse(0);
						}
						break;
					case "DMXStartAddr":
						try {
							einst.setStartAddr(Integer.parseInt(zeile.substring("DMXStartAddr=".length())));
						} catch (NumberFormatException e) {
							einst.setStartAddr(1);
						}
						break;
					case "DMXChannels":
						einst.setChannels(zeile.substring("DMXChannels=".length()).split(";"));
						break;
					case "SeqAutoload":
						einst.setSeqLoad(zeile.substring("SeqAutoload=".length()).split(";"));
					default:
						break;
					}
				}
				read.close();
				System.out.println(einst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
