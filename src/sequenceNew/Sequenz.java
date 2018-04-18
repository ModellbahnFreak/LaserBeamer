package sequenceNew;

import java.util.ArrayList;

import javafx.scene.Node;
import sequence.Cue;
import sequence.DeleteHandler;
import sequence.NodeCreator;
import server.Gui;

public class Sequenz {
	int length = 10;
	private ArrayList<ObjProperties> objEigensch;
	private int msPerFrame = (int)(1000.0/25.0);
	//									  Framerate
	private Thread SeqT = null;
	private boolean TreadActive = false;
	private String seqName = "";
	
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
			int frame = 0;
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
	
	public Sequenz(ArrayList<String> cmds, String Id) {
		objEigensch = new ArrayList<ObjProperties>();
		DeleteHandler DeleteCallback = new DeleteHandler() {
			@Override
			public void deleteElem(String objName) {
				System.out.println("HAHA No deleting here!");
			}
		};
		seqName = Id;
		for (String cmd : cmds) {
			System.out.println("SeqCreate: " + cmd);
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
				}
			}
		}
		for (ObjProperties objekt : objEigensch) {
			objekt.updateInperpolate();
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
			System.out.println("No Number");
		}
	}
	
	private void parseClear(String[] cmdTeil) {
		try {
			String objName = "";
			String propName = "";
			int frameNo = 0;
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
			System.out.println("No Number");
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
			System.out.println("No Number");
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
			} else if (cmdTeil.length >= 6) {
				interpolMode = Integer.parseInt(cmdTeil[5]);
			}
			for (ObjProperties objekt : objEigensch) {
				if (objekt.getName().equals(objName)) {
					objekt.addProperty(propName, frameNo, wert, interpolMode);
					objekt.updateInperpolate();
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("No Number");
		}
	}

	public void play() {
		SeqT = new Thread(play);
		SeqT.setDaemon(true);
		SeqT.setName("SequenzThread");
		SeqT.start();
	}
	
	public void stop() {
		if (SeqT != null) {
			SeqT.interrupt();
			SeqT = null;
		}
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
}
