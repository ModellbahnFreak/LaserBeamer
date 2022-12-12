package de.modellbahnfreak.laserbeamer.webserver.http;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;

public interface SocketCallback {
	public void startWsHandler(Socket _con, BufferedReader _in, PrintStream _out);
}
