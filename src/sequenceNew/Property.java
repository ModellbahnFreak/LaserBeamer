package sequenceNew;

import java.util.ArrayList;

public class Property {
	private ValueSet property = null; 
	public ArrayList<Frame> values = null;
	
	public Property (ValueSet _property) {
		property = _property;
		values = new ArrayList<Frame>();
	}
	
	public void setValue(int FrameNo, double value, int interpolMode) {
		if (FrameNo > values.size()) {
			for (long i = values.size(); i < FrameNo; i++) {
				values.add(new Frame(0, false));
			}
			values.add(new Frame(value, true, interpolMode));
		} else if (FrameNo == values.size()) {
			values.add(new Frame(value, true, interpolMode));
		} else if (FrameNo >= 0) {
			values.set(FrameNo, new Frame(value, true, interpolMode));
		}
	}
	
	public void delFrame(int FrameNo) {
		if (FrameNo < values.size()-1) {
			values.set(FrameNo, new Frame(0, false));
		} else if (FrameNo == values.size()-1) {
			values.remove(FrameNo);
		}
	}
	
	public void clearFrames() {
		values.clear();
	}
	
	public ValueSet getProperty() {
		return property;
	}
	
	public String getName() {
		return property.getName();
	}
	
	public void updateInperpolate() {
		int interpolMode = 0;
		double a = 1;
		double c = 0;
		//y=a*x^n+c
		int nextKeyframe = -1;
		int lastKeyframe = -1;
		for (int i = 0; i < values.size(); i++) {
			Frame bild = values.get(i);
			if (bild.isKeyframe) {
				interpolMode = bild.interpolMode;
				a = 1;
				c = bild.value;
				lastKeyframe = i;
				nextKeyframe = -1;
				for (int j = lastKeyframe+1; j < values.size() && nextKeyframe == -1; j++) {
					Frame NextBild = values.get(j);
					if (NextBild.isKeyframe) {
						nextKeyframe = j;
						if (interpolMode == 1) {
							a = (NextBild.value-c)/(j-lastKeyframe);
						} else if (interpolMode == 2) {
							a = (NextBild.value-c)/((j-lastKeyframe)*(j-lastKeyframe));
						}
					}
				}
			} else if (nextKeyframe >= 0) {
				double newValue = 0;
				switch (interpolMode) {
				case 1:
					newValue = a*(i-lastKeyframe)+c;
					break;
				case 2:
					newValue = a*(i-lastKeyframe)*(i-lastKeyframe)+c;
					break;
				default:
					
					break;
				}
				bild.value = newValue;
			}
		}
	}
	
	@Override
	public String toString() {
		String erg = "";
		erg += "Eigenschaft " + getName() + ":\n\t";
		for (int i = 0; i < values.size(); i++) {
			Frame bild = values.get(i);
			erg += i + ":" + bild.value + "; ";
		}
		return erg;
	}

	public int getLength() {
		return values.size();
	}

	public void playFrame(int frame) {
		property.setVal(values.get(frame).value);
	}
}
