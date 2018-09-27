package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import sequence.DeleteHandler;
import sequence.NodeCreator;
import sequenceNew.Sequenz;

public class NetwProcess extends Thread {
	private final Queue<String> _recvData;
	private final Queue<String> _sendData;
	private ArrayList<Node> elem = new ArrayList<Node>();
	private ArrayList<Sequenz> sequenzen = new ArrayList<Sequenz>();
	private boolean SeqInputAct = false;
	private ArrayList<String> SeqCmds = null;
	private String NewSeqName = "";
	//private Node BlackoutObj;
	//private int _blackoutState = 0; // 0: nicht erstellt, 1: erstellt aber nicht hinzugefügt, 2: erstellt
									// hinzugefügt unsichtbar, 3: black aktiv

	private final DeleteHandler DeleteCallback = new DeleteHandler() {
		@Override
		public void deleteElem(String objName) {
			delete(objName);
		}
	};

	public NetwProcess(Queue<String> recvData, Queue<String> sendData) {
		_recvData = recvData;
		_sendData = sendData;
		setDaemon(true);
	}

	@Override
	public void run() {
		System.out.println("Verarb waret");
		String commText = null;
		while (!Thread.currentThread().isInterrupted()) {
			synchronized (_recvData) {
				commText = _recvData.poll();
			}
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
					case "sequence":
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
					default:
						/*if (SeqInputAct == true) {
							createSequence(commText);
						} else {*/
						try {
							Node nodeNeu = NodeCreator.cmdToNode(commText, DeleteCallback);
							if (nodeNeu != null) {
								synchronized (_sendData) {
									_sendData.add("201:" + nodeNeu.getId());
									_sendData.notifyAll();
									System.out.println("201: Created " + nodeNeu.getId());
								}
								elem.add(nodeNeu);
								Gui.INSTANCE.addNodeList.add(elem.get(elem.indexOf(nodeNeu)));
								System.out.println("Command created: " + NodeCreator.nodeToString(nodeNeu));
							}
						} catch (Exception e) {
							System.err.println("Fehler beim Erstellen des GUI Objekts");
							e.printStackTrace();
						}
						break;
					}
					// Gui.INSTANCE.addNodeList.add(elem.get(elem.indexOf(mediaView)));
				} else {
					synchronized (_recvData) {
						try {
							_recvData.wait(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void editSeq(String seqName) {
		Sequenz seqEdit = null;
		for (Sequenz seq : sequenzen) {
			if (seq.getName().equals(seqName)) {
				seqEdit = seq;
			}
		}
		if (seqEdit != null) {
			synchronized (_sendData) {
				_sendData.add("202:End with 'SeqEnd'");
				_sendData.notifyAll();
				System.out.println("202: Sequence Input starting");
			}
			String cmdText = _recvData.poll();
			while(!"SeqEnd".equals(cmdText) && !"connection;close".equals(cmdText)) {
				if (cmdText != null) {
					seqEdit.editSeq(cmdText);
				} else { 
					synchronized (_recvData) {
						try {
							_recvData.wait(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				cmdText = _recvData.poll();
				System.out.println(cmdText);
			}
			if ("SeqEnd".equals(cmdText)) {
				closeConnection();
			}
		} else {
			synchronized (_sendData) {
				_sendData.add("321:");
				_sendData.notifyAll();
				_sendData.add("101:");
				_sendData.notifyAll();
				System.out.println("321, 101: No Sequence name given");
			}
		}
	}

	private void deleteSeq(String objName) {
		System.out.println(objName);
		if ("all".equals(objName)) {
			sequenzen.clear();
			synchronized (_sendData) {
				_sendData.add("321:");
				_sendData.notifyAll();
				System.out.println("Deleted all");
			}
		} else {
			ArrayList<Sequenz> delSeq = new ArrayList<Sequenz>();
			for (Sequenz seq : sequenzen) {
				if (seq.getName().equals(objName)) {
					delSeq.add(seq);
					synchronized (_sendData) {
						_sendData.add("322:" + objName);
						_sendData.notifyAll();
						System.out.println("322: Deleted " + objName);
					}
				}
			}
			for (Sequenz del : delSeq) {
				sequenzen.remove(del);
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
						sequenzen.add(new Sequenz(cmds, seqName));
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
			for (Sequenz seq : sequenzen) {
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
			for (Node obj : elem) {
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
			for (Node obj : elem) {
				if (obj.getId().equals(objName)) {
					if (obj instanceof ImageView) {
						((ImageView) obj).setFitHeight(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof MediaView) {
						((MediaView) obj).setFitHeight(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof Rectangle) {
						((Rectangle) obj).setHeight(Double.parseDouble(value) * Gui.INSTANCE.scene.getHeight());
					} else if (obj instanceof Circle) {
						((Circle) obj).setRadius((Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth())/2.0);
					} else if (obj instanceof Line) {
						((Line) obj).setEndY(((Line) obj).getStartY() + (Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth()));
					}
				}
			}
		} catch (NumberFormatException e) {
			synchronized (_sendData) {
				_sendData.add("102:");
				_sendData.notifyAll();
				System.err.println("102: Wrong number format");
			}
		}
	}

	private void setWidth(String objName, String value) {
		try {
			for (Node obj : elem) {
				if (obj.getId().equals(objName)) {
					if (obj instanceof ImageView) {
						((ImageView) obj).setFitWidth(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof MediaView) {
						((MediaView) obj).setFitWidth(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Rectangle) {
						((Rectangle) obj).setWidth(Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth());
					} else if (obj instanceof Circle) {
						((Circle) obj).setRadius((Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth())/2.0);
					} else if (obj instanceof Line) {
						((Line) obj).setEndX(((Line) obj).getStartX() + (Double.parseDouble(value) * Gui.INSTANCE.scene.getWidth()));
					}
				}
			}
		} catch (NumberFormatException e) {
			synchronized (_sendData) {
				_sendData.add("102:");
				_sendData.notifyAll();
				System.err.println("102: Wrong number format");
			}
		}
	}

	private void setYPos(String objName, String value) {
		try {
			for (Node obj : elem) {
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
		} catch (NumberFormatException e) {
			synchronized (_sendData) {
				_sendData.add("102:");
				_sendData.notifyAll();
				System.err.println("102: Wrong number format");
			}
		}
	}

	private void setXPos(String objName, String value) {
		try {
			for (Node obj : elem) {
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
		} catch (NumberFormatException e) {
			synchronized (_sendData) {
				_sendData.add("102:");
				_sendData.notifyAll();
				System.err.println("102: Wrong number format");
			}
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
			// Noch nicht hinzugefügt
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
				_sendData.add("304:1");
				_sendData.notifyAll();
				System.out.println("304: Enabled blackout");
			}
			_blackoutState = 3;
		}
		if (!("1".equals(status)) && _blackoutState == 3) {
			// Angezeigt
			BlackoutObj.setOpacity(0);
			// BlackoutObj.toBack();
			synchronized (_sendData) {
				_sendData.add("304:0");
				_sendData.notifyAll();
				System.out.println("232: Disabled blackout");
			}
			_blackoutState = 2;
		}*/
		try {
			double Helligk = Double.parseDouble(status);
			Gui.INSTANCE.root.setOpacity(Helligk);
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
				synchronized (_sendData) {
					_sendData.add("231:");
					_sendData.notifyAll();
					System.out.println("231: Enabled screenshot");
				}
			} else {
				Server.einst.setStateScreenshActive(false);
				Server.StopLivestream();
				synchronized (_sendData) {
					_sendData.add("232:");
					_sendData.notifyAll();
					System.out.println("232: Disabled screenshot");
				}
			}
			break;
		case "refresh":
			refreshList(toDos[2]);
		}
	}

	private void refreshList(String listType) {
		switch (listType) {
		case "video":
			File vidOrdner = new File(Server.einst.getHomeDir() + "/content");
			if (vidOrdner.exists()) {
				ArrayList<File> dateien = new ArrayList<File>(Arrays.asList(vidOrdner.listFiles()));
				synchronized (_sendData) {
					_sendData.add("421:");
					_sendData.notifyAll();
					System.out.println("421: Video list start");
					for (File f : dateien) {
						String endung = getExtension(f.getName()).toLowerCase();
						// System.out.println(endung);
						switch (endung) {
						case "mp4":
							_sendData.add(f.getPath());
							break;
						case "mp3":
							_sendData.add(f.getPath());
							break;
						case "aif":
							_sendData.add(f.getPath());
							break;
						case "aiff":
							_sendData.add(f.getPath());
							break;
						case "wav":
							_sendData.add(f.getPath());
							break;
						case "fxm":
							_sendData.add(f.getPath());
							break;
						}
						_sendData.notifyAll();
					}
					_sendData.add("422:");
					_sendData.notifyAll();
					System.out.println("422: Video list end");
				}
			} else {
				synchronized (_sendData) {
					_sendData.add("311:");
					_sendData.notifyAll();
					System.out.println("311: content folder not found");
				}
			}
			break;
		case "img":
			File imgOrdner = new File(Server.einst.getHomeDir() + "/content");
			if (imgOrdner.exists()) {
				ArrayList<File> dateien = new ArrayList<File>(Arrays.asList(imgOrdner.listFiles()));
				synchronized (_sendData) {
					_sendData.add("423:");
					System.out.println("423: Img list start");
					for (File f : dateien) {
						String endung = getExtension(f.getName()).toLowerCase();
						// System.out.println(endung);
						switch (endung) {
						case "jpg":
							_sendData.add(f.getPath());
							break;
						case "jpeg":
							_sendData.add(f.getPath());
							break;
						case "png":
							_sendData.add(f.getPath());
							break;
						case "gif":
							_sendData.add(f.getPath());
							break;
						case "bmp":
							_sendData.add(f.getPath());
							break;
						case "fxm":
							_sendData.add(f.getPath());
							break;
						}
						_sendData.notifyAll();
					}
					_sendData.add("424:");
					_sendData.notifyAll();
					System.out.println("424: Img list end");
				}
			} else {
				synchronized (_sendData) {
					_sendData.add("311:");
					_sendData.notifyAll();
					System.out.println("311: content folder not found");
				}
			}
			break;
		case "objs":
			synchronized (_sendData) {
				_sendData.add("425:");
				_sendData.notifyAll();
				System.out.println("311: Obj list start");
				for (Node obj : elem) {
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
					_sendData.add(elemName);
					_sendData.notifyAll();
				}
				_sendData.add("426:");
				_sendData.notifyAll();
				System.out.println("426: Obj list end");
			}
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
			synchronized (_sendData) {
				_sendData.add("203:" + NewSeqName);
				_sendData.notifyAll();
				System.out.println("203: Ended sequence input");
			}
			sequenzen.add(new Sequenz(SeqCmds, NewSeqName));
			SeqCmds = null;
		} else {
			synchronized (_sendData) {
				_sendData.add("103:");
				_sendData.notifyAll();
				_sendData.add("321:");
				_sendData.notifyAll();
				System.out.println("103, 321: No Sequence active");
			}
		}
	}

	private void startSeqInput(String[] toDos) {
		if (SeqInputAct != true) {
			if (toDos.length == 2 && !toDos[1].isEmpty()) {
				SeqInputAct = true;
				NewSeqName = toDos[1];
				synchronized (_sendData) {
					_sendData.add("202:End with 'SeqEnd'");
					_sendData.notifyAll();
					System.out.println("202: Sequence Input starting");
				}
				SeqCmds = new ArrayList<String>();
				String cmdText = _recvData.poll();
				while(!"SeqEnd".equals(cmdText) && !"connection;close".equals(cmdText)) {
					if (cmdText != null) {
						SeqCmds.add(cmdText);
					}
					cmdText = _recvData.poll();
				}
				if ("SeqEnd".equals(cmdText)) {
					sequenceEnd();
				} else {
					closeConnection();
				}
			} else {
				synchronized (_sendData) {
					_sendData.add("321:");
					_sendData.notifyAll();
					_sendData.add("101:");
					_sendData.notifyAll();
					System.out.println("321, 101: No Sequence name given");
				}
			}
		} else {
			synchronized (_sendData) {
				_sendData.add("103:");
				_sendData.notifyAll();
				System.out.println("103: Sequence Input already active");
			}
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
			for (Sequenz seq : sequenzen) {
				if (seq.getName().equals(seqName)) {
					seq.play(startFrame);
					System.out.println("Launched");
					synchronized (_sendData) {
						_sendData.add("212:" + toDos[1]);
						_sendData.notifyAll();
						System.out.println("212: Started Sequence " + toDos[1]);
					}
				}
			}
		} catch (NumberFormatException e) {
			
		}
	}
	
	private void stopSequence(String[] toDos) {
		System.out.println("Stopping seq");
		for (Sequenz seq : sequenzen) {
			if (seq.getName().equals(toDos[1])) {
				seq.stop();
				System.out.println("Stopped");
				synchronized (_sendData) {
					_sendData.add("213:" + toDos[1]);
					_sendData.notifyAll();
					System.out.println("213: Stopping Sequence " + toDos[1]);
				}
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
			for (Node obj : elem) {
				Gui.INSTANCE.delNodeList.add(obj);
			}
			elem.clear();
			synchronized (_sendData) {
				_sendData.add("321:");
				_sendData.notifyAll();
				System.out.println("Deleted all");
			}
		} else {
			ArrayList<Node> delNum = new ArrayList<Node>();
			for (Node obj : elem) {
				if (obj.getId().equals(objName)) {
					Gui.INSTANCE.delNodeList.add(obj);
					delNum.add(obj);
					synchronized (_sendData) {
						_sendData.add("322:" + objName);
						_sendData.notifyAll();
						System.out.println("322: Deleted " + objName);
					}
				}
			}
			for (Node obj : delNum) {
				elem.remove(obj);
			}
		}
	}
}
