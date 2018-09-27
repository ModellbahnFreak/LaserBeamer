package sequenceNew;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TempMain extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		Group root = new Group();
		Scene scene = new Scene(root, 800, 600, Color.BLACK);
		primaryStage.setScene(scene);
		primaryStage.show();
		/*Text temp = new Text("Hallo Welt");
		temp.setFill(Color.WHITE);
		temp.setX(100);
		temp.setY(100);
		temp.setFont(Font.font("Ravie", FontWeight.NORMAL, 50));
		temp.setId("Textfeld");
		root.getChildren().add(temp);
		ObjProperties props = new ObjProperties(temp);
		props.addProperty("xPos");
		props.setFrame("xPos", 0, 0, 1);
		props.setFrame("xPos", 50, 25, 1);
		props.setFrame("xPos", 100, 100, 1);
		props.updateInperpolate();
		System.out.println(props.toString());*/
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add("line;linie;0;0;100;100;#FFFFFF");
		cmds.add("rect;rec;150;150;50;100;#FFFFFF");
		cmds.add("frame;linie;xPos;0;0;1");
		cmds.add("frame;linie;xPos;100;100;1");
		cmds.add("frame;rec;opacity;0;1;1");
		cmds.add("frame;rec;opacity;50;0;1");
		cmds.add("frame;rec;opacity;100;1;1");
		cmds.add("frame;linie;yPos;50;500;1");
		cmds.add("frame;linie;yPos;0;0;1");
		cmds.add("frame;linie;yPos;100;30;1");
		cmds.add("frame;linie;yEnd;0;100;2");
		cmds.add("frame;linie;yEnd;100;750;2");
		cmds.add("frame;linie;xEnd;100;750;2");
		cmds.add("frame;linie;xEnd;0;0;2");
		Sequenz seq = new Sequenz(cmds, "Neu");
		System.out.println(seq.toString());
		for (Node elem : seq.getNodes()) {
			root.getChildren().add(elem);
		}
		seq.play(0);
	}
}
