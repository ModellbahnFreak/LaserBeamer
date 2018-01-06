package server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sequence.DeleteHandler;
import sequence.NodeCreator;
import sequence.Sequence;

public class NetwProcess extends Thread {
	private final Queue<String> _comm;
	private final Queue<String> _SendComm;
	private ArrayList<Node> elem = new ArrayList<Node>();
	private ArrayList<Sequence> sequenzen = new ArrayList<Sequence>();
	private boolean SeqInputAct = false;
	private ArrayList<String> SeqCmds = null;
	private String NewSeqName = "";
	private Node BlackoutObj;
	private int _blackoutState = 0; // 0: nicht erstellt, 1: erstellt aber nicht hinzugefügt, 2: erstellt
									// hinzugefügt unsichtbar, 3: black aktiv

	private final DeleteHandler DeleteCallback = new DeleteHandler() {
		@Override
		public void deleteElem(String objName) {
			delete(objName);
		}
	};

	public NetwProcess(Queue<String> comm, Queue<String> SendComm) {
		_comm = comm;
		_SendComm = SendComm;
		setDaemon(true);
	}

	@Override
	public void run() {
		System.out.println("Verarb waret");
		while (true) {
			String commText = null;
			synchronized (_comm) {
				commText = _comm.poll();
			}
			if (commText != null) {
				String[] ToDos = commText.split(";");
				System.out.println("Received cmd: " + ToDos[0]);
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
				case "SeqEnd":
					sequenceEnd();
					break;
				case "playSeq":
					playSequence(ToDos);
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
				default:
					if (SeqInputAct == true) {
						createSequence(commText);
					} else {
						Node nodeNeu = NodeCreator.cmdToNode(commText, DeleteCallback);
						if (nodeNeu != null) {
							synchronized (_SendComm) {
								_SendComm.add("201:" + nodeNeu.getId());
								System.out.println("201: Created " + nodeNeu.getId());
							}
							elem.add(nodeNeu);
							Gui.INSTANCE.addNodeList.add(elem.get(elem.indexOf(nodeNeu)));
						}
					}
					break;
				}
				// Gui.INSTANCE.addNodeList.add(elem.get(elem.indexOf(mediaView)));
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
					}
				}
			}
		} catch (NumberFormatException e) {
			synchronized (_SendComm) {
				_SendComm.add("102:");
				System.out.println("102: Wrong number format");
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
					}
				}
			}
		} catch (NumberFormatException e) {
			synchronized (_SendComm) {
				_SendComm.add("102:");
				System.out.println("102: Wrong number format");
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
					}
				}
			}
		} catch (NumberFormatException e) {
			synchronized (_SendComm) {
				_SendComm.add("102:");
				System.out.println("102: Wrong number format");
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
					}
				}
			}
		} catch (NumberFormatException e) {
			synchronized (_SendComm) {
				_SendComm.add("102:");
				System.out.println("102: Wrong number format");
			}
		}
	}

	private void blackout(String status) {
		if (_blackoutState == 0) {
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
			synchronized (_SendComm) {
				_SendComm.add("304:1");
				System.out.println("304: Enabled blackout");
			}
			_blackoutState = 3;
		}
		if (!("1".equals(status)) && _blackoutState == 3) {
			// Angezeigt
			BlackoutObj.setOpacity(0);
			// BlackoutObj.toBack();
			synchronized (_SendComm) {
				_SendComm.add("304:0");
				System.out.println("232: Disabled blackout");
			}
			_blackoutState = 2;
		}
	}

	private void editPreferences(String[] toDos) {
		switch (toDos[1]) {
		case "screenshot":
			if ("1".equals(toDos[2])) {
				Server.einst.setStateScreenshActive(true);
				Server.StartLivestream();
				synchronized (_SendComm) {
					_SendComm.add("231:");
					System.out.println("231: Enabled screenshot");
				}
			} else {
				Server.einst.setStateScreenshActive(false);
				Server.StopLivestream();
				synchronized (_SendComm) {
					_SendComm.add("232:");
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
			File vidOrdner = new File(Server.homeDirectory + "/content");
			if (vidOrdner.exists()) {
				ArrayList<File> dateien = new ArrayList<File>(Arrays.asList(vidOrdner.listFiles()));
				synchronized (_SendComm) {
					_SendComm.add("421:");
					System.out.println("421: Video list start");
					for (File f : dateien) {
						String endung = getExtension(f.getName()).toLowerCase();
						// System.out.println(endung);
						switch (endung) {
						case "mp4":
							_SendComm.add(f.getPath());
							break;
						case "mp3":
							_SendComm.add(f.getPath());
							break;
						case "aif":
							_SendComm.add(f.getPath());
							break;
						case "aiff":
							_SendComm.add(f.getPath());
							break;
						case "wav":
							_SendComm.add(f.getPath());
							break;
						case "fxm":
							_SendComm.add(f.getPath());
							break;
						}
					}
					_SendComm.add("422:");
					System.out.println("422: Video list end");
				}
			} else {
				synchronized (_SendComm) {
					_SendComm.add("311:");
					System.out.println("311: content folder not found");
				}
			}
			break;
		case "img":
			File imgOrdner = new File(Server.homeDirectory + "/content");
			if (imgOrdner.exists()) {
				ArrayList<File> dateien = new ArrayList<File>(Arrays.asList(imgOrdner.listFiles()));
				synchronized (_SendComm) {
					_SendComm.add("423:");
					System.out.println("423: Img list start");
					for (File f : dateien) {
						String endung = getExtension(f.getName()).toLowerCase();
						// System.out.println(endung);
						switch (endung) {
						case "jpg":
							_SendComm.add(f.getPath());
							break;
						case "jpeg":
							_SendComm.add(f.getPath());
							break;
						case "png":
							_SendComm.add(f.getPath());
							break;
						case "gif":
							_SendComm.add(f.getPath());
							break;
						case "bmp":
							_SendComm.add(f.getPath());
							break;
						case "fxm":
							_SendComm.add(f.getPath());
							break;
						}
					}
					_SendComm.add("424:");
					System.out.println("424: Img list end");
				}
			} else {
				synchronized (_SendComm) {
					_SendComm.add("311:");
					System.out.println("311: content folder not found");
				}
			}
			break;
		case "objs":
			synchronized (_SendComm) {
				_SendComm.add("425:");
				System.out.println("311: Obj list start");
				for (Node obj : elem) {
					_SendComm.add(obj.getId());
				}
				_SendComm.add("426:");
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

	private void startSeqInput(String[] toDos) {
		if (SeqInputAct != true) {
			if (toDos.length == 2 && !toDos[1].isEmpty()) {
				SeqInputAct = true;
				NewSeqName = toDos[1];
				synchronized (_SendComm) {
					_SendComm.add("202:End with 'SeqEnd'");
					System.out.println("202: Sequence Input starting");
				}
				SeqCmds = new ArrayList<String>();
			} else {
				synchronized (_SendComm) {
					_SendComm.add("321:");
					_SendComm.add("101:");
					System.out.println("321, 101: No Sequence name given");
				}
			}
		} else {
			synchronized (_SendComm) {
				_SendComm.add("103:");
				System.out.println("103: Sequence Input already active");
			}
		}
	}

	private void playSequence(String[] toDos) {
		System.out.println("Starting");
		for (Sequence seq : sequenzen) {
			System.out.println("For");
			if (seq.GetId().equals(toDos[1])) {
				System.out.println("If");
				seq.run();
				System.out.println("Launched");
				synchronized (_SendComm) {
					_SendComm.add("212:" + toDos[1]);
					System.out.println("212: Started Sequence " + toDos[1]);
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

	private void sequenceEnd() {
		if (SeqInputAct == true) {
			SeqInputAct = false;
			synchronized (_SendComm) {
				_SendComm.add("203:" + NewSeqName);
				System.out.println("203: Ended sequence input");
			}
			sequenzen.add(new Sequence(SeqCmds, NewSeqName));
			SeqCmds = null;
		} else {
			synchronized (_SendComm) {
				_SendComm.add("103:");
				_SendComm.add("321:");
				System.out.println("103, 321: No Sequence active");
			}
		}
	}

	private void createSequence(String commText) {
		SeqCmds.add(commText);
	}

	private void delete(String objName) {
		System.out.println(objName);
		if ("all".equals(objName)) {
			for (Node obj : elem) {
				Gui.INSTANCE.delNodeList.add(obj);
			}
			elem.clear();
			sequenzen.clear();
			synchronized (_SendComm) {
				_SendComm.add("321:");
				System.out.println("Deleted all");
			}
		} else {
			ArrayList<Node> delNum = new ArrayList<Node>();
			for (Node obj : elem) {
				if (obj.getId().equals(objName)) {
					Gui.INSTANCE.delNodeList.add(obj);
					delNum.add(obj);
					synchronized (_SendComm) {
						_SendComm.add("322:" + objName);
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
