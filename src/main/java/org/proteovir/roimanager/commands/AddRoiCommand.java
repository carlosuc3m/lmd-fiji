package org.proteovir.roimanager.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.proteovir.roimanager.RoiManager;

import ai.nets.samj.annotation.Mask;

public class AddRoiCommand implements Command {
	private RoiManager roiManager;
	private final List<Mask> polys;
	private String shape = "";
	private int promptCount = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
	private String modelName = "";
  
	public AddRoiCommand(RoiManager roiManager, List<Mask> polys) {
		this.roiManager = roiManager;
		this.polys = polys;
	}
	
	public void setPromptShape(String shape) {
		this.shape = shape;
	}
	
	public void setPromptCount(int promptCount) {
		this.promptCount = promptCount;
	}
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public List<Mask> getMasks(){
		return polys;
	}
  
	@Override
	public void execute() {
		int resNo = 1;
		for (Mask m : polys) {
			String name = promptCount + "." + (resNo ++) + "_"+shape + "_" + modelName;
			if (shape.equals("") && modelName.equals(""))
				name = "" + promptCount;
			else if (modelName.equals(""))
				name = promptCount + "." + (resNo) + "_"+shape;
			else if (shape.equals(""))
				name = promptCount + "." + (resNo) + "_"+modelName;
				
			m.setName(name);
		}
	}
  
	@Override
	public void undo() {
		try {
			for (Mask rr2 : polys) {
		    	for (int n = this.roiManager.getROIsNumber() - 1; n >= 0; n --) {
		    		Mask rr = roiManager.getRoisAsArray()[n];
	    			if (!Arrays.equals(rr.getContour().xpoints, rr2.getContour().ypoints))
	    				continue;
	    			if (!Arrays.equals(rr.getContour().xpoints, rr2.getContour().ypoints))
	    				continue;
	    			roiManager.delete(n);
		    		break;		    		
		    	}
				
			}
		} catch (Exception ex) {
    		ex.printStackTrace();
    	}
	}
}