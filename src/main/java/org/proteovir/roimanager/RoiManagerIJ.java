package org.proteovir.roimanager;

import java.awt.Color;
import java.util.List;

import ai.nets.samj.annotation.Mask;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.plugin.OverlayLabels;

public class RoiManagerIJ implements RoiManagerConsumer {

	public void setRois(List<Mask> rois) {
		ImagePlus imp = WindowManager.getCurrentImage();
		Overlay overlay = newOverlay();
		for (Mask mm : rois) {
			PolygonRoi roi = new PolygonRoi(mm.getContour(), PolygonRoi.POLYGON);
			overlay.add(roi);
		}
		imp.deleteRoi();
		setOverlay(imp, overlay);
	}

	public void setRois(List<Mask> rois, int ind) {
		ImagePlus imp = WindowManager.getCurrentImage();
		Overlay overlay = newOverlay();
		int i = 0;
		for (Mask mm : rois) {
			PolygonRoi roi = new PolygonRoi(mm.getContour(), PolygonRoi.POLYGON);
			overlay.add(roi);
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
}
