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
import ij.gui.Roi;
import ij.plugin.OverlayLabels;

public class RoiManagerIJ implements RoiManagerConsumer {

	PolygonRoi roi = null;
	public void setRois(List<Mask> rois) {
		ImagePlus imp = WindowManager.getCurrentImage();
		Overlay overlay = newOverlay();
		for (Mask mm : rois) {
			roi = new PolygonRoi(mm.getContour(), PolygonRoi.POLYGON);
			overlay.add(roi);
		}
		roi.setImage(null);
		imp.setRoi(roi);
		imp.getWindow().getCanvas().getImage().setRoi(roi);
		setOverlay(imp, overlay);
		imp.getWindow().getCanvas().getImage().setRoi(roi);
		imp.getWindow().getCanvas().repaint();;
		//imp.getCanvas().repaint();
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
