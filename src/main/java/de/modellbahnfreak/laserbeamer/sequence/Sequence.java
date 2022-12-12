package de.modellbahnfreak.laserbeamer.sequence;

import java.util.ArrayList;

import javafx.scene.Node;
import de.modellbahnfreak.laserbeamer.server.Gui;

public class Sequence /* implements Runnable */ {
	private ArrayList<Node> objekte = null;
	private ArrayList<String> objCmds = new ArrayList<String>();
	private ArrayList<Cue> cues = new ArrayList<Cue>();
	private int step = 0;
	private String _Id = "";
	private String _mode = "once";
	private boolean _canceled = false;
	Runnable CueCallb = new Runnable() {
		@Override
		public void run() {
			step++;
			if (_canceled) {
				endSequence();
				_canceled = false;
			}
			if (step >= cues.size() && "loop".equals(_mode)) {
				cues.get(0).launch(objekte);
			} else if (step >= cues.size()) {
				endSequence();
			} else {
				//System.out.println("Launch Cue No. " + step);
				cues.get(step).launch(objekte);
			}
		}
	};
	
	private void endSequence() {
		for (Node obj : objekte) {
			Gui.INSTANCE.delNodeList.add(obj);
		}
		objekte = null;
		step = 0;
	}

	private final DeleteHandler DeleteCallback = new DeleteHandler() {
		@Override
		public void deleteElem(String objName) {
			System.out.println("HAHA No deleting here!");
		}
	};

	public Sequence(ArrayList<Node> GuiObjekte, ArrayList<Cue> cueList) {
		objekte = GuiObjekte;
		cues = cueList;

		for (Cue cue : cues) {
			cue.setOnFinished(CueCallb);
		}
	}
	
	public Sequence(ArrayList<Node> GuiObjekte, ArrayList<Cue> cueList, String mode) {
		this(GuiObjekte, cueList);
		_mode = mode;
	}

	public Sequence(ArrayList<String> cmds, String Id) {
		_Id = Id;

		boolean CueInputAct = false;
		ArrayList<String> cueCmds = null;
		ArrayList<Integer> cueTiming = null;

		for (String cmd : cmds) {
			String[] ToDos = cmd.split(";");
			switch (ToDos[0]) {
			case "de/modellbahnfreak/laserbeamer/sequence":
				SetId(ToDos[1]);
				break;
			case "cue":
				CueInputAct = true;
				cueTiming = new ArrayList<Integer>();
				cueTiming.add(Integer.parseInt(ToDos[1]));// Wait
				cueTiming.add(Integer.parseInt(ToDos[2]));// FadeIn
				cueTiming.add(Integer.parseInt(ToDos[3]));// FadeOut
				cueTiming.add(Integer.parseInt(ToDos[4]));// Active
				cueCmds = new ArrayList<String>();
				break;
			case "cueEnd":
				CueInputAct = false;
				cues.add(new Cue(cueTiming.get(0), cueTiming.get(1), cueTiming.get(2), cueTiming.get(3), cueCmds,
						CueCallb));
				cueCmds = null;
				break;
			default:
				if (NodeCreator.validateCmd(cmd) == true) {
					objCmds.add(cmd);
				} else {
					if (CueInputAct == true) {
						cueCmds.add(cmd);
					}
				}
				break;
			}
		}
	}
	
	public Sequence(ArrayList<String> cmds, String Id, String mode) {
		this (cmds, Id);
		_mode = mode;
	}

	// @Override
	public void run() {
		step = 0;
		// objekteRun = new ArrayList<Node>(objekte);
		// objekteRun = (ArrayList<Node>) objekte.clone();
		CmdsToNodes();
		//System.out.println("Launch Cue No. " + step);
		cues.get(step).launch(objekte);
	}

	private void CmdsToNodes() {
		objekte = new ArrayList<Node>();
		for (String cmd : objCmds) {
			Node createdNode = NodeCreator.cmdToNode(cmd, DeleteCallback);
			if (createdNode != null) {
				objekte.add(createdNode);
			}
		}
		for (Node obj : objekte) {
			Gui.INSTANCE.addNodeList.add(obj);
		}
	}

	public void SetId(String Id) {
		_Id = Id;
	}

	public String GetId() {
		return _Id;
	}
	
	public void cancel() {
		_canceled = true;
	}

}
