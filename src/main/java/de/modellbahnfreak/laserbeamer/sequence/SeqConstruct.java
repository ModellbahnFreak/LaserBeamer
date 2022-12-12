package de.modellbahnfreak.laserbeamer.sequence;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class SeqConstruct {
	public static Sequence getSeq(String typ) {
		switch (typ) {
		case "LineImg":
			return LineImg();
		default:
			return null;
		}
	}

	private static Sequence LineImg() {
		ArrayList<Node> objekte = new ArrayList<Node>();
		ArrayList<Cue> cues = new ArrayList<Cue>();
		ArrayList<String> cmds = null;
		
		Line linie = new Line();
		linie.setId("Hallo");
		linie.setStartX(50);
		linie.setEndX(500);
		linie.setStartY(50);
		linie.setEndY(500);
		linie.setStroke(Color.web("#FFFF00"));
		linie.setStrokeWidth(20);
		// linie.setOpacity(0);
		URL path = null;
		try {
			path = new File("./Martin_und_Regine.JPG").toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageView iv = new ImageView(new Image(path.toString()));
		iv.setId("Bild");
		iv.setX(0);
		iv.setY(0);
		iv.setPreserveRatio(true);
		iv.setFitWidth(200);
		// iv.setOpacity(0);
		objekte.add(iv);
		objekte.add(linie);
		
		cmds = new ArrayList<String>();
		cmds.add("rotate;Bild;45");
		cmds.add("move;Bild;200;200");
		cmds.add("move;Hallo;500;0");
		cmds.add("color;Hallo;#FF0000");
		cues.add(new Cue(0, 2000, 0, 2000, cmds));
		
		cmds = new ArrayList<String>();
		cmds.add("opacity;Hallo;0");
		cmds.add("rotate;Bild;360");
		cues.add(new Cue(0, 1000, 0, 2000, cmds));
		
		cmds = new ArrayList<String>();
		cmds.add("scale;Bild;2;2");
		cues.add(new Cue(1000, 2000, 500, 2000, cmds));
		
		return new Sequence(objekte, cues);
	}
}
