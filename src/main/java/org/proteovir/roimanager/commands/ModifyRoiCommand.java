package org.proteovir.roimanager.commands;

import java.awt.Polygon;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.proteovir.roimanager.RoiManager;

import ai.nets.samj.annotation.Mask;

public class ModifyRoiCommand implements Command {
	private RoiManager roiManager;
	private final List<Mask> polys;
	private HashMap<String, HashMap<String, Polygon>> modsMap = new HashMap<>();
	
	private static final String OLD_KEY = "oldPolygon";
	private static final String NEW_KEY = "newPolygon";
  
	public ModifyRoiCommand(RoiManager roiManager, List<Mask> polys) {
		this.roiManager = roiManager;
		this.polys = polys;
	}
	
	public void setOldContour(String id, Polygon oldContour) {
		if (modsMap.get(id) == null) {
			HashMap<String, Polygon> idMap = new HashMap<String, Polygon>();
			idMap.put(OLD_KEY, null);
			idMap.put(NEW_KEY, null);
		}
		modsMap.get(id).put(OLD_KEY, oldContour);
	}
	
	public void setNewContour(String id, Polygon newContour) {
		if (modsMap.get(id) == null) {
			HashMap<String, Polygon> idMap = new HashMap<String, Polygon>();
			idMap.put(OLD_KEY, null);
			idMap.put(NEW_KEY, null);
		}
		modsMap.get(id).put(NEW_KEY, newContour);
	}
	
	public List<Mask> getMasks(){
		return polys;
	}
  
	@Override
	public void execute() {
		for (Mask m : polys)
			this.roiManager.addRoi(m);
		this.roiManager.updateShowAll();
	}
  
	@Override
	public void undo() {
		for (Mask rr2 : polys) {
	    	for (int n = this.roiManager.getROIsNumber() - 1; n >= 0; n --) {
	    		Mask rr = roiManager.getRoisAsArray()[n];
    			if (!Arrays.equals(rr.getContour().xpoints, rr2.getContour().xpoints))
    				continue;
    			if (!Arrays.equals(rr.getContour().ypoints, rr2.getContour().ypoints))
    				continue;
    			roiManager.delete(n);
	    		break;		    		
	    	}
			
		}
		this.roiManager.updateShowAll();
	}
}