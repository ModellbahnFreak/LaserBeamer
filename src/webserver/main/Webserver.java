package webserver.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import webserver.http.HttpListener;

public class Webserver {

	/*public static void main(String[] args) {
		HttpListener listen = new HttpListener();
		Thread tListen = new Thread(listen);
		tListen.setName("Http-Listener");
		tListen.start();
		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(System.in));
			while (!Thread.currentThread().isInterrupted()) {
				String s = listen.sendRecv.popReceive();
				while (s != null) {
					System.err.println(s);
					s = listen.sendRecv.popReceive();
				}
				listen.sendRecv.send(buf.readLine());
			}
			buf.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			tListen.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*LinkedList<String> test = new LinkedList<String>();
		test.add("1");
		test.add("2");
		test.add("3");
		test.add("4");
		while (!test.isEmpty()) {
			System.out.println(test.pop());
		}
	}*/

}
