package dmx;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Queue;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import server.Gui;
import server.Server;

public class ArtNetProcess implements Runnable {
	
	byte[] daten = null;
	byte[] datenAktuell = null;
	byte[] datenAlt = null;
	byte[] kanaele = null;
	ImageView gobo = null;
	MediaView goboVid = null;
	boolean isVideo = false;
	Queue<String> _recvData;
	
	public ArtNetProcess(byte[] recvDmx, Queue<String> recvData) {
		daten = recvDmx;
		kanaele = Server.einst.getChannels();
		datenAktuell = new byte[Server.einst.getChannelCount()];
		datenAlt = new byte[Server.einst.getChannelCount()];
		_recvData = recvData;
	}

	@Override
	public void run() {
		while (Gui.INSTANCE == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("ArtNetProcessing started");
		gobo = new ImageView();
		gobo.setCache(true);
		gobo.setCacheHint(CacheHint.SPEED);
		gobo.setId("DMXGobo");
		
		goboVid = new MediaView();
		goboVid.setCache(true);
		goboVid.setCacheHint(CacheHint.SPEED);
		goboVid.setId("DMXGoboVideo");
		goboVid.opacityProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				try {
					if (newValue.doubleValue() <= 0) {
						goboVid.getMediaPlayer().pause();
					} else {
						goboVid.getMediaPlayer().play();
					}
				} catch (Exception e) {
					
				}
			}
		});
		
		if (isVideo) {
			gobo.setOpacity(0);
			goboVid.setOpacity(1);
		} else {
			goboVid.setOpacity(0);
			gobo.setOpacity(1);
		}
		Gui.INSTANCE.addNodeList.add(gobo);
		Gui.INSTANCE.addNodeList.add(goboVid);
		byte Function = -1;
		byte Select = -1;
		byte Play = -1;
		boolean playChange = false;
		while (!Thread.currentThread().isInterrupted()) {
			playChange = false;
			synchronized (daten) {
				try {
					daten.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				datenAktuell = daten;
			}
			/*System.out.print(Arrays.toString(datenAktuell) + " - ");
			System.out.println(Arrays.toString(datenAlt));*/
			for (int i = 0; i < kanaele.length; i++) {
				if (datenAktuell[i] != datenAlt[i]) {
					switch (kanaele[i]) {
					case 0: //MasterDimmer
							Gui.INSTANCE.root.setOpacity((0xff&datenAktuell[i])/255.0);
						break;
					case 1: //Function
						Function = datenAktuell[i];
						playChange = true;
						break;
					case 2: //Select
						Select = datenAktuell[i];
						playChange = true;
						break;
					case 3: //Play
						Play = datenAktuell[i];
						playChange = true;
						break;
					case 4: //Pattern
						//if (datenAktuell[i] != 0) {
							File bild = new File (Server.einst.getHomeDir() + "/gobos/" + (0xff&datenAktuell[i]) + ".png");
							if (bild.exists()) {
								System.out.println("Loading " + bild.getAbsolutePath());
								try {
									gobo.setImage(new Image(bild.toURI().toURL().toString()));
									isVideo = false;
								} catch (MalformedURLException e) {
									e.printStackTrace();
								}
							} else {
								File film = new File (Server.einst.getHomeDir() + "/gobos/" + (0xff&datenAktuell[i]) + ".mp4");
								if (film.exists()) {
									System.out.println("Loading " + film.getAbsolutePath());
									try {
										goboVid.setMediaPlayer(new MediaPlayer(new Media(film.toURI().toURL().toString())));
										goboVid.getMediaPlayer().setOnEndOfMedia(new Runnable() {
											@Override
											public void run() {
												goboVid.getMediaPlayer().seek(Duration.ZERO);
											}
										});
										isVideo = true;
										goboVid.getMediaPlayer().play();
									} catch (MalformedURLException e) {
										e.printStackTrace();
									}
								}
							}
							if (isVideo) {
								gobo.setOpacity(0);
								goboVid.setOpacity(1);
							} else {
								goboVid.setOpacity(0);
								gobo.setOpacity(1);
							}
						/*} else {
							
						}*/
						break;
					case 5: //Color
						
						break;
					case 6: //Size
						gobo.setScaleX(((0xff&datenAktuell[i])/255.0)*5);
						gobo.setScaleY(((0xff&datenAktuell[i])/255.0)*5);
						goboVid.setScaleX(((0xff&datenAktuell[i])/255.0)*5);
						goboVid.setScaleY(((0xff&datenAktuell[i])/255.0)*5);
						break;
					case 7: //Multi
						
						break;
					case 8: //xPos
						goboVid.setX((((0xff&datenAktuell[i])/255.0)*Gui.INSTANCE.scene.getWidth()));
						gobo.setX((((0xff&datenAktuell[i])/255.0)*Gui.INSTANCE.scene.getWidth()));
						break;
					case 9: //yPos
						goboVid.setY((((0xff&datenAktuell[i])/255.0)*Gui.INSTANCE.scene.getHeight()));
						gobo.setY((((0xff&datenAktuell[i])/255.0)*Gui.INSTANCE.scene.getHeight()));
						break;
					case 10: //Dimmer
						if (isVideo) {
							goboVid.setOpacity((0xff&datenAktuell[i])/255.0);
						} else {
							gobo.setOpacity((0xff&datenAktuell[i])/255.0);
						}
						break;
					case 11: //RotOffset
						goboVid.setRotate(360-((0xff&datenAktuell[i])/255.0)*360.0);
						gobo.setRotate(360-((0xff&datenAktuell[i])/255.0)*360.0);
						break;
					}
					datenAlt[i] = datenAktuell[i];
				}
			}
			if (playChange) {
				processPlayState(0xff&Function, 0xff&Select, 0xff&Play);
			}
			//datenAlt = datenAktuell;
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		Gui.INSTANCE.delNodeList.add(gobo);
	}

	private void processPlayState(int function, int select, int play) {
		switch (function) {
		case 1:
			if (play == 3) {
				synchronized (_recvData) {
					_recvData.add("playSeq;" + (0xff&select));
					_recvData.notifyAll();
				}
			}
			break;
		case 2:
			if (play == 3) {
				if (new File("content/" + (0xff&select) + ".mp4").exists()) {
					synchronized (_recvData) {
						_recvData.add("vid;dmxVideo;content/" + (0xff&select) + ".mp4;single;0;0");
						_recvData.notifyAll();
						_recvData.add("player;dmxVideo;play");
						_recvData.notifyAll();
					}
					System.out.println("Spiele Vid");
				} else if (new File("content/" + (0xff&select) + ".mp3").exists()) {
					synchronized (_recvData) {
						_recvData.add("vid;dmxVideo;content/" + (0xff&select) + ".mp3;single;0;0");
						_recvData.notifyAll();
						_recvData.add("player;dmxVideo;play");
						_recvData.notifyAll();
					}
					System.out.println("Spiele Aud");
				}
			} else if (play == 5) {
				synchronized (_recvData) {
					_recvData.add("del;dmxVideo");
					_recvData.notifyAll();
				}
				System.out.println("Entf Vid");
			} else if (play == 1) {
				synchronized (_recvData) {
					_recvData.add("player;dmxVideo;stop");
					_recvData.notifyAll();
				}
				System.out.println("Stoppe Vid");
			} else if (play == 2) {
				synchronized (_recvData) {
					_recvData.add("player;dmxVideo;pause");
					_recvData.notifyAll();
				}
				System.out.println("Pause Vid");
			} else if (play == 4) {
				synchronized (_recvData) {
					_recvData.add("player;dmxVideo;play");
					_recvData.notifyAll();
				}
				System.out.println("Spiele Vid erneut");
			}
			break;
		case 3:
			if (play == 3) {
				synchronized (_recvData) {
					_recvData.add("img;dmxBild;content/" + (0xff&select) + ".png;0;0");
					_recvData.notifyAll();
				}
			} else if (play == 5) {
				synchronized (_recvData) {
					_recvData.add("del;dmxBild");
					_recvData.notifyAll();
				}
			}
			break;
		}
	}

}
