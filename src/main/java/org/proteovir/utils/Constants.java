package org.proteovir.utils;

import java.io.File;

import io.bioimage.modelrunner.system.PlatformDetection;

public class Constants {

    /**
     * The folder of Fiji
     */
	public static final String FIJI_FOLDER = getFijiFolder();
	
	private static String getFijiFolder() {
		File jvmFolder = new File(System.getProperty("java.home"));
		String imageJExecutable;
		if (PlatformDetection.isWindows())
			imageJExecutable = "fiji-windows-x64.exe";
		else if (PlatformDetection.isLinux())
			imageJExecutable = "fiji-linux-x64";
		else if (PlatformDetection.isMacOS() && PlatformDetection.getArch().equals(PlatformDetection.ARCH_ARM64))
			imageJExecutable = "Fiji.App/Contents/MacOS/fiji-macos-arm64";
		else if (PlatformDetection.isMacOS())
			imageJExecutable = "Fiji.App/Contents/MacOS/fiji-macos-x64";
		else
			throw new IllegalArgumentException("Unsupported Operating System");
		while (true && jvmFolder != null) {
			jvmFolder = jvmFolder.getParentFile();
			if (new File(jvmFolder + File.separator + imageJExecutable).isFile())
				return jvmFolder.getAbsolutePath();
		}
		return getImageJFolder();
	}
    
	private static String getImageJFolder() {
		File jvmFolder = new File(System.getProperty("java.home"));
		String imageJExecutable;
		if (PlatformDetection.isWindows())
			imageJExecutable = "ImageJ-win64.exe";
		else if (PlatformDetection.isLinux())
			imageJExecutable = "ImageJ-linux64";
		else if (PlatformDetection.isMacOS())
			imageJExecutable = "Contents/MacOS/ImageJ-macosx";
		else
			throw new IllegalArgumentException("Unsupported Operating System");
		while (true && jvmFolder != null) {
			jvmFolder = jvmFolder.getParentFile();
			if (new File(jvmFolder + File.separator + imageJExecutable).isFile())
				return jvmFolder.getAbsolutePath();
		}
		return new File("").getAbsolutePath();
		// TODO remove throw new RuntimeException("Unable to find the path to the ImageJ/Fiji being used.");
	}

}
