package server;

import java.awt.image.BufferedImage;

public class Settings {

	private boolean _screenshActive = false;
	private BufferedImage _screensh;
	private boolean newScreensh = false;

	Settings() {
	}

	public boolean getStateScreenshActive() {
		return _screenshActive;
	}

	public void setStateScreenshActive(boolean Status) {
		_screenshActive = Status;
	}

	public BufferedImage getScreenshot() {
		synchronized (this) {
			if (newScreensh && _screenshActive) {
				newScreensh = false;
				return _screensh;
			} else {
				return null;
			}
		}
	}

	public void setScreenshot(BufferedImage bild) {
		synchronized (this) {
			_screensh = bild;
			newScreensh = true;
		}
	}
}
