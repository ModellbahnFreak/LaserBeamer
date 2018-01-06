package server;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Queue;

import javax.imageio.ImageIO;

public class Network extends Thread {
	private final Queue<String> _comm;
	private final Queue<String> _SendComm;
	private ServerSocket server = null;
	private int port = 8082;
	Socket socket = null;
	private BufferedReader rein;
	private PrintStream raus;

	Runnable checkSend = new Runnable() {

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				checkSend();
			}
		}
	};

	public Network(Queue<String> comm, Queue<String> SendComm, int ServerPort) {
		this._comm = comm;
		this._SendComm = SendComm;
		setDaemon(true);
		port = ServerPort;
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

		// while (!isInterrupted()) {
		Thread checkSendT = new Thread(checkSend);
		try {
			System.out.println("Waiting");
			socket = server.accept();
			System.out.println("Connection");
			checkSendT.setDaemon(true);
			checkSendT.setName("NetzwerkCheckSend");
			checkSendT.start();
			reinRaus();
			System.out.println("Finished");
		} catch (IOException e) {
			System.out.println("IOFehler (Vermutl. Verbindung beendet)");
			// e.printStackTrace();
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					System.out.println("Closing");
					socket.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
			checkSendT.interrupt();
			synchronized (_comm) {
				_comm.add("connection;close");
			}
			System.out.println("Completed");
		}
		// }
	}

	private void reinRaus() throws IOException {
		rein = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		raus = new PrintStream(socket.getOutputStream());
		String s;
		// while (socket.isBound()) {
		while ((s = rein.readLine()) != null) {
			// raus.println(s);
			// raus.println("OK");
			// System.out.println("Empfangen: " + s);
			if ("exit".equals(s)) {
				rein.close();
				raus.close();
				socket.close();
			} else {
				synchronized (_comm) {
					if (!("connection;close".equals(s))) {
						_comm.add(s);
					}
				}
			}
			/*try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			/*
			 * String toSend = null; synchronized (_SendComm) { toSend = _SendComm.poll(); }
			 * if (toSend != null) { raus.println(toSend); }
			 */
		}
		// }
	}

	private void checkSend() {
		if (socket != null) {
			if (!socket.isConnected()) {
			} else {
				String toSend = null;
				synchronized (_SendComm) {
					toSend = _SendComm.poll();
				}
				if (toSend != null) {
					raus.print(toSend + "\r\n");
				}
				/*BufferedImage bild = Server.einst.getScreenshot();
				if (bild != null) {
					raus.print("401:");
					System.out.println("Sended State");
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					try {
						//ImageIO.write(bild, "jpg", byteStream);
						ImageIO.write(bild, "gif", byteStream);
						
						//byte[] size = ByteBuffer.allocate(4).putInt(byteStream.size()).array();
						//raus.write(size);
						raus.print(byteStream.size());
						System.out.println("Größe des Bildes: " + byteStream.size());
						raus.print("\n\r");
						raus.write(byteStream.toByteArray());
						//raus.flush();
						System.out.println("Flushed: " + System.currentTimeMillis());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//raus.print(toSend);
					//raus.write(null);
					//raus.print("402:\r\n");
				}*/
			}
		}
	}

	@Override
	public void run() {
		init();
		System.out.println("Server set up");
		while (!isInterrupted()) {
			verbinde();
		}
		System.out.println("Server closed");
	}
}
