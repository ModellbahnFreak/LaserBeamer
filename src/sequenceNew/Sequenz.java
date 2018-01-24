package sequenceNew;

import java.util.ArrayList;

public class Sequenz implements Runnable {
	int length = 10;
	private ArrayList<Frame> frames;
	private int msPerFrame = (int)(1.0/25.0);
	private Thread SeqT = null;
	private boolean TreadActive = false;
	
	public Sequenz(int _length) {
		length = _length;
		frames = new ArrayList<Frame>(length);
	}
	public Sequenz(int _msPerFrame, int _length) {
		length = _length;
		msPerFrame = _msPerFrame;
		frames = new ArrayList<Frame>(length);
	}
	
	public void setFrame(int frameNum, Frame frame) {
		frames.set(frameNum, frame);
	}
	
	public void play() {
		Thread SeqT = new Thread(this);
		SeqT.setDaemon(true);
		SeqT.setName("SequenzThread");
		SeqT.start();
	}
	
	@Override
	public void run() {
		
	}
}
