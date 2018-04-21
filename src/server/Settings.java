package server;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Settings {

	//Screenshot stuff
	private boolean _screenshActive = false;
	private BufferedImage _screensh;
	private boolean newScreensh = false;
	
	//Art-Net Settings
	private boolean ArtNetActive = false;
	private int universe = 0;
	private int startAddr = 1;
	private byte[] kanaele;
	
	//Other Settings
	private String homeDirectory = "./";
	private int UDPintertface = 0;
	private String[] loadSeq;

	Settings() {
	}

	public boolean getStateScreenshActive() {
		return _screenshActive;
	}

	public void setStateScreenshActive(boolean Status) {
		_screenshActive = Status;
	}

	public BufferedImage getScreenshot() {
		synchronized (this) {
			if (newScreensh && _screenshActive) {
				newScreensh = false;
				return _screensh;
			} else {
				return null;
			}
		}
	}

	public void setScreenshot(BufferedImage bild) {
		synchronized (this) {
			_screensh = bild;
			newScreensh = true;
		}
	}
	
	public boolean getArtNetState() {
		return ArtNetActive;
	}
	
	public void setArtNetState(boolean State) {
		ArtNetActive = State;
	}
	
	public String getHomeDir() {
		return homeDirectory;
	}
	
	public void setHomeDir(String home) {
		homeDirectory = home;
	}
	
	public byte[] getChannels() {
		return kanaele;
	}
	
	public int getChannelCount() {
		return kanaele.length;
	}
	
	public void setChannels(byte[] channels) {
		kanaele = channels;
	}
	
	public void setChannels(String[] channels) {
		byte[] tempArr = new byte[channels.length];
		int laenge = 0;
		for (int i = 0; i < channels.length; i++) {
			String kanal = channels[i];
			switch(kanal) {
			case "MasterDimmer":
				tempArr[i] = 0;
				laenge++;
				break;
			case "Function":
				tempArr[i] = 1;
				laenge++;
				break;
			case "Select":
				tempArr[i] = 2;
				laenge++;
				break;
			case "Play":
				tempArr[i] = 3;
				laenge++;
				break;
			case "Pattern":
				tempArr[i] = 4;
				laenge++;
				break;
			case "Color":
				tempArr[i] = 5;
				laenge++;
				break;
			case "Size":
				tempArr[i] = 6;
				laenge++;
				break;
			case "Multi":
				tempArr[i] = 7;
				laenge++;
				break;
			case "xPos":
				tempArr[i] = 8;
				laenge++;
				break;
			case "yPos":
				tempArr[i] = 9;
				laenge++;
				break;
			case "Dimmer":
				tempArr[i] = 10;
				laenge++;
				break;
			case "RotOffset":
				tempArr[i] = 11;
				laenge++;
				break;
			default:
				tempArr[i] = -1;/*Reserved*/
				break;
			}
		}
		byte[] kanalNeu = new byte[laenge];
		int a = 0;
		for (int i = 0; i < tempArr.length; i++) {
			byte kanal = tempArr[i];
			if (kanal != -1) {
				kanalNeu[a] = kanal;
				a++;
			}
		}
		kanaele = kanalNeu;
	}
	
	public int getUDPinterface() {
		return UDPintertface;
	}
	
	public void setUDPinterface(int interfNum) {
		UDPintertface = interfNum;
	}
	
	public int getUniverse() {
		return universe;
	}
	
	public void setUniverse(int universeNum) {
		universe = universeNum;
	}
	
	public int getStartAddr() {
		return startAddr;
	}
	
	public void setStartAddr(int adress) {
		startAddr = adress;
	}
	
	public String[] getSeqLoad() {
		return loadSeq;
	}
	
	public void setSeqLoad(String[] seqLoad) {
		loadSeq = seqLoad;
	}
	
	@Override
	public String toString() {
		String erg = "Einstellungen:\n";
		erg += "\tDmxEnable: " + ArtNetActive + "\n";
		erg += "\tDmxUniverse: " + universe + "\n";
		erg += "\tDmxAddr: " + startAddr + "\n";
		erg += "\tDmxKanaele: " + Arrays.toString(kanaele) + "\n";
		erg += "\thomeDir: " + homeDirectory + "\n";
		erg += "\tUdpInterface: " + UDPintertface + "\n";
		erg += "\tAutoloadSeq: " + Arrays.deepToString(loadSeq);
		return erg;
	}
}
