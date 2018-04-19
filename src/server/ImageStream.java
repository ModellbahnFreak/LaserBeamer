package server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class ImageStream implements Runnable {
	private ServerSocket server = null;
	private int port = 8083;
	private Socket socket = null;
	private PrintStream raus;
	private boolean canceled = false;

	public ImageStream(int StreamPort) {
		port = StreamPort;
	}

	public void init() {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Socket creation Fehler");
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	private void verbinde() {
		try {
			System.out.println("Stream: Waiting");
			socket = server.accept();
			System.out.println("Stream: Connection");
			raus = new PrintStream(socket.getOutputStream());
			sendStream();
			System.out.println("Stream: Finished");
		} catch (IOException e) {
			System.out.println("Stream: IOFehler (Vermutl. Verbindung beendet)");
			// e.printStackTrace();
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					System.out.println("Stream: Closing");
					socket.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
			System.out.println("Stream: Completed");
		}
	}

	private void sendStream() throws IOException {
		while (socket != null && socket.isConnected() && !canceled) {
			BufferedImage bild = Server.einst.getScreenshot();
			if (bild != null) {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				// ImageIO.write(bild, "jpg", byteStream);
				ImageIO.write(bild, "gif", byteStream);
				// ImageIO.write(bild, "tiff", byteStream);

				// byte[] size = ByteBuffer.allocate(4).putInt(byteStream.size()).array();
				// raus.write(size);

				byte[] size = ByteBuffer.allocate(4).putInt(byteStream.size()).array();
				raus.write(size);
				raus.write(byteStream.toByteArray());
				raus.flush();

				// raus.print(byteStream.size());
				System.out.println("Größe des Bildes: " + byteStream.size());
				// raus.print("\n\r");
				// raus.write(byteStream.toByteArray());
				// raus.flush();
				System.out.println("Flushed: " + System.currentTimeMillis());
				/*
				 * raus.print(toSend); raus.write(null);
				 */
				// raus.print("402:\r\n");
			}
		}
	}

	@Override
	public void run() {
		init();
		System.out.println("Stream: Server set up");
		while (!canceled) {
			verbinde();
		}
		System.out.println("Stream: Server closed");
		canceled = false;
	}
	
	public void cancel() {
		canceled = true;
	}
}
