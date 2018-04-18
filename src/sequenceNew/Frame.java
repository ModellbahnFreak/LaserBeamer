package sequenceNew;

public class Frame {
	public double value;
	public boolean isKeyframe = false;
	public int interpolMode = 0;
	
	public Frame (double _value, boolean _isKeyframe) {
		value = _value;
		isKeyframe = _isKeyframe;
	}
	
	/***
	 * 
	 * @param _value
	 * @param _isKeyframe
	 * @param _interpolMode 0: step; 1: linear; 2: cubic
	 */
	public Frame (double _value, boolean _isKeyframe, int _interpolMode) {
		this(_value, _isKeyframe);
		interpolMode = _interpolMode;
	}
}
