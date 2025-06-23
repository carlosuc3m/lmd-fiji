package org.proteovir.roimanager;

import java.awt.geom.Point2D;
import java.util.List;

import ai.nets.samj.annotation.Mask;

public interface RoiManagerConsumer {

	public void setRois(List<Mask> rois);

	public void setSelected(Mask roi);
	
	public void deleteAllRois();

	public void setSelected(List<Point2D> simple);
}
