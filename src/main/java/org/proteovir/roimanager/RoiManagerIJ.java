package org.proteovir.roimanager;

import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ai.nets.samj.annotation.Mask;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.plugin.OverlayLabels;

public class RoiManagerIJ implements RoiManagerConsumer, RoiListener {
	
	private List<Roi> roiList;
	
	private BiConsumer<Integer,Polygon> modifyRoiCallback;
	
	public RoiManagerIJ() {
		Roi.addRoiListener(this);
	}

	public void setRois(List<Mask> rois) {
		ImagePlus imp = WindowManager.getCurrentImage();
		Overlay overlay = newOverlay();
		roiList = new ArrayList<Roi>();
		for (Mask mm : rois) {
			PolygonRoi roi = new PolygonRoi(mm.getContour(), PolygonRoi.POLYGON);
			overlay.add(roi);
			roiList.add(roi);
		}
		imp.deleteRoi();
		setOverlay(imp, overlay);
	}

	public void setRois(List<Mask> rois, int ind) {
		ImagePlus imp = WindowManager.getCurrentImage();
		Overlay overlay = newOverlay();
		int i = 0;
		roiList = new ArrayList<Roi>();
		for (Mask mm : rois) {
			PolygonRoi roi = new PolygonRoi(mm.getContour(), PolygonRoi.POLYGON);
			overlay.add(roi);
			roiList.add(roi);
			if (i == ind)
				imp.setRoi(roi);
			i ++;
		}
		setOverlay(imp, overlay);
	}

	@Override
	public void setSelected(Mask mm) {
		ImagePlus imp = WindowManager.getCurrentImage();
		PolygonRoi roi = new PolygonRoi(mm.getContour(), PolygonRoi.POLYGON);
		imp.setRoi(roi, true);
	}

	private Overlay newOverlay() {
		Overlay overlay = OverlayLabels.createOverlay();
		overlay.drawLabels(true);
		if (overlay.getLabelFont()==null && overlay.getLabelColor()==null) {
			overlay.setLabelColor(Color.white);
			overlay.drawBackgrounds(true);
		}
		overlay.drawNames(Prefs.useNamesAsLabels);
		return overlay;
	}

	private void setOverlay(ImagePlus imp, Overlay overlay) {
		if (imp==null)
			return;
		ImageCanvas ic = imp.getCanvas();
		if (ic==null) {
			if (imp.getOverlay()==null)
				imp.setOverlay(overlay);
			return;
		} else {
			imp.setOverlay(overlay);
		}
		ic.setShowAllList(overlay);
		imp.draw();
	}
	
	public void deleteAllRois() {
		
	}

	@Override
	public void roiModified(ImagePlus imp, int id) {
		if (id != RoiListener.MODIFIED && id != RoiListener.MOVED)
			return;
		Roi roi = imp.getRoi();
		int i = 0; 
		for (Roi roi2 : this.roiList) {
			if (roi.equals(roi2)) {
				modifyRoiCallback.accept(i, roi.getPolygon());
				break;
			}
			i ++;
		}
	}

	@Override
	public void setModifyRoiCallback(BiConsumer<Integer,Polygon> modifyRoiCallback) {
		this.modifyRoiCallback = modifyRoiCallback;
		
	}
}
