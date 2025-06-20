package org.proteovir.roimanager;

import java.util.List;

import ai.nets.samj.annotation.Mask;

public interface RoiManagerConsumer {

	public void setRois(List<Mask> rois);
	
	public void deleteAllRois();
}
