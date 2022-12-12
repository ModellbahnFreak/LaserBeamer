package de.modellbahnfreak.laserbeamer.webserver.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import de.modellbahnfreak.laserbeamer.sequence.DeleteHandler;
import de.modellbahnfreak.laserbeamer.sequence.NodeCreator;
import de.modellbahnfreak.laserbeamer.sequenceNew.Sequenz;
import de.modellbahnfreak.laserbeamer.server.Gui;
import de.modellbahnfreak.laserbeamer.server.Server;
import de.modellbahnfreak.laserbeamer.webserver.http.ProcessCallback;
import de.modellbahnfreak.laserbeamer.webserver.ws.SocketSendReceive;

public class SocketProcess implements Runnable {

	private final SocketSendReceive _sendRecv;
	private final ArrayList<Node> _elem;
	private final ArrayList<Sequenz> _sequenzen;
	private boolean SeqInputAct = false;
	private ArrayList<String> SeqCmds = null;
	private String NewSeqName = "";
	private final ProcessCallback processCallback;

	private final DeleteHandler DeleteCallback = new DeleteHandler() {
		@Override
		public void deleteElem(String objName) {
			delete(objName);
		}
	};

	public SocketProcess(SocketSendReceive sendRecv, ArrayList<Node> elem, ArrayList<Sequenz> sequenzen, ProcessCallback procCallb) {
		_sendRecv = sendRecv;
		_elem = elem;
		_sequenzen = sequenzen;
		processCallback = procCallb;
	}

	@Override
	public void run() {
		System.out.println("Verarb waret");
		String commText = null;
		while (!Thread.currentThread().isInterrupted()) {
			commText = _sendRecv.popRecvBlocking();
			try {
				if (commText != null) {
					String[] ToDos = commText.split(";");
					System.out.println("Process cmd: " + commText);
					switch (ToDos[0]) {
					/*
					 * case "img": createImg(ToDos); break; case "vid": createVid(ToDos); break;
					 * case "txt": showText(ToDos, commText); System.out.println("New Text"); break;
					 */
					case "del":
						delete(ToDos[1]);
						break;
					case "de/modellbahnfreak/laserbeamer/sequence":
						startSeqInput(ToDos);
						break;
					case "connection":
						if ("close".equals(ToDos[1])) {
							closeConnection();
						}
						break;
					/*case "SeqEnd":
						sequenceEnd();
						break;*/
					case "playSeq":
						playSequence(ToDos);
						break;
					case "stopSeq":
						stopSequence(ToDos);
						break;
					case "xPos":
						setXPos(ToDos[1], ToDos[2]);
						break;
					case "yPos":
						setYPos(ToDos[1], ToDos[2]);
						break;
					case "width":
						setWidth(ToDos[1], ToDos[2]);
						break;
					case "height":
						setHeight(ToDos[1], ToDos[2]);
						break;
					case "blackout":
						blackout(ToDos[1]);
						break;
					case "quit":
						System.exit(1);
						break;
					case "system":
						editPreferences(ToDos);
						break;
					case "player":
						togglePlayer(ToDos);
						break;
					case "saveSeq":
						saveSequence(ToDos);
						break;
					case "loadSeq":
						loadSequence(ToDos);
						break;
					case "delSeq":
						deleteSeq(ToDos[1]);
						break;
					case "editSeq":
						editSeq(ToDos[1]);
						break;
					case "filename":
						setNewFilename(ToDos[1], ToDos[2]);
						break;
					case "settings":
						cangeSettings();
						break;
					default:
						/*if (SeqInputAct == true) {
							createSequence(commText);
						} else {*/
						try {
							Node nodeNeu = NodeCreator.cmdToNode(commText, DeleteCallback);
							if (nodeNeu != null) {
								//_sendRecv.send("201:" + nodeNeu.getId());
								processCallback.sendAll("201:" + NodeCreator.nodeToString(nodeNeu));
								System.out.println("201: Created " + nodeNeu.getId());
								_elem.add(nodeNeu);
								Gui.INSTANCE.addNodeList.add(nodeNeu);
								System.out.println("Command created: " + NodeCreator.nodeToString(nodeNeu));
							}
						} catch (Exception e) {
							System.err.println("Fehler beim Erstellen des GUI Objekts");
							e.printStackTrace();
						}
						break;
					}
					// Gui.INSTANCE.addNodeList.add(elem.get(elem.indexOf(mediaView)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void cangeSettings() {
		// TODO Auto-generated method stub
		
	}

	private void setNewFilename(String objName, String filename) {
		//filename;obj;Filename
		try {
			URL pfad = null;
			File datei = new File(filename); 
			if (!datei.exists()) {
				_sendRecv.send("102:");
				System.err.println("102: File doesn't exist");
				return;
			}
			pfad = datei.toURI().toURL();
			// System.out.println(pfad.toString());
			for (Node obj : _elem) {
				if (obj.getId().equals(objName)) {
					if (obj instanceof ImageView) {
						Image Bild = new Image(pfad.toString());
						((ImageView) obj).setImage(Bild);
						((ImageView) obj).getProperties().put("path", datei.toString());
					} else if (obj instanceof MediaView) {
						Media m = new Media(pfad.toURI().toString());
						MediaPlayer player = new MediaPlayer(m);
						((MediaView) obj).setMediaPlayer(player);
						((MediaView) obj).getProperties().put("path", datei.toString());
					}
				}
			}
		} catch (URISyntaxException e) {
			_sendRecv.send("102:");
			System.err.println("102: File open error");
		} catch (MalformedURLException e) {
			_sendRecv.send("102:");
			System.err.println("102: File open error");
		}
	}

	private void editSeq(String seqName) {
		Sequenz seqEdit = null;
		for (Sequenz seq : _sequenzen) {
			if (seq.getName().equals(seqName)) {
				seqEdit = seq;
			}
		}
		if (seqEdit != null) {
			_sendRecv.send("202:End with 'SeqEnd'");
			System.out.println("202: Sequence Input starting");
			String cmdText = _sendRecv.popRecvBlocking();
			while(!"SeqEnd".equals(cmdText) && !"connection;close".equals(cmdText)) {
				if (cmdText != null) {
					seqEdit.editSeq(cmdText);
				}
				cmdText = _sendRecv.popRecvBlocking();
				System.out.println(cmdText);
			}
			if ("SeqEnd".equals(cmdText)) {
				closeConnection();
			}
		} else {
			_sendRecv.send("321:");
			_sendRecv.send("101:");
			System.out.println("321, 101: No Sequence name given");
		}
	}

	private void deleteSeq(String objName) {
		System.out.println(objName);
		if ("all".equals(objName)) {
			_sequenzen.clear();
			//_sendRecv.send("321:");
			processCallback.sendAll("321:");
			System.out.println("Deleted all");
		} else {
			ArrayList<Sequenz> delSeq = new ArrayList<Sequenz>();
			for (Sequenz seq : _sequenzen) {
				if (seq.getName().equals(objName)) {
					delSeq.add(seq);
					//_sendRecv.send("322:" + objName);
					processCallback.sendAll("322:" + objName);
					System.out.println("322: Deleted " + objName);
				}
			}
			for (Sequenz del : delSeq) {
				_sequenzen.remove(del);
			}
		}
	}

	private void loadSequence(String[] toDos) {
		if (toDos.length == 2) {
			try {
				if (new File(toDos[1]).exists()) {
					BufferedReader readSeq = new BufferedReader(new FileReader(toDos[1]));
					String zeile = null;
					ArrayList<String> cmds = new ArrayList<String>();
					zeile = readSeq.readLine();
					if (zeile != null && zeile.startsWith("sequence;")) {
						String seqName = zeile.split(";")[1];
						while ((zeile = readSeq.readLine()) != null) {
							if ("SeqEnd".equals(zeile)) {
								
							} else {
								cmds.add(zeile);
							}
						}
						readSeq.close();
						_sequenzen.add(new Sequenz(cmds, seqName));
					} else {
						System.err.println("Startet nicht mit 'sequence'");
						readSeq.close();
					}
				} else {
					System.err.println("Datei nicht vorhanden");
				}
			} catch (IOException e) {
				System.err.println("Fehler beim lesen der Sequence");
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Fehler beim deserialisieren der Sequence");
				e.printStackTrace();
			}
		}
	}

	private void saveSequence(String[] toDos) {
		if (toDos.length == 3) {
			for (Sequenz seq : _sequenzen) {
				if (seq.getName().equals(toDos[1])) {
					try {
						/*ObjectOutputStream seqOut = new ObjectOutputStream(new FileOutputStream(toDos[2]));
						seqOut.writeObject(seq);
						seqOut.flush();
						seqOut.close();*/
						PrintWriter writeSeq = new PrintWriter(toDos[2], "UTF-8");
						for(String cmd : seq.getCmds()) {
							writeSeq.println(cmd);
						}
						writeSeq.flush();
						writeSeq.close();
					} catch (IOException e) {
						System.err.println("Fehler beim speichern der Sequence");
						e.printStackTrace();
					} catch (Exception e) {
						System.err.println("Fehler beim serialisieren der Sequence");
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void togglePlayer(String[] toDos) {
		if (toDos.length == 3) {
			for (Node obj : _elem) {
				if (obj.getId().equals(toDos[1]) && obj instanceof MediaView) {
					switch (toDos[2]) {
					case "play":
						((MediaView)obj).getMediaPlayer().play();
						break;
					case "pause":
						((MediaView)obj).getMediaPlayer().pause();
						break;
					case "stop":
						((MediaView)obj).getMediaPlayer().stop();
						break;
					}
				}
				
			}
		}
	}

	private void setHeight(String objName, String value) {
		try {
			for (Node obj : _elem) {
				if (obj.getId().equals(objName)) {
					if (obj instanceof ImageView) {
						((ImageView) obj).setFitHeight(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof MediaView) {
						((MediaView) obj).setFitHeight(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof Rectangle) {
						((Rectangle) obj).setHeight(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof Circle) {
						((Circle) obj).setRadius((Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth()));
					} else if (obj instanceof Line) {
						((Line) obj).setEndY(((Line) obj).getStartY() + (Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth()));
					}
				}
			}
			processCallback.sendAll("205:height;"+objName+";"+Double.parseDouble(value));
		} catch (NumberFormatException e) {
			_sendRecv.send("102:");
			System.err.println("102: Wrong number format");
		}
	}

	private void setWidth(String objName, String value) {
		try {
			for (Node obj : _elem) {
				if (obj.getId().equals(objName)) {
					if (obj instanceof ImageView) {
						((ImageView) obj).setFitWidth(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof MediaView) {
						((MediaView) obj).setFitWidth(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Rectangle) {
						((Rectangle) obj).setWidth(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Circle) {
						((Circle) obj).setRadius((Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth()));
					} else if (obj instanceof Line) {
						((Line) obj).setEndX(((Line) obj).getStartX() + (Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth()));
					}
				}
			}
			processCallback.sendAll("205:width;"+objName+";"+Double.parseDouble(value));
		} catch (NumberFormatException e) {
			_sendRecv.send("102:");
			System.err.println("102: Wrong number format");
		}
	}

	private void setYPos(String objName, String value) {
		try {
			for (Node obj : _elem) {
				if (obj.getId().equals(objName)) {
					if (obj instanceof ImageView) {
						((ImageView) obj).setY(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof MediaView) {
						((MediaView) obj).setY(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof Rectangle) {
						((Rectangle) obj).setY(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof Text) {
						((Text) obj).setY(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Circle) {
						((Circle) obj).setCenterY(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Line) {
						((Line) obj).setStartY(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					}
				}
			}
			processCallback.sendAll("205:yPos;"+objName+";"+Double.parseDouble(value));
		} catch (NumberFormatException e) {
			_sendRecv.send("102:");
			System.err.println("102: Wrong number format");
		}
	}

	private void setXPos(String objName, String value) {
		try {
			for (Node obj : _elem) {
				if (obj.getId().equals(objName)) {
					if (obj instanceof ImageView) {
						((ImageView) obj).setX(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof MediaView) {
						((MediaView) obj).setX(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Rectangle) {
						((Rectangle) obj).setX(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Text) {
						((Text) obj).setX(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Circle) {
						((Circle) obj).setCenterX(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Line) {
						((Line) obj).setStartX(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					}
				}
			}
			processCallback.sendAll("205:xPos;"+objName+";"+Double.parseDouble(value));
		} catch (NumberFormatException e) {
			_sendRecv.send("102:");
			System.err.println("102: Wrong number format");
		}
	}

	private void blackout(String status) {
		/*if (_blackoutState == 0) {
			// Noch nicht erstellt
			Rectangle BORect = new Rectangle(0, 0);
			BORect.setWidth(Gui.INSTANCE.scene.getWidth());
			BORect.setHeight(Gui.INSTANCE.scene.getHeight());
			BORect.setFill(Color.BLACK);
			BORect.setStrokeWidth(0);
			BORect.setId("blackout");
			BlackoutObj = BORect;
			_blackoutState = 1;
		}
		if (_blackoutState == 1) {
			// Noch nicht hinzugef�gt
			Gui.INSTANCE.addNodeList.add(BlackoutObj);
			_blackoutState = 2;
		}
		if ("1".equals(status) && _blackoutState == 2) {
			// Noch nicht angezeigt
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					BlackoutObj.toFront();
				}
			});
			((Rectangle) BlackoutObj).setX(0);
			((Rectangle) BlackoutObj).setY(0);
			((Rectangle) BlackoutObj).setWidth(Gui.INSTANCE.scene.getWidth());
			((Rectangle) BlackoutObj).setHeight(Gui.INSTANCE.scene.getHeight());
			BlackoutObj.setOpacity(1);
			synchronized (_sendData) {
				_sendData.add("204:1");
				_sendData.notifyAll();
				System.out.println("204: Enabled blackout");
			}
			_blackoutState = 3;
		}
		if (!("1".equals(status)) && _blackoutState == 3) {
			// Angezeigt
			BlackoutObj.setOpacity(0);
			// BlackoutObj.toBack();
			synchronized (_sendData) {
				_sendData.add("204:0");
				_sendData.notifyAll();
				System.out.println("232: Disabled blackout");
			}
			_blackoutState = 2;
		}*/
		try {
			double Helligk = Double.parseDouble(status);
			Gui.INSTANCE.root.setOpacity(Helligk);
			processCallback.sendAll("204:"+Helligk);
		} catch (Exception e) {
			System.err.println("Keine Zahl");
			Gui.INSTANCE.root.setOpacity(1);
		}
	}

	private void editPreferences(String[] toDos) {
		switch (toDos[1]) {
		case "screenshot":
			if ("1".equals(toDos[2])) {
				Server.einst.setStateScreenshActive(true);
				Server.StartLivestream();
				_sendRecv.send("231:");
				System.out.println("231: Enabled screenshot");
			} else {
				Server.einst.setStateScreenshActive(false);
				Server.StopLivestream();
				_sendRecv.send("232:");
				System.out.println("232: Disabled screenshot");
			}
			break;
		case "refresh":
			refreshList(toDos[2]);
			break;
		case "screen":
			getScreenParam(toDos[2]);
			break;
		case "cmds":
			getCommands();
			break;
		}
	}

	private void getCommands() {
		_sendRecv.send("243:"+NodeCreator.getAllObjTypes());
	}

	private void getScreenParam(String action) {
		switch (action) {
		case "ratio": //Ratio=Height/Width
			_sendRecv.send("241:"+(Gui.INSTANCE.scene.getHeight()/Gui.INSTANCE.scene.getWidth()));
			break;
		case "size":
			_sendRecv.send("242:w:"+Gui.INSTANCE.scene.getWidth()+":h:"+Gui.INSTANCE.scene.getHeight());
			break;
		default:
			_sendRecv.send("242:w:"+Gui.INSTANCE.scene.getWidth()+":h:"+Gui.INSTANCE.scene.getHeight());
			break;
		}
	}

	private void refreshList(String listType) {
		switch (listType) {
		case "video":
			File vidOrdner = new File(Server.einst.getHomeDir() + "/content");
			if (vidOrdner.exists()) {
				ArrayList<File> dateien = new ArrayList<File>(Arrays.asList(vidOrdner.listFiles()));
				_sendRecv.send("421:");
				System.out.println("421: Video list start");
				for (File f : dateien) {
					String endung = getExtension(f.getName()).toLowerCase();
					// System.out.println(endung);
					switch (endung) {
					case "mp4":
						_sendRecv.send(f.getPath());
						break;
					case "mp3":
						_sendRecv.send(f.getPath());
						break;
					case "aif":
						_sendRecv.send(f.getPath());
						break;
					case "aiff":
						_sendRecv.send(f.getPath());
						break;
					case "wav":
						_sendRecv.send(f.getPath());
						break;
					case "fxm":
						_sendRecv.send(f.getPath());
						break;
					}
				}
				_sendRecv.send("422:");
				System.out.println("422: Video list end");
			} else {
				_sendRecv.send("311:");
				System.out.println("311: content folder not found");
			}
			break;
		case "img":
			File imgOrdner = new File(Server.einst.getHomeDir() + "/content");
			if (imgOrdner.exists()) {
				ArrayList<File> dateien = new ArrayList<File>(Arrays.asList(imgOrdner.listFiles()));
				_sendRecv.send("423:");
				System.out.println("423: Img list start");
				for (File f : dateien) {
					String endung = getExtension(f.getName()).toLowerCase();
					// System.out.println(endung);
					switch (endung) {
					case "jpg":
						_sendRecv.send(f.getPath());
						break;
					case "jpeg":
						_sendRecv.send(f.getPath());
						break;
					case "png":
						_sendRecv.send(f.getPath());
						break;
					case "gif":
						_sendRecv.send(f.getPath());
						break;
					case "bmp":
						_sendRecv.send(f.getPath());
						break;
					case "fxm":
						_sendRecv.send(f.getPath());
						break;
					}
				}
				_sendRecv.send("424:");
				System.out.println("424: Img list end");
			} else {
				_sendRecv.send("311:");
				System.out.println("311: content folder not found");
			}
			break;
		case "objs":
			_sendRecv.send("425:");
			System.out.println("311: Obj list start");
			for (Node obj : _elem) {
				String elemName = "";
				elemName = NodeCreator.nodeToString(obj);
				/*if (obj instanceof Circle) {
					elemName += "Circle:";
				} else if (obj instanceof Rectangle) {
					elemName += "Rectangle:";
				} else if (obj instanceof Line) {
					elemName += "Line:";
				} else if (obj instanceof MediaView) {
					elemName += "MediaView:";
				} else if (obj instanceof ImageView) {
					elemName += "ImageView:";
				} else if (obj instanceof Text) {
					elemName += "Text:";
				}
				elemName += obj.getId();*/
				_sendRecv.send(elemName);
			}
			_sendRecv.send("426:");
			System.out.println("426: Obj list end");
			break;
		default:
			break;
		}
	}

	private String getExtension(String name) {
		int punktPos = name.lastIndexOf('.');
		if (punktPos >= 0) {
			return name.substring(punktPos + 1);
		} else {
			return null;
		}
	}
	
	private void sequenceEnd() {
		if (SeqInputAct == true) {
			SeqInputAct = false;
			//_sendRecv.send("203:" + NewSeqName);
			processCallback.sendAll("203:" + NewSeqName);
			System.out.println("203: Ended sequence input");
			_sequenzen.add(new Sequenz(SeqCmds, NewSeqName));
			SeqCmds = null;
		} else {
			_sendRecv.send("103:");
			_sendRecv.send("321:");
			System.out.println("103, 321: No Sequence active");
		}
	}

	private void startSeqInput(String[] toDos) {
		if (SeqInputAct != true) {
			if (toDos.length == 2 && !toDos[1].isEmpty()) {
				SeqInputAct = true;
				NewSeqName = toDos[1];
				_sendRecv.send("202:End with 'SeqEnd'");
				System.out.println("202: Sequence Input starting");
				SeqCmds = new ArrayList<String>();
				String cmdText = _sendRecv.popReceive();
				while(!"SeqEnd".equals(cmdText) && !"connection;close".equals(cmdText)) {
					if (cmdText != null) {
						SeqCmds.add(cmdText);
					}
					cmdText = _sendRecv.popReceive();
				}
				if ("SeqEnd".equals(cmdText)) {
					sequenceEnd();
				} else {
					closeConnection();
				}
			} else {
				_sendRecv.send("321:");
				_sendRecv.send("101:");
				System.out.println("321, 101: No Sequence name given");
			}
		} else {
			_sendRecv.send("103:");
			System.out.println("103: Sequence Input already active");
		}
	}

	private void playSequence(String[] toDos) {
		try {
			System.out.println("Starting");
			String seqName = "";
			int startFrame = 0;
			if (toDos.length >= 2) {
				seqName = toDos[1];
			}
			if (toDos.length >= 3) {
				startFrame = Integer.parseInt(toDos[2]);
			}
			for (Sequenz seq : _sequenzen) {
				if (seq.getName().equals(seqName)) {
					seq.play(startFrame);
					System.out.println("Launched");
					//_sendRecv.send("212:" + toDos[1]);
					processCallback.sendAll("212:" + toDos[1]);
					System.out.println("212: Started Sequence " + toDos[1]);
				}
			}
		} catch (NumberFormatException e) {
			
		}
	}
	
	private void stopSequence(String[] toDos) {
		System.out.println("Stopping seq");
		for (Sequenz seq : _sequenzen) {
			if (seq.getName().equals(toDos[1])) {
				seq.stop();
				System.out.println("Stopped");
				//_sendRecv.send("213:" + toDos[1]);
				processCallback.sendAll("213:" + toDos[1]);
				System.out.println("213: Stopping Sequence " + toDos[1]);
			}
		}
	}

	private void closeConnection() {
		// Copy of sequence end withoud send netw and no seq is added
		if (SeqInputAct == true) {
			SeqInputAct = false;
			System.out.println("Canceled sequence input");
			SeqCmds = null;
		} else {
			// System.out.println("No Sequence active");
		}
	}

	private void delete(String objName) {
		System.out.println(objName);
		if ("all".equals(objName)) {
			for (Node obj : _elem) {
				Gui.INSTANCE.delNodeList.add(obj);
			}
			_elem.clear();
			//_sendRecv.send("221:");
			processCallback.sendAll("221:");
			System.out.println("Deleted all");
		} else {
			ArrayList<Node> delNum = new ArrayList<Node>();
			for (Node obj : _elem) {
				if (obj.getId().equals(objName)) {
					Gui.INSTANCE.delNodeList.add(obj);
					delNum.add(obj);
					//_sendRecv.send("222:" + objName);
					processCallback.sendAll("222:" + objName);
					System.out.println("222: Deleted " + objName);
				}
			}
			for (Node obj : delNum) {
				_elem.remove(obj);
			}
		}
	}

}
