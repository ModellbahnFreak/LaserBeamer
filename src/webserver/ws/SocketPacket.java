package webserver.ws;

public class SocketPacket {
	
	private byte[] _payload = null;
	private byte opcode = 0;
	private byte[] maskKey = null;
	private byte isMasked = 0b0;
	private byte flags = 0b1000;
	
	public SocketPacket() {
		setMasked((byte) 0b0);
	}

	public SocketPacket(String payload) {
		this(payload.getBytes(), (byte) 1);
	}
	
	public SocketPacket(byte[] binData) {
		this(binData, (byte) 2);
	}
	
	public SocketPacket(byte[] payload, byte packetType) {
		this();
		_payload = payload;
		opcode = packetType;
	}
	
	public void setMasked(byte on) {
		if (on > 0) {
			isMasked = 0b1;
			maskKey = new byte[4];
			for (int i = 0; i < 4; i++) {
				maskKey[i] = (byte) (Math.random()*256-127);
			}
		} else {
			isMasked = 0b0;
			maskKey = null;
		}
	}
	
	public byte[] getWsPacket() {
		if (_payload != null) {
			int len = 2;
			if (_payload.length < 126) {
			} else if (_payload.length >= 126 && _payload.length < 65536) {
				len += 2;
			} else if (_payload.length >= 126 && _payload.length < 65536) {
				len += 8;
			}
			if (isMasked > 0) {
				len += 4;
			}
			len += _payload.length;
			byte[] paket = new byte[len];
			int index = 0;
			paket[0] = (byte) (flags<<4 | opcode);
			if (_payload.length < 126) {
				paket[1] = (byte) (isMasked<<7 | (byte)(_payload.length));
				index = 2;
			} else if (_payload.length >= 126 && _payload.length < 65536) {
				paket[1] = (byte) (isMasked<<7 | (byte)(126));;
				paket[2] = (byte) (_payload.length>>8);
				paket[3] = (byte) (_payload.length);
				index = 4;
			} else if (_payload.length >= 126 && _payload.length < 65536) {
				paket[1] = 0;//(byte) (isMasked<<7 | (byte)(127));;
				paket[2] = 0;//(byte) (_payload.length>>56);
				paket[3] = 0;//(byte) (_payload.length>>48);
				paket[4] = 0;//(byte) (_payload.length>>40);
				paket[5] = 0;//(byte) (_payload.length>>32);
				paket[6] = (byte) (_payload.length>>24);//25-32
				paket[7] = (byte) (_payload.length>>16);//17-24
				paket[8] = (byte) (_payload.length>>8);//9-16
				paket[9] = (byte) (_payload.length);//0-8
				index = 10;
			}
			if (isMasked > 0) {
				paket[index] = maskKey[0];
				paket[index+1] = maskKey[1];
				paket[index+2] = maskKey[2];
				paket[index+3] = maskKey[3];
				index += 4;
			}
			if (isMasked > 0) {
				//Falsch
				System.arraycopy(_payload, 0, paket, index, _payload.length);
			} else {
				System.arraycopy(_payload, 0, paket, index, _payload.length);
			}
			return paket;
		} else {
			return new byte[0];
		}
	}
	
	public byte getOpcode() {
		return opcode;
	}
	
	public boolean isValid() {
		if (_payload != null) {
			return true;
		}
		return false;
	}
}
