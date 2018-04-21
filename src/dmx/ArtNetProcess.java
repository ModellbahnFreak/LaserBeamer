package dmx;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import javafx.scene.CacheHint;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import server.Gui;
import server.Server;

public class ArtNetProcess implements Runnable {
	
	byte[] daten = null;
	byte[] datenAktuell = null;
	byte[] datenAlt = null;
	byte[] kanaele = null;
	ImageView gobo = null;
	
	public ArtNetProcess(byte[] recvDmx) {
		daten = recvDmx;
		kanaele = Server.einst.getChannels();
		datenAktuell = new byte[Server.einst.getChannelCount()];
		datenAlt = new byte[Server.einst.getChannelCount()];
	}

	@Override
	public void run() {
		while (Gui.INSTANCE == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("ArtNetProcessing started");
		gobo = new ImageView();
		gobo.setCache(true);
		gobo.setCacheHint(CacheHint.SPEED);
		gobo.setId("DMXGobo");
		/*try {
			gobo.setImage(new Image(new File (Server.einst.getHomeDir() + "1.png").toURI().toURL().toString()));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		gobo.setFitWidth(500);*/
		Gui.INSTANCE.addNodeList.add(gobo);
		while (!Thread.currentThread().isInterrupted()) {
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
						
						break;
					case 2: //Select
						
						break;
					case 3: //Play
						
						break;
					case 4: //Pattern
						if (datenAktuell[i] != 0) {
							File bild = new File (Server.einst.getHomeDir() + "/gobos/" + (0xff&datenAktuell[i]) + ".png");
							System.out.println("Loading " + bild.getAbsolutePath());
							if (bild.exists()) {
								try {
									gobo.setImage(new Image(bild.toURI().toURL().toString()));
								} catch (MalformedURLException e) {
									e.printStackTrace();
								}
							}
						} else {
							
						}
						break;
					case 5: //Red
						
						break;
					case 6: //Green
						
						break;
					case 7: //Blue
						
						break;
					case 8: //xPos
						gobo.setX(((0xff&datenAktuell[i])/255.0)*Gui.INSTANCE.scene.getWidth());
						break;
					case 9: //yPos
						gobo.setY(((0xff&datenAktuell[i])/255.0)*Gui.INSTANCE.scene.getHeight());
						break;
					case 10: //Dimmer
						gobo.setOpacity((0xff&datenAktuell[i])/255.0);
						break;
					case 11: //RotOffset
						gobo.setRotate(((0xff&datenAktuell[i])/255.0)*360.0);
						break;
					}
					datenAlt[i] = datenAktuell[i];
				}
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

}
