package webserver.ws;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javafx.scene.Node;
import sequenceNew.Sequenz;
import server.Server;
import webserver.http.ProcessCallback;
import webserver.main.SocketProcess;

public class SocketHandler {
	
	private Socket con = null;
	private BufferedReader in = null;
	private PrintStream out = null;
	private Thread recvT = null;
	private Thread sendT = null;
	private Thread processT = null;
	private boolean cancel = false;
	private boolean finishedClosing = false;
	private LinkedList<SocketPacket> senden = new LinkedList<SocketPacket>();
	private LinkedList<String> recvText = new LinkedList<String>();
	private final ProcessCallback processCallback;

	public SocketHandler(Socket _con, BufferedReader _in, PrintStream _out, ProcessCallback procCallb) {
		con = _con;
		in = _in;
		out = _out;
		processCallback = procCallb;
	}
	
	public final SocketSendReceive sendRecv = new SocketSendReceive() {
		
		@Override
		public void send(String text) {
			SocketPacket paket = new SocketPacket(text);
			synchronized (senden) {
				senden.add(paket);
				senden.notifyAll();
			}
		}
		
		@Override
		public String popReceive() {
			synchronized (recvText) {
				return recvText.pop();
			}
		}

		@Override
		public boolean hasRecv() {
			if (recvText.size() > 0) {
				return true;
			}
			return false;
		}

		@Override
		public String popRecvBlocking() {
			synchronized (recvText) {
				if (recvText.size() > 0) {
					return recvText.pop();
				} else {
					try {
						recvText.wait(1000);
					} catch (InterruptedException e) {
						System.out.println("Warten unterbrochen");
					}
				}
			}
			return null;
		}
	};
	
	public void handle() {
		if (con != null && in != null && out != null && recvT == null && sendT == null) {
			recvT = new Thread(recv);
			recvT.setDaemon(true);
			recvT.setName("WebsocketRecv-"+con.getInetAddress().getHostAddress());
			recvT.start();
			sendT = new Thread(send);
			sendT.setDaemon(true);
			sendT.setName("WebsocketSend-"+con.getInetAddress().getHostAddress());
			sendT.start();
			SocketProcess verarb = new SocketProcess(sendRecv, Server.elem, Server.sequenzen, processCallback);
			processT = new Thread(verarb);
			processT.setDaemon(true);
			processT.setName("WebsocketProcess-"+con.getInetAddress().getHostAddress());
			processT.start();
			System.out.println("Started Ws handlers");
		}
	}
	
	public boolean isFinished() {
		if (recvT != null && sendT != null) {
			return !recvT.isAlive()&&!sendT.isAlive();
		} else {
			return false;
		}
	}
	
	private Runnable recv = new Runnable() {

		@Override
		public void run() {
			try {
				InputStream readBytes = con.getInputStream();
				int index = 0;
				long len = 0;
				int dataIndex = 0;
				byte[] dataGes = new byte[0];
				byte opcode = -1;
				String fileName = "";
				boolean receivedClose = false;
				boolean sentClose = false;
				while(con.isConnected() && !(receivedClose && sentClose)) {
					try {
						if (index == 0 && len == 0) {
							boolean isMasked = false;
							boolean isFin = false;
							int lenType = 0; //0: 7 bit, 1: 7+16 bit, 2: 7+64 bit
							byte[] maskKey = null;
							byte[] buffer = null;
							
							buffer = new byte[2];
							while (index < 2) {
								index += readBytes.read(buffer, index, 2-index);
							}
							if ((buffer[0] & 0x0f) > 0) {
								opcode = (byte) (buffer[0] & 0x0f);
							}
							int anzHeader = 0;
							int len1 = (buffer[1] & 0b01111111)&0xff;
							if (len1 < 126) {
								lenType = 0;
								len = len1;
							} else if (len1 == 126) {
								lenType = 1;
								anzHeader += 2;
							} else if (len1 == 127) {
								lenType = 2;
								anzHeader += 8;
							}
							if ((buffer[1] & 0b10000000) == 0b10000000) {
								isMasked = true;
								anzHeader += 4;
							}
							if ((buffer[0] & 0b10000000) == 0b10000000) {
								isFin = true;
							}
							index = 0;
							
							if (anzHeader > 0) {
								buffer = new byte[anzHeader];
								while (index < anzHeader) {
									index += readBytes.read(buffer, index, anzHeader-index);
								}
								index = 0;
								if (lenType == 1) {
									long len2 = ( ( ((int)buffer[0]) &0xff ) << 8 ) | ( ((int)buffer[1]) &0xff );
									len = len2;
									index = 2;
								} else if (lenType == 2) {
									long len3 = ( ( ((int)buffer[0]) &0xff ) << 56 ) | (( ((int)buffer[1]) &0xff ) << 48 ) | (( ((int)buffer[2]) &0xff ) << 40 ) | (( ((int)buffer[3]) &0xff ) << 32 ) | (( ((int)buffer[4]) &0xff ) << 24 ) | (( ((int)buffer[5]) &0xff ) << 16 ) | (( ((int)buffer[6]) &0xff ) << 8 ) | ( ((int)buffer[7]) &0xff );
									len = len3;
									index = 8;
								}
								if (isMasked == true) {
									maskKey = new byte[4];
									maskKey[0] = buffer[index];
									maskKey[1] = buffer[index+1];
									maskKey[2] = buffer[index+2];
									maskKey[3] = buffer[index+3];
								}
							}
							
							dataIndex = 0;
							byte[] data = new byte[(int) len];
							while (dataIndex < len) {
								dataIndex += readBytes.read(data, dataIndex, (int)(len-dataIndex));
							}
							if (isMasked) {
								for (dataIndex = 0; dataIndex < len; dataIndex++) {
									data[dataIndex] = (byte) (data[dataIndex] ^ maskKey[dataIndex%4]);
								}
							}
							dataGes = combineArr(dataGes, data);
							if (isFin) {
								switch (opcode) {
								case 1:
									fileName = "";
									String textData = new String(dataGes);
									if (textData.startsWith("BinaryFileName:")) {
										fileName = textData.substring(15);
									} else if ("Hallo".equals(textData)) {
										SocketPacket paket = new SocketPacket("Welt");
										synchronized (senden) {
											senden.add(paket);
											senden.notifyAll();
										}
									} else {
										System.out.println("Text-Data: " + textData);
										synchronized (recvText) {
											recvText.add(textData);
											recvText.notifyAll();
										}
									}
									break;
								case 2:
									System.out.println("Binary-Data; Filename: " + fileName);
									if (!fileName.isEmpty()) {
										OutputStream out = new FileOutputStream("upload/" + fileName);
										out.write(dataGes);
										out.flush();
										out.close();
									}
									break;
								case 9:
									SocketPacket paket = new SocketPacket(dataGes, (byte) 0xA);
									synchronized (senden) {
										senden.add(paket);
										senden.notifyAll();
									}
									System.out.println("Ping");
									break;
								case 0xA:
									System.out.println("Pong");
									break;
								case 0x8:
									System.out.println("Closing requested due to: " + new String(dataGes));
									receivedClose = true;
									if (!sentClose) {
										SocketPacket paket2 = new SocketPacket(dataGes, (byte) 0x8);
										synchronized (senden) {
											senden.add(paket2);
											senden.notifyAll();
										}
										sentClose = true;
									}
									break;
								}
								dataGes = new byte[0];
							}
							len = 0;
							index = 0;
						} else {
							System.err.println("We've got a problem");
						}
						if (cancel) {
							SocketPacket paket = new SocketPacket(new byte[0], (byte) 0x8);
							synchronized (senden) {
								senden.add(paket);
								senden.notifyAll();
							}
							sentClose = true;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (sentClose && receivedClose) {
					processT.interrupt();
					finishedClosing = true;
				}
				//System.out.println("Ending recv thread");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	};
	
	private Runnable send = new Runnable() {
		
		@Override
		public void run() {
			try {
				OutputStream writeBytes = con.getOutputStream();
				while(con.isConnected() && !finishedClosing) {
					try {
						SocketPacket nextPacket = null;
						synchronized (senden) {
							nextPacket = senden.removeFirst();
						}
						if (nextPacket != null && nextPacket.isValid()) {
							writeBytes.write(nextPacket.getWsPacket());
						} else {
							synchronized (senden) {
								senden.wait(5000);
							}
						}
					} catch (NoSuchElementException e) {
						synchronized (senden) {
							try {
								senden.wait(5000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					synchronized (senden) {
						while (con.isConnected() && senden.size() > 0) {
							try {
								SocketPacket nextPacket = senden.removeFirst();
								if (nextPacket != null && nextPacket.isValid()) {
									writeBytes.write(nextPacket.getWsPacket());
								}
							} catch (NoSuchElementException e) {
								
							}
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				out.flush();
				writeBytes.flush();
				in.close();
				out.close();
				writeBytes.close();
				con.close();
				System.out.println("Closed socket - Ending send thread");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	};

	private static byte[] combineArr(byte[] arr1, byte[] arr2) {
		byte[] erg = new byte[arr1.length+arr2.length];
		System.arraycopy(arr1, 0, erg, 0, arr1.length);
		System.arraycopy(arr2, 0, erg, arr1.length, arr2.length);
		return erg;
	}

}
