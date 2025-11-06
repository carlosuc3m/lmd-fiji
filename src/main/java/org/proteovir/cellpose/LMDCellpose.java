package org.proteovir.cellpose;

import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.proteovir.utils.Constants;
import org.proteovir.utils.Mask;

import ai.nets.samj.models.PythonMethods;
import io.bioimage.modelrunner.apposed.appose.Types;
import io.bioimage.modelrunner.apposed.appose.Service.Task;
import io.bioimage.modelrunner.apposed.appose.Service.TaskStatus;
import io.bioimage.modelrunner.bioimageio.description.ModelDescriptor;
import io.bioimage.modelrunner.exceptions.RunModelException;
import io.bioimage.modelrunner.model.special.cellpose.Cellpose;
import io.bioimage.modelrunner.tensor.shm.SharedMemoryArray;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class LMDCellpose extends Cellpose {
	/**
	 * All the Python imports and configurations needed to start using EfficientViTSAM.
	 */
	public static final String IMPORTS = ""
			+ "task.update('start')" + System.lineSeparator()
			+ "import numpy as np" + System.lineSeparator()
			+ "import torch" + System.lineSeparator()
			+ "from skimage import measure" + System.lineSeparator()
			+ "measure.label(np.ones((10, 10)), connectivity=1)" + System.lineSeparator()
			+ "from scipy.ndimage import binary_fill_holes" + System.lineSeparator()
			+ "from scipy.ndimage import label" + System.lineSeparator()
			+ "globals()['measure'] = measure" + System.lineSeparator()
			+ "globals()['label'] = label" + System.lineSeparator()
			+ "globals()['torch'] = torch" + System.lineSeparator()
			+ "globals()['np'] = np" + System.lineSeparator()
			+ "globals()['binary_fill_holes'] = binary_fill_holes" + System.lineSeparator();

	protected LMDCellpose(String modelFile, String callable, String weightsPath, Map<String, Object> kwargs,
			ModelDescriptor descriptor) throws IOException, InterruptedException {
		super(modelFile, callable, weightsPath, kwargs, descriptor);
		Task task = this.getPythonSerice().task(IMPORTS + PythonMethods.RLE_METHOD + PythonMethods.TRACE_EDGES);
		task.waitFor();
		if (task.status == TaskStatus.CANCELED)
			throw new RuntimeException("Task canceled");
		else if (task.status == TaskStatus.FAILED)
			throw new RuntimeException(task.error);
		else if (task.status == TaskStatus.CRASHED)
			throw new RuntimeException(task.error);
	}
	
	
	public static LMDCellpose create() throws IOException, InterruptedException {
		String modelPath = Cellpose.findPretrainedModelInstalled("cyto3", Constants.FIJI_FOLDER + File.separator + "models");
		return new LMDCellpose(null, null, modelPath, null, null);
	}
	/**
	 * Simply run inference on the images provided. If the dimensions, number, data type or other
	 * characteristic of the tensor is not correct, an exception will be thrown.
	 * @param <T>
	 * 	input data type
	 * @param <R>
	 * 	ouptut data type
	 * @param inputs
	 * 	the list of {@link RandomAccessibleInterval} that will be used as inputs
	 * @return a list of {@link RandomAccessibleInterval} that has been outputed by the model
	 * @throws RunModelException
	 *             if there is an error in the execution of the model
	 */
	public <T extends RealType<T> & NativeType<T>>
	List<Mask> inferenceContours(List<RandomAccessibleInterval<T>> inputs, int slice, int frame) throws RunModelException {

		if (!loaded)
			throw new RuntimeException("Please load the model first.");
		List<String> names = IntStream.range(0, inputs.size())
				.mapToObj(i -> "var_" + UUID.randomUUID().toString().replace("-", "_")).collect(Collectors.toList());
		String code = createInputsCode(inputs, names);
		return runCode(code, slice, frame);
	}
	
	private List<Mask> runCode(String code, int slice, int frame) 
	throws RunModelException {
		List<Mask> masks;
		try {
			Task task = this.getPythonSerice().task(code);
			task.waitFor();
			if (task.status == TaskStatus.CANCELED)
				throw new RuntimeException("Task canceled");
			else if (task.status == TaskStatus.FAILED)
				throw new RuntimeException(task.error);
			else if (task.status == TaskStatus.CRASHED)
				throw new RuntimeException(task.error);
			loaded = true;

			final List<List<Number>> contours_x_container = (List<List<Number>>)task.outputs.get("contours_x");
			final Iterator<List<Number>> contours_x = contours_x_container.iterator();
			final Iterator<List<Number>> contours_y = ((List<List<Number>>)task.outputs.get("contours_y")).iterator();
			final Iterator<List<Number>> rles = ((List<List<Number>>)task.outputs.get("rle")).iterator();
			masks = new ArrayList<Mask>(contours_x_container.size());
			while (contours_x.hasNext()) {
				int[] xArr = contours_x.next().stream().mapToInt(Number::intValue).toArray();
				int[] yArr = contours_y.next().stream().mapToInt(Number::intValue).toArray();
				long[] rle = rles.next().stream().mapToLong(Number::longValue).toArray();
				masks.add(Mask.build(new Polygon(xArr, yArr, xArr.length), rle, slice, frame));
			}
			cleanShm();
		} catch (IOException | InterruptedException e) {
			try {
				cleanShm();
			} catch (InterruptedException | IOException e1) {
				throw new RunModelException(Types.stackTrace(e1));
			}
			throw new RunModelException(Types.stackTrace(e));
		}
		return masks;
	}
	
	protected <T extends RealType<T> & NativeType<T>> String createInputsCode(List<RandomAccessibleInterval<T>> inRais, List<String> names) {
		String code = "created_shms = []" + System.lineSeparator();
		code += setDiameterCode + System.lineSeparator();
		setDiameterCode = "";
		code += "try:" + System.lineSeparator();
		for (int i = 0; i < inRais.size(); i ++) {
			SharedMemoryArray shma = SharedMemoryArray.createSHMAFromRAI(inRais.get(i), false, false);
			code += codeToConvertShmaToPython(shma, names.get(i));
			inShmaList.add(shma);
		}
		String nameList = "[";
		String channelList = "[";
		for (int i = 0; i < inRais.size(); i ++) {
			nameList += names.get(i) + ", ";
			channelList += createChannelsArgCode(inRais.get(i)) + ", ";
		}
		nameList += "]";
		channelList += "]";
		code += createDiamCode(nameList, channelList);
		code += "  with torch.no_grad():" + System.lineSeparator();
		code += "    " + OUTPUT_LIST_KEY + " = " + MODEL_VAR_NAME + ".eval(" + nameList + ", channels=" + channelList + ", ";
		code += "diameter=diameter)" + System.lineSeparator();
		code += ""
				+ "  print(diameter)" + System.lineSeparator()
				+ "  contours_x, contours_y, rle_masks = get_polygons_from_binary_mask(" + OUTPUT_LIST_KEY + "[0][0], only_biggest=False)" + System.lineSeparator()
				+ "  task.update('all contours traced')" + System.lineSeparator()
				+ "  task.outputs['contours_x'] = contours_x" + System.lineSeparator()
				+ "  task.outputs['contours_y'] = contours_y" + System.lineSeparator()
				+ "  task.outputs['rle'] = rle_masks" + System.lineSeparator();
		String closeEverythingWin = closeSHMWin();
		code += "  " + closeEverythingWin + System.lineSeparator();
		code += "except Exception as e:" + System.lineSeparator();
		code += "  " + closeEverythingWin + System.lineSeparator();
		code += "  raise e" + System.lineSeparator();
		return code;
	}

}
