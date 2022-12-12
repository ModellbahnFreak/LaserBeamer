package de.modellbahnfreak.laserbeamer.sequence;

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
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import de.modellbahnfreak.laserbeamer.server.Gui;

public class NodeCreator {
	
	public static boolean validateCmd(String cmd) {
		String[] ToDos = cmd.split(";");
		switch (ToDos[0]) {
		case "img":
			return true;
		case "vid":
			return true;
		case "txtSize":
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
		case "txtSize":
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
				kreis.setCenterX(Double.parseDouble(toDos[2])*Gui.INSTANCE.scene.getWidth());
				kreis.setCenterY(Double.parseDouble(toDos[3])*Gui.INSTANCE.scene.getHeight());
				if ("full".equals(toDos[4])) {
					kreis.setRadius(Gui.INSTANCE.scene.getWidth() - kreis.getCenterX());
				} else {
					kreis.setRadius(Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getWidth());
				}
				kreis.setFill(Color.web(toDos[5]));
				kreis.setStroke(Color.WHITE);
				kreis.setStrokeWidth(0);
			}
			if (toDos.length >= 8) {
				kreis.setStroke(Color.web(toDos[6]));
				kreis.setStrokeWidth(Double.parseDouble(toDos[7])*Gui.INSTANCE.scene.getWidth());
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
				rect.setX(Double.parseDouble(toDos[2])*Gui.INSTANCE.scene.getWidth());
				rect.setY(Double.parseDouble(toDos[3])*Gui.INSTANCE.scene.getHeight());
				if ("full".equals(toDos[4])) {
					rect.setWidth(Gui.INSTANCE.scene.getWidth());
				} else {
					rect.setWidth(Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getWidth());
				}
				if ("full".equals(toDos[5])) {
					rect.setHeight(Gui.INSTANCE.scene.getHeight());
				} else {
					rect.setHeight(Double.parseDouble(toDos[5])*Gui.INSTANCE.scene.getHeight());
				}
				rect.setFill(Color.web(toDos[6]));
				rect.setStroke(Color.WHITE);
				rect.setStrokeWidth(0);
			}
			if (toDos.length >= 9) {
				rect.setArcWidth(Double.parseDouble(toDos[7])*rect.getWidth());
				rect.setArcHeight(Double.parseDouble(toDos[8])*rect.getHeight());
			}
			if (toDos.length >= 11) {
				rect.setStroke(Color.web(toDos[9]));
				rect.setStrokeWidth(Double.parseDouble(toDos[10])*Gui.INSTANCE.scene.getWidth());
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
				linie.setStartX(Double.parseDouble(toDos[2])*Gui.INSTANCE.scene.getWidth());
				linie.setStartY(Double.parseDouble(toDos[3])*Gui.INSTANCE.scene.getHeight());
				linie.setEndX(Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getWidth());
				linie.setEndY(Double.parseDouble(toDos[5])*Gui.INSTANCE.scene.getHeight());
				linie.setStroke(Color.web(toDos[6]));
				// linie.setStrokeWidth(10);
			}
			if (toDos.length == 8) {
				linie.setStrokeWidth(Double.parseDouble(toDos[7])*Gui.INSTANCE.scene.getWidth());
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
					System.out.println("Datei existiert nicht!");
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
				mediaView.getProperties().put("path", datei.toString());
				String mode = "";
				if (toDos.length == 3) {
					mediaView.setX(0);
					mediaView.setY(0);
				} else if (toDos.length == 4) {
					mediaView.setX(0);
					mediaView.setY(0);
					mode = toDos[3];
				} else if (toDos.length == 6) {
					mode = toDos[3];
					mediaView.setX(Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getWidth());
					mediaView.setY(Double.parseDouble(toDos[5])*Gui.INSTANCE.scene.getHeight());
				} else if (toDos.length == 8) {
					mode = toDos[3];
					mediaView.setX(Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getWidth());
					mediaView.setY(Double.parseDouble(toDos[5])*Gui.INSTANCE.scene.getHeight());
					if ("full".equals(toDos[6])) {
						mediaView.setFitWidth(Gui.INSTANCE.scene.getWidth());
					} else {
						mediaView.setFitWidth(Double.parseDouble(toDos[6])*Gui.INSTANCE.scene.getWidth());
					}
					if ("full".equals(toDos[7])) {
						mediaView.setFitHeight(Gui.INSTANCE.scene.getHeight());
					} else {
						mediaView.setFitHeight(Double.parseDouble(toDos[7])*Gui.INSTANCE.scene.getHeight());
					}
				} else {
					return null;
				}
				
				mediaView.getProperties().put("mode", mode);
				
				Runnable loopMode = new Runnable() {
					@Override
					public void run() {
						player.seek(Duration.ZERO);
					}
				};
				Runnable singleMode = new Runnable() {
					@Override
					public void run() {
					}
				};
				Runnable onceMode = new Runnable() {
					@Override
					public void run() {
						voidDel.deleteElem(VidUrl);
					}
				};
				Runnable autoloopMode = new Runnable() {
					@Override
					public void run() {
						player.seek(Duration.ZERO);
					}
				};
				
				switch (mode) {
				case "loop":
					player.setOnEndOfMedia(loopMode);
					break;
				case "once":
					player.setOnEndOfMedia(onceMode);
					break;
				case "autoonce":
					player.setOnEndOfMedia(onceMode);
					System.out.println("Playing once");
					player.play();
					break;
				case "single":
					player.setOnEndOfMedia(singleMode);
					break;
				case "autoloop":
					player.setOnEndOfMedia(autoloopMode);
					player.play();
					break;
				default:
					player.setOnEndOfMedia(onceMode);
					break;
				}
				//player.play();
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
					BildView.setY(0);
				} else if (toDos.length == 5) {
					BildView.setX(Double.parseDouble(toDos[3])*Gui.INSTANCE.scene.getWidth());
					BildView.setY(Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getHeight());
				} else if (toDos.length == 7) {
					BildView.setX(Double.parseDouble(toDos[3])*Gui.INSTANCE.scene.getWidth());
					BildView.setY(Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getHeight());
					if ("full".equals(toDos[5])) {
						BildView.setFitWidth(Gui.INSTANCE.scene.getWidth());
					} else {
						BildView.setFitWidth(Double.parseDouble(toDos[5])*Gui.INSTANCE.scene.getWidth());
					}
					if ("full".equals(toDos[6])) {
						BildView.setFitHeight(Gui.INSTANCE.scene.getHeight());
					} else {
						BildView.setFitHeight(Double.parseDouble(toDos[6])*Gui.INSTANCE.scene.getHeight());
					}
				} else {
					return null;
				}
				BildView.setImage(Bild);
				BildView.setId(toDos[1]);
				BildView.getProperties().put("path", datei.toString());
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
			String text = "";
			if (textStart < 0 || textEnd < 0) {
				text = toDos[4];
			} else {
				text = sendData.substring(textStart, textEnd);
			}
			if (text.length() <= 0 || text.isEmpty()) {
				text = toDos[1];
			}
			double textSize = 50;
			String font = "Ravie";
			if ("txtSize".equals(toDos[0])) {
				textSize = Double.parseDouble(toDos[4])*Gui.INSTANCE.scene.getWidth();
				font = toDos[5];
			}
			Text Ausg = new Text(text);
			Ausg.setFill(Color.web(toDos[toDos.length - 1]));
			Ausg.setX(Double.parseDouble(toDos[2])*Gui.INSTANCE.scene.getWidth());
			Ausg.setY(Double.parseDouble(toDos[3])*Gui.INSTANCE.scene.getHeight());
			Ausg.setFont(Font.font(font, FontWeight.NORMAL, textSize));
			Ausg.setId(toDos[1]);
			return Ausg;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static String nodeToString (Node objekt) {
		String erg = "";
		double sceneW = Gui.INSTANCE.scene.getWidth(),
				sceneH = Gui.INSTANCE.scene.getHeight();
		if (objekt instanceof ImageView) {
			double xPos = ((ImageView)objekt).getX()/sceneW,
				yPos = ((ImageView)objekt).getY()/sceneH,
				width = ((ImageView)objekt).getFitWidth()/sceneW,
				height = ((ImageView)objekt).getFitHeight()/sceneH;
			erg = "img;" + objekt.getId() + ";" + objekt.getProperties().get("path").toString() + ";" + xPos + ";" + yPos + ";" + width + ";" + height;
		} else if (objekt instanceof MediaView) {
			double xPos = ((MediaView)objekt).getX()/sceneW,
					yPos = ((MediaView)objekt).getY()/sceneH,
					width = ((MediaView)objekt).getFitWidth()/sceneW,
					height = ((MediaView)objekt).getFitHeight()/sceneH;
			erg = "vid;" + objekt.getId() + ";" + objekt.getProperties().get("path").toString() + ";" + objekt.getProperties().get("mode") + ";" + xPos + ";" + yPos + ";" + width + ";" + height;
		} else if (objekt instanceof Text) {
			double xPos = ((Text)objekt).getX()/sceneW,
					yPos = ((Text)objekt).getY()/sceneH,
					size = ((Text)objekt).getFont().getSize()/sceneW;
			String fontFamily = ((Text)objekt).getFont().getFamily();
			erg = "txtSize;" + objekt.getId() + ";" + xPos + ";" + yPos + ";" + size + ";" + fontFamily + ";'" + ((Text)objekt).getText() + "';" + colorToStr(((Text) objekt).getFill());
		} else if (objekt instanceof Line) {
			double x1 = ((Line)objekt).getStartX()/sceneW,
					y1 = ((Line)objekt).getStartY()/sceneH,
					x2 = ((Line)objekt).getEndX()/sceneW,
					y2 = ((Line)objekt).getEndY()/sceneH,
					strokeW = ((Line)objekt).getStrokeWidth()/sceneW;
			erg = "line;" + objekt.getId() + ";" + x1 + ";" + y1 + ";" + x2 + ";" + y2 + ";" + colorToStr(((Line)objekt).getStroke()) + ";" + strokeW;
		} else if (objekt instanceof Rectangle) {
			String id = objekt.getId();
			double xpos = ((Rectangle)objekt).getX()/sceneW;
			double ypos = ((Rectangle)objekt).getY()/sceneH;
			double w = ((Rectangle)objekt).getWidth()/sceneW;
			double h = ((Rectangle)objekt).getHeight()/sceneH;
			String color = colorToStr(((Rectangle)objekt).getFill());
			double ArcW = ((Rectangle)objekt).getArcWidth()/((Rectangle)objekt).getWidth();
			double ArcH = ((Rectangle)objekt).getArcHeight()/((Rectangle)objekt).getHeight();
			String StrokeC = colorToStr(((Rectangle)objekt).getStroke());
			double StrokeW = ((Rectangle)objekt).getStrokeWidth()/sceneW;
			erg = "rect;" + id + ";" + xpos + ";" + ypos + ";" + w + ";" + h + ";" + color + ";" + ArcW + ";" + ArcH + ";" + StrokeC + ";" + StrokeW;
			//erg = "rect;" + objekt.getId() + ";" + ((Rectangle)objekt).getX() + ";" + ((Rectangle)objekt).getY() + ";" + ((Rectangle)objekt).getWidth() + ";" + ((Rectangle)objekt).getHeight() + ";" + ((Rectangle)objekt).getFill().toString() + ";" + ((Rectangle)objekt).getArcWidth() + ";" + ((Rectangle)objekt).getArcHeight() + ";" + ((Rectangle)objekt).getStroke().toString() + ";" + ((Rectangle)objekt).getStrokeWidth();
		} else if (objekt instanceof Circle) {
			double xPos = ((Circle)objekt).getCenterX()/sceneW,
					yPos = ((Circle)objekt).getCenterY()/sceneH,
					size = ((Circle)objekt).getRadius()/sceneW,
					strokeW = ((Circle)objekt).getStrokeWidth()/sceneW;
			erg = "circle;" + objekt.getId() + ";" + xPos + ";" + yPos + ";" + size + ";" + colorToStr(((Circle)objekt).getFill()) + ";" + colorToStr(((Circle)objekt).getStroke()) + ";" + strokeW;
		}
		return erg;
	}
	
	public static String getAllObjTypes() {
		String erg = "txt-Text;" + 
				"txtSize-Text individuell;" + 
				"img-Bild;" + 
				"vid-Video;" + 
				"line-Linie;" + 
				"rect-Rechteck/Oval;" + 
				"circle-Kreis";
		return erg;
	}
	
	private static String colorToStr(Paint paint) {
		Color c = (Color) paint;
		return String.format( "#%02X%02X%02X", (int)( c.getRed() * 255 ), (int)( c.getGreen() * 255 ), (int)( c.getBlue() * 255 ) );
	}
}
