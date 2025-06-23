package org.proteovir.roimanager;

import java.util.List;

import ai.nets.samj.annotation.Mask;

public interface RoiManagerConsumer {

	public void setRois(List<Mask> rois);

	public void setRois(List<Mask> rois, int index);

	public void setSelected(Mask roi);
	
	public void deleteAllRois();
}
