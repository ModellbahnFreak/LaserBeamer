package de.modellbahnfreak.laserbeamer.server;

import java.net.URL;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;

public class Gui extends Application implements Runnable {
	// private Text Ausg = new Text("(leer)");
	public static Gui INSTANCE;
	public Group root;
	public Scene scene;
	public ArrayList<Node> addNodeList = new ArrayList<Node>();
	public ArrayList<Node> delNodeList = new ArrayList<Node>();

	private final String[] args;

	//private final int snapshotRate = 1;// Snapshots per second

	public Gui() {
		this.args = new String[0];
	}

	public Gui(String ...args) {
		this.args = args;
	}

	@Override
	public void init() {
		try {
			super.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		INSTANCE = this;
	}

	Image Bild;
	ImageView BildView;

	@Override
	public void start(Stage primaryStage) {
		root = new Group();
		scene = new Scene(root, 800, 600, Color.BLACK);
		primaryStage.setScene(scene);

		/*
		 * File datei = new File("E:\\Georg\\Projekte\\Java\\Network\\MAF00127.MP4");
		 * Media m = new Media(datei.toURI().toString()); MediaPlayer player = new
		 * MediaPlayer(m); MediaView mediaView = new MediaView(player);
		 * player.setOnEndOfMedia(new Runnable() {
		 * 
		 * @Override public void run() { root.getChildren().remove(mediaView); }
		 * 
		 * });
		 */
		// player.play();
		// root.getChildren().add(mediaView);

		/*Bild = new Image("file:///E:/Georg/Projekte/Java/JavaFxBeamer/Martin_und_Regine.jpg");
		ImageView BildView = new ImageView();
		BildView.setX(0);
		BildView.setX(0);
		BildView.setImage(Bild);
		BildView.setId("Bild1");
		root.getChildren().add(BildView);*/

		/*
		 * Ausg.setFill(Color.WHITE); Ausg.setX(100); Ausg.setY(100);
		 * Ausg.setFont(Font.font("Ravie", FontWeight.NORMAL, 50));
		 * root.getChildren().add(Ausg);
		 */

		System.out.println("Gui started");
		primaryStage.setTitle("Beamer GUI");
		URL iconPath;
		iconPath = getClass().getResource("/icon.png");
		primaryStage.getIcons().add(new Image(iconPath.toString()));
		primaryStage.show();
		// primaryStage.setFullScreen(true);

		primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case F:
					primaryStage.setFullScreen(true);
					break;
				default:
					break;
				}
			}
		});
		if (getParameters().getRaw().stream().map(String::toLowerCase).map(String::trim).anyMatch(p -> p.equals("-f") || p.equals("--fullscreen"))) {
			System.out.println("Launching fullscreen");
			primaryStage.setFullScreen(true);
		}


		Task<Void> addTask = new Task<Void>() {
			// Runnable addTask = new Runnable() {
			@Override
			public Void call() throws Exception {
				// public void run() {
				while (true) {
					while (!addNodeList.isEmpty()) {
						final Node addNode = addNodeList.get(0);
						addNodeList.remove(0);
						System.out.println("adding " + addNode.getId());
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								root.getChildren().add(addNode);
							}
						});
					}
					Thread.sleep(10);
				}
			}
		};
		Thread addTh = new Thread(addTask);
		addTh.setDaemon(true);
		addTh.setName("AddGuiElem");
		addTh.start();

		Task<Void> delTask = new Task<Void>() {
			// Runnable delTask = new Runnable() {
			@Override
			public Void call() throws Exception {
				// public void run() {
				while (true) {
					while (!delNodeList.isEmpty()) {
						final Node delNode = delNodeList.get(0);
						delNodeList.remove(0);
						System.out.println("removing " + delNode.getId());
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								if (delNode instanceof MediaView) {
									((MediaView) delNode).getMediaPlayer().stop();
								}
								root.getChildren().remove(delNode);
							}
						});
					}
					Thread.sleep(10);
				}
			}
		};
		Thread delTh = new Thread(delTask);
		delTh.setDaemon(true);
		delTh.setName("DelGuiElem");
		delTh.start();

		Task<Void> snapsTask = new Task<Void>() {
			@Override
			public Void call() throws Exception {
				while (!Thread.currentThread().isInterrupted()) {
					/*if (Server.einst.getStateScreenshActive() == true) {
						//System.out.println("Sceenshot enabled");
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								WritableImage image = new WritableImage((int) primaryStage.getScene().getWidth(),
										(int) primaryStage.getScene().getHeight());
								image = primaryStage.getScene().snapshot(null);
								BufferedImage bild = SwingFXUtils.fromFXImage(image, null);
								Server.einst.setScreenshot(bild);
								//System.out.println("snap taken");
							}
						});
						long waitTime = (1 / snapshotRate) * 1000;
						Thread.sleep(waitTime);
						//Server.einst.setStateScreenshActive(false);
					} else {
						//System.out.println("Sceenshot disabled");
						Thread.sleep(1000);
					}*/
				}
				return null;
			}
		};
		Thread snapTh = new Thread(snapsTask);
		snapTh.setDaemon(true);
		snapTh.setName("CaptureSnapshot");
		//snapTh.start();

	}

	@Override
	public void run() {
		System.out.println("Gui start");
		launch(args);
	}
}
