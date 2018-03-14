package sequence;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import server.Gui;

public class NodeCreator {
	public static boolean validateCmd(String cmd) {
		String[] ToDos = cmd.split(";");
		switch (ToDos[0]) {
		case "img":
			return true;
		case "vid":
			return true;
		case "txt":
			return true;
		case "line":
			return true;
		case "rect":
			return true;
		case "circle":
			return true;
		default:
			break;
		}
		return false;
	}

	public static Node cmdToNode(String cmd, DeleteHandler VoidDel) {
		String[] ToDos = cmd.split(";");
		switch (ToDos[0]) {
		case "img":
			return createImg(ToDos);
		case "vid":
			return createVid(ToDos, VoidDel);
		case "txt":
			return showText(ToDos, cmd);
		case "line":
			return createLine(ToDos);
		case "rect":
			return createRect(ToDos);
		case "circle":
			return createCircle(ToDos);
		default:
			break;
		}
		return null;
	}

	private static Node createCircle(String[] toDos) {
		try {
			Circle kreis = null;
			if (toDos.length >= 6) {
				kreis = new Circle();
				kreis.setId(toDos[1]);
				kreis.setCenterX(Double.parseDouble(toDos[2]));
				kreis.setCenterY(Double.parseDouble(toDos[3]));
				if ("full".equals(toDos[4])) {
					kreis.setRadius(Gui.INSTANCE.scene.getWidth() - kreis.getCenterX());
				} else {
					kreis.setRadius(Double.parseDouble(toDos[4]));
				}
				kreis.setFill(Color.web(toDos[5]));
				kreis.setStrokeWidth(0);
			}
			if (toDos.length >= 8) {
				kreis.setStroke(Color.web(toDos[6]));
				kreis.setStrokeWidth(Double.parseDouble(toDos[7]));
			}
			return kreis;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Node createRect(String[] toDos) {
		try {
			Rectangle rect = null;
			if (toDos.length >= 7) {
				rect = new Rectangle();
				rect.setId(toDos[1]);
				rect.setX(Double.parseDouble(toDos[2]));
				rect.setY(Double.parseDouble(toDos[3]));
				if ("full".equals(toDos[4])) {
					rect.setWidth(Gui.INSTANCE.scene.getWidth());
				} else {
					rect.setWidth(Double.parseDouble(toDos[4]));
				}
				if ("full".equals(toDos[5])) {
					rect.setHeight(Gui.INSTANCE.scene.getHeight());
				} else {
					rect.setHeight(Double.parseDouble(toDos[5]));
				}
				rect.setFill(Color.web(toDos[6]));
				rect.setStrokeWidth(0);
			}
			if (toDos.length >= 9) {
				rect.setArcWidth(Double.parseDouble(toDos[7]));
				rect.setArcHeight(Double.parseDouble(toDos[8]));
			}
			if (toDos.length >= 11) {
				rect.setStroke(Color.web(toDos[9]));
				rect.setStrokeWidth(Double.parseDouble(toDos[10]));
			}
			return rect;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Node createLine(String[] toDos) {
		try {
			Line linie = null;
			if (toDos.length >= 7) {
				linie = new Line();
				linie.setId(toDos[1]);
				linie.setStartX(Double.parseDouble(toDos[2]));
				linie.setStartY(Double.parseDouble(toDos[3]));
				linie.setEndX(Double.parseDouble(toDos[4]));
				linie.setEndY(Double.parseDouble(toDos[5]));
				linie.setStroke(Color.web(toDos[6]));
				// linie.setStrokeWidth(10);
			}
			if (toDos.length == 8) {
				linie.setStrokeWidth(Double.parseDouble(toDos[7]));
			}
			return linie;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Node createVid(String[] toDos, DeleteHandler voidDel) {
		try {
			MediaView mediaView = null;
			URL pfad = null;
			try {
				File datei =new File(toDos[2]); 
				if (!datei.exists()) {
					return null;
				}
				pfad = datei.toURI().toURL();
				System.out.println(pfad.toString());
				// File datei = new File("E:\\Georg\\Projekte\\Java\\Network\\MAF00127.MP4");
				Media m = new Media(pfad.toURI().toString());
				MediaPlayer player = new MediaPlayer(m);
				mediaView = new MediaView(player);
				String VidUrl = toDos[1];
				mediaView.setId(VidUrl);
				String mode = "";
				if (toDos.length == 3) {
					mediaView.setX(0);
					mediaView.setX(0);
				} else if (toDos.length == 4) {
					mediaView.setX(0);
					mediaView.setX(0);
					mode = toDos[3];
				} else if (toDos.length == 6) {
					mode = toDos[3];
					mediaView.setX(Double.parseDouble(toDos[4]));
					mediaView.setX(Double.parseDouble(toDos[5]));
				} else if (toDos.length == 8) {
					mode = toDos[3];
					mediaView.setX(Double.parseDouble(toDos[4]));
					mediaView.setX(Double.parseDouble(toDos[5]));
					if ("full".equals(toDos[6])) {
						mediaView.setFitWidth(Gui.INSTANCE.scene.getWidth());
					} else {
						mediaView.setFitWidth(Double.parseDouble(toDos[6]));
					}
					if ("full".equals(toDos[7])) {
						mediaView.setFitHeight(Gui.INSTANCE.scene.getHeight());
					} else {
						mediaView.setFitHeight(Double.parseDouble(toDos[7]));
					}
				} else {
					return null;
				}
				switch (mode) {
				case "loop":
					player.setOnEndOfMedia(new Runnable() {
						@Override
						public void run() {
							player.seek(Duration.ZERO);
						}
					});
					break;
				case "once":
					player.setOnEndOfMedia(new Runnable() {
						@Override
						public void run() {
							voidDel.deleteElem(VidUrl);
						}
					});
					break;
				case "single":

					break;
				default:
					player.setOnEndOfMedia(new Runnable() {
						@Override
						public void run() {
							// voidDel.delete(mediaView.getId());
							voidDel.deleteElem(VidUrl);
						}
					});
					break;
				}
				player.play();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return mediaView;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Node createImg(String[] toDos) {
		try {
			// Image Bild = new
			// Image("file:///E:/Georg/Projekte/Java/JavaFxBeamer/Martin_und_Regine.jpg");
			ImageView BildView = null;
			URL pfad = null;
			try {
				File datei = new File(toDos[2]); 
				if (!datei.exists()) {
					return null;
				}
				pfad = datei.toURI().toURL();
				// System.out.println(pfad.toString());
				Image Bild = new Image(pfad.toString());
				BildView = new ImageView();
				if (toDos.length == 3) {
					BildView.setX(0);
					BildView.setX(0);
				} else if (toDos.length == 5) {
					BildView.setX(Double.parseDouble(toDos[3]));
					BildView.setX(Double.parseDouble(toDos[4]));
				} else if (toDos.length == 7) {
					BildView.setX(Double.parseDouble(toDos[3]));
					BildView.setX(Double.parseDouble(toDos[4]));
					if ("full".equals(toDos[5])) {
						BildView.setFitWidth(Gui.INSTANCE.scene.getWidth());
					} else {
						BildView.setFitWidth(Double.parseDouble(toDos[5]));
					}
					if ("full".equals(toDos[6])) {
						BildView.setFitHeight(Gui.INSTANCE.scene.getHeight());
					} else {
						BildView.setFitHeight(Double.parseDouble(toDos[6]));
					}
				} else {
					return null;
				}
				BildView.setImage(Bild);
				BildView.setId(toDos[1]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return BildView;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Node showText(String[] toDos, String sendData) {
		try {
			int textStart = sendData.indexOf("'") + 1;
			int textEnd = sendData.lastIndexOf("'");
			String text = sendData.substring(textStart, textEnd);
			if (text.length() <= 0 || text.isEmpty()) {
				text = toDos[4];
			}
			if (text.length() <= 0 || text.isEmpty()) {
				text = toDos[1];
			}
			Text Ausg = new Text(text);
			Ausg.setFill(Color.web(toDos[toDos.length - 1]));
			Ausg.setX(Double.parseDouble(toDos[2]));
			Ausg.setY(Double.parseDouble(toDos[3]));
			Ausg.setFont(Font.font("Ravie", FontWeight.NORMAL, 50));
			Ausg.setId(toDos[1]);
			return Ausg;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
