package org.proteovir.roimanager;

import java.util.List;

import ai.nets.samj.annotation.Mask;

public interface ConsumerInterface {

	public void setRois(List<Mask> rois);
	
	public void deleteAllRois();
}
