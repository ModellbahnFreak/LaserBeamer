package webserver.ws;

public interface SocketSendReceive {
public void send(String text);
public String popReceive();
public boolean hasRecv();
public String popRecvBlocking();
}
