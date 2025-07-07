package org.proteovir.roimanager.commands;

import java.util.Arrays;
import java.util.List;

import org.proteovir.roimanager.RoiManager;

import ai.nets.samj.annotation.Mask;

public class DeleteRoiCommand implements Command {
	private RoiManager roiManager;
	private final List<Mask> polys;
  
	public DeleteRoiCommand(RoiManager roiManager, List<Mask> polys) {
		this.roiManager = roiManager;
		this.polys = polys;
	}
	
	public void execute() {
		try {
			for (Mask rr2 : polys) {
		    	for (int n = this.roiManager.getROIsNumber() - 1; n >= 0; n --) {
		    		Mask rr = roiManager.getRoisAsArray()[n];
	    			if (!Arrays.equals(rr.getContour().xpoints, rr2.getContour().xpoints))
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
	
	public void undo() {
		for (Mask m : polys) {
			roiManager.addRoi(m);;
		}
	}
	
	@Override
	public List<Mask> getMasks(){
		return polys;
	}
}
