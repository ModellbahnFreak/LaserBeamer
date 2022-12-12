package de.modellbahnfreak.laserbeamer.sequence;

import java.util.ArrayList;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class Cue {
	public int wait = 0;
	public int fadeIn = 0;
	public int fadeOut = 0;
	public int active = 0;
	public ArrayList<String> cmds;
	private Runnable FinishedCallback = null;

	Cue(int _wait, int _fadeIn, int _fadeOut, int _active, ArrayList<String> _cmds) {
		wait = _wait;
		fadeIn = _fadeIn;
		fadeOut = _fadeOut;
		active = _active;
		cmds = _cmds;
	}
	
	Cue(int _wait, int _fadeIn, int _fadeOut, int _active, ArrayList<String> _cmds, Runnable OnFinished) {
		wait = _wait;
		fadeIn = _fadeIn;
		fadeOut = _fadeOut;
		active = _active;
		cmds = _cmds;
		FinishedCallback = OnFinished;
	}
	
	void setOnFinished(Runnable OnFinished) {
		FinishedCallback = OnFinished;
	}

	void launch(ArrayList<Node> nodes) {
		PauseTransition waitForFade = new PauseTransition();
		waitForFade.setDuration(new Duration(wait));
		waitForFade.setOnFinished(e -> {
			SceneFadeIn(nodes);
		});
		//System.out.println("Waiting for fade in");
		waitForFade.play();
	}

	private void SceneFadeIn(ArrayList<Node> nodes) {
		//System.out.println("Fade in");
		for (String cmd : cmds) {
			if (cmd != null) {
				String[] ToDos = cmd.split(";");
				switch (ToDos[0]) {
				case "opacity":
					makeOpacityFade(nodes, ToDos);
					break;
				case "color":
					makeColorFade(nodes, ToDos);
					break;
				case "rotate":
					makeRotateFade(nodes, ToDos);
					break;
				case "scale":
					makeScaleFade(nodes, ToDos);
					break;
				case "move":
					makeMoveFade(nodes, ToDos);
					break;
				default:

					break;
				}
			}
		}
		PauseTransition waitForOut = new PauseTransition();
		waitForOut.setDuration(new Duration(fadeIn));
		waitForOut.setOnFinished(e -> {
			SceneActiveWait(nodes);
		});
		waitForOut.play();
	}

	private void makeMoveFade(ArrayList<Node> nodes, String[] toDos) {
		for(Node node : nodes) {
			if(node.getId().equals(toDos[1])) {
				TranslateTransition changeObj = new TranslateTransition();
				changeObj.setNode(node);
				changeObj.setDuration(new Duration(fadeIn));
				changeObj.setToX(Double.parseDouble(toDos[2]));
				changeObj.setToY(Double.parseDouble(toDos[3]));
				changeObj.play();
			}
		}
	}

	private void makeScaleFade(ArrayList<Node> nodes, String[] toDos) {
		for(Node node : nodes) {
			if(node.getId().equals(toDos[1])) {
				ScaleTransition changeObj = new ScaleTransition();
				changeObj.setNode(node);
				changeObj.setDuration(new Duration(fadeIn));
				changeObj.setToX(Double.parseDouble(toDos[2]));
				changeObj.setToY(Double.parseDouble(toDos[3]));
				changeObj.play();
			}
		}
	}

	private void makeRotateFade(ArrayList<Node> nodes, String[] toDos) {
		for(Node node : nodes) {
			if(node.getId().equals(toDos[1])) {
				RotateTransition changeObj = new RotateTransition();
				changeObj.setNode(node);
				changeObj.setDuration(new Duration(fadeIn));
				changeObj.setToAngle(Double.parseDouble(toDos[2]));
				changeObj.play();
			}
		}
	}

	private void makeColorFade(ArrayList<Node> nodes, String[] toDos) {
		for(Node node : nodes) {
			if(node.getId().equals(toDos[1]) && node instanceof Shape) {
				FillTransition changeObj = new FillTransition();
				changeObj.setShape((Shape) node);
				changeObj.setDuration(new Duration(fadeIn));
				changeObj.setToValue(Color.web(toDos[2]));
				changeObj.play();
			}
		}
	}

	private void makeOpacityFade(ArrayList<Node> nodes, String[] toDos) {
		for(Node node : nodes) {
			if(node.getId().equals(toDos[1])) {
				FadeTransition changeObj = new FadeTransition();
				changeObj.setNode(node);
				changeObj.setDuration(new Duration(fadeIn));
				changeObj.setToValue(Double.parseDouble(toDos[2]));
				changeObj.play();
			}
		}
	}

	private void SceneActiveWait(ArrayList<Node> nodes) {
		PauseTransition waitForOut = new PauseTransition();
		waitForOut.setDuration(new Duration(active));
		waitForOut.setOnFinished(e -> {
			SceneFadeOut(nodes);
		});
		//System.out.println("Waiting for fade out");
		waitForOut.play();
	}

	private void SceneFadeOut(ArrayList<Node> nodes) {
		if (fadeOut > 0) {
			//System.out.println("fade out");
			for (Node node : nodes) {
				FadeTransition nodeFadeOut = new FadeTransition();
				nodeFadeOut.setNode(node);
				nodeFadeOut.setDuration(new Duration(fadeOut));
				nodeFadeOut.setFromValue(node.getOpacity());
				nodeFadeOut.setToValue(0.0);
				nodeFadeOut.play();
			}
			PauseTransition waitForCall = new PauseTransition();
			waitForCall.setDuration(new Duration(fadeOut));
			waitForCall.setOnFinished(e -> {
				SceneCallback();
			});
			//System.out.println("Waiting for callback");
			waitForCall.play();
		} else {
			SceneCallback();
			//System.out.println("Waiting for callback");
		}
	}
	
	private void SceneCallback() {
		//System.out.println("try callback");
		if (FinishedCallback != null) {
			//System.out.println("callback");
			Thread EndeCallback = new Thread(FinishedCallback);
			EndeCallback.setDaemon(true);
			EndeCallback.setName("CueEndCallback");
			EndeCallback.start();
		}
	}
}
