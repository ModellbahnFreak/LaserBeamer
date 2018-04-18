package sequenceNew;

import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ObjProperties {
	private ArrayList<Property> eigensch;
	private Node obj = null;
	
	public ObjProperties (ArrayList<Property> _eigensch, Node _obj) {
		eigensch = _eigensch;
		obj = _obj;
	}

	public ObjProperties (Node _obj) {
		obj = _obj;
		eigensch = new ArrayList<Property>();
	}
	
	public void addProperty (String name) {
		boolean exist = false;
		for (Property eig : eigensch) {
			if (eig.getName().equals(name)) {
				exist = true;
			}
		}
		if (!exist) {
			ValueSet Eigenschaft = castProp(name);
			if (Eigenschaft != null) {
				eigensch.add(new Property(Eigenschaft));
			}
		}
	}
	
	private ValueSet castProp(String name) {
		ValueSet setter = null;
		if (obj instanceof ImageView) {
			setter = castImageView(name);
		} else if (obj instanceof MediaView) {
			setter = castMediaView(name);
		} else if (obj instanceof Rectangle) {
			setter = castRectangle(name);
		} else if (obj instanceof Text) {
			setter = castText(name);
		} else if (obj instanceof Circle) {
			setter = castCircle(name);
		} else if (obj instanceof Line) {
			setter = castLine(name);
		}		
		return setter;
	}

	private ValueSet castLine(String name) {
		switch (name) {
		case "xPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Line)obj).setStartX(value);
				}
				@Override
				public String getName() {
					return "xPos";
				}
			};
		case "yPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Line)obj).setStartY(value);
				}
				@Override
				public String getName() {
					return "yPos";
				}
			};
		case "xEnd":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Line)obj).setEndX(value);
				}
				@Override
				public String getName() {
					return "xEnd";
				}
			};
		case "yEnd":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Line)obj).setEndY(value);
				}
				@Override
				public String getName() {
					return "yEnd";
				}
			};
		case "opacity":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Line)obj).setOpacity(value);
				}
				@Override
				public String getName() {
					return "opacity";
				}
			};
		}
		return null;
	}

	private ValueSet castCircle(String name) {
		switch (name) {
		case "xPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Circle)obj).setCenterX(value);
				}
				@Override
				public String getName() {
					return "xPos";
				}
			};
		case "yPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Circle)obj).setCenterY(value);
				}
				@Override
				public String getName() {
					return "yPos";
				}
			};
		case "radius":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Circle)obj).setRadius(value);
				}
				@Override
				public String getName() {
					return "radius";
				}
			};
		case "opacity":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Circle)obj).setOpacity(value);
				}
				@Override
				public String getName() {
					return "opacity";
				}
			};
		}
		return null;
	}

	private ValueSet castText(String name) {
		switch (name) {
		case "xPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Text)obj).setX(value);
				}
				@Override
				public String getName() {
					return "xPos";
				}
			};
		case "yPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Text)obj).setY(value);
				}
				@Override
				public String getName() {
					return "yPos";
				}
			};
		case "size":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Text)obj).setFont(new Font(((Text)obj).getFont().getName(), value));
				}
				@Override
				public String getName() {
					return "size";
				}
			};
		case "opacity":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Text)obj).setOpacity(value);
				}
				@Override
				public String getName() {
					return "opacity";
				}
			};
		}
		return null;
	}

	private ValueSet castRectangle(String name) {
		switch (name) {
		case "xPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Rectangle)obj).setX(value);
				}
				@Override
				public String getName() {
					return "xPos";
				}
			};
		case "yPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Rectangle)obj).setY(value);
				}
				@Override
				public String getName() {
					return "yPos";
				}
			};
		case "width":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Rectangle)obj).setWidth(value);
				}
				@Override
				public String getName() {
					return "width";
				}
			};
		case "height":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Rectangle)obj).setWidth(value);
				}
				@Override
				public String getName() {
					return "height";
				}
			};
		case "opacity":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((Rectangle)obj).setOpacity(value);
				}
				@Override
				public String getName() {
					return "opacity";
				}
			};
		}
		return null;
	}

	private ValueSet castMediaView(String name) {
		switch (name) {
		case "xPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((MediaView)obj).setX(value);
				}
				@Override
				public String getName() {
					return "xPos";
				}
			};
		case "yPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((MediaView)obj).setY(value);
				}
				@Override
				public String getName() {
					return "yPos";
				}
			};
		case "width":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((MediaView)obj).setFitWidth(value);
				}
				@Override
				public String getName() {
					return "width";
				}
			};
		case "height":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((MediaView)obj).setFitHeight(value);
				}
				@Override
				public String getName() {
					return "height";
				}
			};
		case "opacity":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((MediaView)obj).setOpacity(value);
				}
				@Override
				public String getName() {
					return "opacity";
				}
			};
		}
		return null;
	}

	private ValueSet castImageView(String name) {
		switch (name) {
		case "xPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((ImageView)obj).setX(value);
				}
				@Override
				public String getName() {
					return "xPos";
				}
			};
		case "yPos":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((ImageView)obj).setY(value);
				}
				@Override
				public String getName() {
					return "yPos";
				}
			};
		case "width":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((ImageView)obj).setFitWidth(value);
				}
				@Override
				public String getName() {
					return "width";
				}
			};
		case "height":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((ImageView)obj).setFitHeight(value);
				}
				@Override
				public String getName() {
					return "height";
				}
			};
		case "opacity":
			return new ValueSet() {
				@Override
				public void setVal(double value) {
					((ImageView)obj).setOpacity(value);
				}
				@Override
				public String getName() {
					return "opacity";
				}
			};
		}
		return null;
	}

	public void addProperty (String name, int FrameNo, double wert, int interpolMode) {
		addProperty(name);
		setFrame(name, FrameNo, wert, interpolMode);
	}
	
	public void setFrame (String name, int FrameNo, double wert, int interpolMode) {
		for (Property eig : eigensch) {
			if (eig.getName().equals(name)) {
				eig.setValue(FrameNo, wert, interpolMode);
			}
		}
	}
	
	public void updateInperpolate() {
		for (Property eig : eigensch) {
			eig.updateInperpolate();
		}
	}
	
	@Override
	public String toString() {
		String erg = "";
		erg += "Eigenschaften von " + getName() + ":\n";
		for (Property eig : eigensch) {
			erg += "\t" + eig.toString() + "\n";
		}
		return erg;
	}

	public int getLength() {
		int maxLen = 0;
		for (Property eig : eigensch) {
			int laenge = eig.getLength();
			if (laenge > maxLen) {
				maxLen = laenge;
			}
		}
		return maxLen;
	}
	
	public String getName() {
		return obj.getId();
	}
	
	public Node getObj() {
		return obj;
	}

	public void delFrame(String propName, int frameNo) {
		for (Property eig : eigensch) {
			if (eig.getName().equals(propName)) {
				eig.delFrame(frameNo);
			}
		}
	}
	
	public void clearFrames(String propName) {
		for (Property eig : eigensch) {
			if (eig.getName().equals(propName)) {
				eig.clearFrames();
			}
		}
	}
	
	public void delProperty(String propName) {
		for (Property eig : eigensch) {
			if (eig.getName().equals(propName)) {
				eigensch.remove(eig);
			}
		}
	}

	public void playFrame(int frame) {
		for (Property eig : eigensch) {
			eig.playFrame(frame);
		}
	}
}
