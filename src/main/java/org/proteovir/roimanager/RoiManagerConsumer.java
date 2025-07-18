package org.proteovir.roimanager;

import java.awt.Polygon;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ai.nets.samj.annotation.Mask;

public interface RoiManagerConsumer {

	public void setRois(List<Mask> rois);

	public void setRois(List<Mask> rois, int index);

	public void setSelected(Mask roi);
	
	public void deleteAllRois();
	
	public void setModifyRoiCallback(BiConsumer<Integer,Polygon> modifyRoiCallback);

	public void setImage(Object image);

	void setSelectedCallback(Consumer<Integer> selectedCallback);
}
