package de.modellbahnfreak.laserbeamer.sequenceNew;

import java.util.ArrayList;

import javafx.scene.Node;
import de.modellbahnfreak.laserbeamer.sequence.DeleteHandler;
import de.modellbahnfreak.laserbeamer.sequence.NodeCreator;
import de.modellbahnfreak.laserbeamer.server.Gui;

public class Sequenz {
	transient int length = 10;
	private ArrayList<ObjProperties> objEigensch;
	private int msPerFrame = (int)(1000.0/25.0);
	//									  Framerate
	private transient Thread SeqT = null;
	private String seqName = "";
	private int dmxNum = 0; //Nummer unter der die Sequence per DMX abzuspielen ist
	int frame = 0;
	
	private Runnable play = new Runnable() {
		@Override
		public void run() {
			for (ObjProperties props : objEigensch) {
				Gui.INSTANCE.addNodeList.add(props.getObj());
				int propLength = props.getLength(); 
				if (propLength > length) {
					length = propLength;
				}
			}
			System.out.println("Laenge: " + length);
			System.out.println(System.currentTimeMillis());
			long lasttime = System.currentTimeMillis();
			while(!SeqT.isInterrupted() && frame < length) {
				//System.out.println(frame);
				for (ObjProperties objekt : objEigensch) {
					objekt.playFrame(frame);
				}
				while(System.currentTimeMillis() - lasttime < msPerFrame && System.currentTimeMillis() - lasttime >= 0) {
					
				}
				lasttime = System.currentTimeMillis();
				frame++;
			}
			System.out.println(System.currentTimeMillis());
			for (ObjProperties props : objEigensch) {
				Gui.INSTANCE.delNodeList.add(props.getObj());
			}
		}
	};
	
	DeleteHandler DeleteCallback = new DeleteHandler() {
		@Override
		public void deleteElem(String objName) {
			System.out.println("HAHA No deleting here!");
		}
	};
	
	public Sequenz(ArrayList<String> cmds, String Id) {
		objEigensch = new ArrayList<ObjProperties>();
		seqName = Id;
		for (String cmd : cmds) {
			parseStrCmd(cmd);
			//System.out.println("SeqCreate: " + cmd);
		}
		for (ObjProperties objekt : objEigensch) {
			objekt.updateInperpolate();
		}
	}
	
	public void editSeq(String cmd) {
		parseStrCmd(cmd);
		for (ObjProperties objekt : objEigensch) {
			objekt.updateInperpolate();
		}
	}
	
	private void parseStrCmd(String cmd) {
		if (NodeCreator.validateCmd(cmd)) {
			Node nodeNeu = NodeCreator.cmdToNode(cmd, DeleteCallback);
			ObjProperties propsNeu = new ObjProperties(nodeNeu); 
			objEigensch.add(propsNeu);
		} else {
			String[] cmdTeil = cmd.split(";");
			switch(cmdTeil[0]) {
			case "frame":
				parseFrame(cmdTeil);
				break;
			case "delFrame":
				parseDel(cmdTeil);
				break;
			case "clear":
				parseClear(cmdTeil);
				break;
			case "delProp":
				parseDelProp(cmdTeil);
				break;
			case "fps":
				parseFps(cmdTeil);
				break;
			case "setDmx":
				parseDmx(cmdTeil);
				break;
			default:
				break;
			}
		}
	}

	private void parseDmx(String[] cmdTeil) {
		try {
			dmxNum = Integer.parseInt(cmdTeil[1]);
		} catch (NumberFormatException e) {
			System.err.println("Konnte DMX-Nummer nicht in Zahl umwandeln.");
		}
	}
	
	public int getDmxNum() {
		return dmxNum;
	}

	private void parseFps(String[] cmdTeil) {
		try {
			msPerFrame = (int) (1000.0/Double.parseDouble(cmdTeil[1]));
			System.out.println("Neue msPerFrame: " + msPerFrame);
		} catch (NumberFormatException e) {
			System.err.println("KEine g�ltige FPS-Zahl");
		}
	}

	private void parseDelProp(String[] cmdTeil) {
		try {
			String objName = "";
			String propName = "";
			if (cmdTeil.length >= 3) {
				objName = cmdTeil[1];
				propName = cmdTeil[2];
			}
			for (ObjProperties objekt : objEigensch) {
				if (objekt.getName().equals(objName)) {
					objekt.delProperty(propName);
					objekt.updateInperpolate();
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("No Number");
		}
	}
	
	private void parseClear(String[] cmdTeil) {
		try {
			String objName = "";
			String propName = "";
			if (cmdTeil.length >= 3) {
				objName = cmdTeil[1];
				propName = cmdTeil[2];
			}
			for (ObjProperties objekt : objEigensch) {
				if (objekt.getName().equals(objName)) {
					objekt.clearFrames(propName);
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("No Number");
		}
	}

	private void parseDel(String[] cmdTeil) {
		try {
			String objName = "";
			String propName = "";
			int frameNo = 0;
			if (cmdTeil.length >= 4) {
				objName = cmdTeil[1];
				propName = cmdTeil[2];
				frameNo = Integer.parseInt(cmdTeil[3]);
			}
			for (ObjProperties objekt : objEigensch) {
				if (objekt.getName().equals(objName)) {
					objekt.delFrame(propName, frameNo);
					objekt.updateInperpolate();
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("No Number");
		}
	}

	private void parseFrame(String[] cmdTeil) {
		try {
			String objName = "";
			String propName = "";
			int frameNo = 0;
			double wert = 0;
			int interpolMode = 1;
			if (cmdTeil.length >= 5) {
				objName = cmdTeil[1];
				propName = cmdTeil[2];
				frameNo = Integer.parseInt(cmdTeil[3]);
				wert = Double.parseDouble(cmdTeil[4]);
			}
			if (cmdTeil.length >= 6) {
				interpolMode = Integer.parseInt(cmdTeil[5]);
			}
			for (ObjProperties objekt : objEigensch) {
				if (objekt.getName().equals(objName)) {
					objekt.addProperty(propName, frameNo, wert, interpolMode);
					objekt.updateInperpolate();
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("No Number");
		}
	}

	public void play(int FrameNo) {
		frame = FrameNo;
		SeqT = new Thread(play);
		SeqT.setDaemon(true);
		SeqT.setName("SequenzThread");
		SeqT.start();
	}
	
	public void stop() {
		if (SeqT != null) {
			SeqT.interrupt();
			while (SeqT.isAlive()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			SeqT = null;
		}
	}
	
	public boolean isActive() {
		if (SeqT != null) {
			return true;
		}
		return false;
	}
	
	public String getName() {
		return seqName;
	}
	
	@Override
	public String toString() {
		String erg ="Sequence " + getName() + ":\n";
		for (ObjProperties objekt : objEigensch) {
			erg += objekt.toString();
		}
		return erg;
	}
	
	public ArrayList<Node> getNodes() {
		ArrayList<Node> elem = new ArrayList<Node>();
		for (ObjProperties objekt : objEigensch) {
			elem.add(objekt.getObj());
		}
		return elem;
	}
	
	public ArrayList<String> getCmds() {
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add("sequence;" + getName());
		for (ObjProperties objekt : objEigensch) {
			cmds.add(NodeCreator.nodeToString(objekt.getObj()));
		}
		for (ObjProperties objekt : objEigensch) {
			cmds.addAll(objekt.getFramesStr());
		}
		cmds.add("SeqEnd");
		return cmds;
	}
}
