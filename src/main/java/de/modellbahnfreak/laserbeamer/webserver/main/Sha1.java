package de.modellbahnfreak.laserbeamer.webserver.main;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Sha1 {
	
	private static String byteArrToHex(byte[] in) {
		String aus = "";
		for (int i = 0; i < in.length; i++) {
			aus += decToSystem(in[i]&0xff, 16);
		}
		return aus;
	}
	
	private static String decToSystem(int zahl, int basis) {
		String zeichen[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		String ausgabe = "";
		while (zahl > 0) {
			int Rest = zahl % basis;
			if (Rest < 10) {
				ausgabe = Rest + ausgabe;
			} else if (Rest < 35) {
				ausgabe = zeichen[Rest-10] + ausgabe;
			}
			zahl = zahl / basis;
		}
		return ausgabe;
	}
	
	private static byte[] calcSha1(String eing) {
		try {
			MessageDigest conv = MessageDigest.getInstance("SHA-1");
			conv.update(eing.getBytes("UTF-8"), 0, eing.length());
			return conv.digest();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String Sha1Hex(String eing) {
		return byteArrToHex(calcSha1(eing));
	}
	
	public static String accesToken(String key) {
		String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		return Base64.getEncoder().encodeToString(calcSha1(key+magicString));
	}

}
