package de.modellbahnfreak.laserbeamer.webserver.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.FileNameMap;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.modellbahnfreak.laserbeamer.webserver.main.Sha1;

public class HttpRequest implements Runnable {
	
	private static boolean useResource = true;

	private Socket con = null;
	private boolean finished = false;
	private final SocketCallback wsCallback;

	public HttpRequest(Socket newCon, SocketCallback wsCallb) {
		con = newCon;
		wsCallback = wsCallb;
	}

	@Override
	public void run() {
		BufferedReader bufIn = null;
		PrintStream out = null;
		int connectionType = 0; //0: Standard(=http), 1: Http, 2: Websocket
		try {
			bufIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
			out = new PrintStream(con.getOutputStream());
			String s;
			int response = 0;
			/*int httpVersion = 0;
			int httpSubversion = 0;*/
			boolean sendResponsePage = true;
			HashMap<String, String> keyVal = new HashMap<String, String>();
			String getPath = "";
			while ((s = bufIn.readLine()) != null && !s.isEmpty()) {
				int getIndex = s.indexOf("GET");
				int httpIndex = s.indexOf("HTTP");
				if (getIndex >= 0 && httpIndex >= 0) {
					getPath = s.substring(4, httpIndex - 1);
					/*int dotIndex = s.indexOf(".", httpIndex + 5);
					httpVersion = Integer.valueOf(s.substring(httpIndex + 5, dotIndex));
					httpSubversion = Integer.valueOf(s.substring(dotIndex + 1).trim());*/
					// System.out.println(con.getInetAddress().getHostAddress() + ": " + getPath);
				} else {
					String[] param = s.split(":");
					keyVal.put(param[0].trim(), param[1].trim());
				}
				/*
				 * if (!s.isEmpty()) { System.out.println(con.getInetAddress().getHostAddress()
				 * + ": " + s); }
				 */
			}
			// System.out.println("Finished reading-Analysing");
			
			HashMap<String, String> responseKeyVal = new HashMap<String, String>();

			if (getPath.endsWith("/")) {
				getPath += "index.html";
			}
			if (getPath.indexOf("..") >= 0 && response == 0) {
				response = 400;
			}
			if (response == 0) {
				String connection = keyVal.get("Connection");
				if (connection == null) {
					connection = "";
				}
				String[] connections = connection.toLowerCase().replace(" ", "").split(",");
				if (contains(connections, "upgrade")) {
					if ("websocket".equals(keyVal.get("Upgrade"))) {
						//System.out.println("Websocket connection");
						if ("13".equals(keyVal.get("Sec-WebSocket-Version"))) {
							responseKeyVal.put("Upgrade", "websocket");
							responseKeyVal.put("Connection", "Upgrade");
							responseKeyVal.put("Sec-WebSocket-Accept", Sha1.accesToken(keyVal.get("Sec-WebSocket-Key")));
							response = 101;
							sendResponsePage = false;
							connectionType = 2;
							//System.err.println("New WS connection");
						} else {
							responseKeyVal.put("Sec-WebSocket-Version", "13");
							response = 400;
							sendResponsePage = false;
							//System.err.println("Wrong Socket version");
						}
					}
				} else if (contains(connections, "keep-alive")) {
					//System.out.println("HTTP connection");
				} else {
					response = 400;
					System.err.println("Unknown connection");
				}
			} else {
				//System.err.println("Response not 0");
			}

			if (response == 0) {
				response = 200;
			}

			// System.out.println("Responding");
			String responseStr = "";
			if (response == 200) {
				getPath = "client/content" + getPath;
				responseStr = "OK";
			} else if (response >= 400 && response < 500) {
				getPath = "client/err/" + response + ".html";
				responseStr = "Bad Request";
			} else if (response == 101) {
				responseStr = "Switching Protocols";
			} else {
				response = 500;
				getPath = "client/err/" + response + ".html";
				responseStr = "Internal Server Error";
			}
			String ausg = "HTTP/1.1 " + response + " " + responseStr + "\r\n";
			Iterator<Entry<String, String>> it = responseKeyVal.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, String> param = (Entry<String, String>) it.next();
				ausg += param.getKey() + ": " + param.getValue() + "\r\n";
			}
			if (sendResponsePage) {
				//System.out.println("Pfad: " + getPath)
				//File sendFile = new File(getPath);
				String mimeType = "text/html";
				InputStream datStream = null;
				byte[] datei = null;
				/*if (!sendFile.exists()) {
					System.err.println("404 HTTP-Version " + httpVersion + "." + httpSubversion);
					if (sendFile.getParentFile().exists()) {
						response = 404;
						getPath = "client/err/404.html";
						responseStr = "Not Found";
						ausg = "HTTP/1.1 " + response + " " + responseStr + "\r\n";
						sendFile = new File(getPath);
						mimeType = Files.probeContentType(sendFile.toPath());
						datStream = new FileInputStream(getPath);
						datei = new byte[(int)sendFile.length()];
						datStream.read(datei);
						datStream.close();
					} else {
						response = 404;
						getPath = "404.html";
						responseStr = "Not Found";
						ausg = "HTTP/1.1 " + response + " " + responseStr + "\r\n";
						mimeType = "text/html";
						datei = page404.getBytes("UTF-8");
					}
				} else {
					mimeType = Files.probeContentType(sendFile.toPath());
					datStream = new FileInputStream(getPath);
					datei = new byte[(int)sendFile.length()];
					datStream.read(datei);
					datStream.close();
				}*/
				datStream = getInputStream(getPath);
				if (datStream == null) {
					response = 404;
					getPath = "client/err/404.html";
					responseStr = "Not Found";
					ausg = "HTTP/1.1 " + response + " " + responseStr + "\r\n";
					datStream = getInputStream(getPath);
				}
				if (datStream == null) {
					datei = page404.getBytes("UTF-8");
					mimeType = "text/html";
				} else {
					datei = getFileContent(datStream);
					datStream.close();
					mimeType = getMimeType(getPath, datei);
				}
				SimpleDateFormat datum = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss zzz");
				ausg += "Date: " + datum.format(new Date()) + "\r\n" +
						"Server: LaserBeamer/1.2.3\r\n" +
						"Last-Modified: " + datum.format(new Date()) + "\r\n" +
						"Etag: W/\"1234\"\r\n" +
						"Accept-Ranges: bytes\r\n" + 
						"Content-Length: " + datei.length + "\r\n" + 
						"Connection: close\r\n" +
						"Content-Type: " + mimeType + "\r\n";
				out.print(ausg+"\r\n");
				out.write(datei);
			} else {
				out.print(ausg+"\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (connectionType == 2) {
					if (wsCallback != null) {
						wsCallback.startWsHandler(con, bufIn, out);
					} else {
						bufIn.close();
						out.flush();
						out.close();
						con.close();
					}
				} else {
					bufIn.close();
					out.flush();
					out.close();
					con.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		finished = true;
	}

	private InputStream getInputStream(String getPath) {
		if (useResource) {
			try {
				URL pfad = getClass().getClassLoader().getResource(getPath);
				if (pfad != null) {
					URLConnection con = pfad.openConnection();
					con.setUseCaches(false);
					con.setDefaultUseCaches(false);
					return con.getInputStream();
				}
			} catch (IOException e) {
				//e.printStackTrace();
			}
		} else {
			try {
				return new FileInputStream("htmlSource/" + getPath);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return null;
	}

	public boolean isFinished() {
		return finished;
	}
	
	public boolean contains(String[] arr, String needle) {
		for (String s : arr) {
			if (s.equals(needle)) {
				return true;
			}
		}
		return false;
	}
	
	private byte[] getFileContent(InputStream stream) {//108203ns = 108,203µs = 0,108203ms
		byte[] buffer = new byte[4096];
		byte[] temp = new byte[0];
		byte[] erg = new byte[0];
		int anzBytes = 0;
		try {
			anzBytes = stream.read(buffer);
			while (anzBytes > 0) {
				temp = erg;
				erg = new byte[temp.length+anzBytes];
				System.arraycopy(temp, 0, erg, 0, temp.length);
				System.arraycopy(buffer, 0, erg, temp.length, anzBytes);
				anzBytes = stream.read(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return erg;
	}
	
	private String getMimeType(String fName, byte[] data) {
		String erg = mimeTypen.getContentTypeFor(fName);
		if (erg == null || erg.isEmpty()) {
			erg = URLConnection.guessContentTypeFromName(fName);
			if (erg == null || erg.isEmpty()) {
				InputStream dataStream = new ByteArrayInputStream(data);
				try {
					erg = URLConnection.guessContentTypeFromStream(dataStream);
					dataStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return erg;
	}
	
	private static FileNameMap mimeTypen = new FileNameMap() {
		
		@Override
		public String getContentTypeFor(String fileName) {
			String[] parts = fileName.split("\\.");
			if (parts.length > 0) {
				String ext = parts[parts.length-1].toLowerCase().trim();
				switch (ext) {
				case "htm":
				case "shtml":
				case "html":
					return "text/html";
				case "js":
					return "text/javascript";
				case "css":
					return "text/css";
				case "txt":
					return "text/plain";
				case "ico":
					return "image/x-icon";
				case "jpeg":
				case "jpg":
				case "jpe":
					return "image/jpeg";
				case "png":
					return "image/png";
				case "bmp":
					return "image/bmp";
				case "mpeg":
				case "mpg":
				case "mpe":
				case "mp4":
					return "video/mpeg";
				case "pdf":
					return "application/pdf";
				default:
					System.err.println("Unknown extension: " + ext);
				}
			}
			return null;
		}
	};
	
	private static String page404 = "<html>\r\n" + 
			"<head>\r\n" + 
			"<title>404</title>\r\n" + 
			"</head>\r\n" + 
			"<body style=\"font-family: sans-serif;\">\r\n" + 
			"<h1>404 - Die Client-Daten konnten nicht geladen werden!</h1>\r\n" + 
			"Bitte sicherstellen, dass die Anwendung korrekt ausgef&uuml;hrt wird!\r\n" + 
			"</body>\r\n" + 
			"</html>";
}
