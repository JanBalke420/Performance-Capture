import java.lang.reflect.Field;

import org.opencv.core.Core;

public class OpenCVTracking {

	public static void main(String[] args) { // laden der OpenCV bibliothek und
												// erstellen einer neuen GUI
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		initOpenCv();
		new GUI();
	}
	
	public static void initOpenCv() {

	    setLibraryPath();

	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	    System.out.println("OpenCV loaded. Version: " + Core.VERSION);

	}

	private static void setLibraryPath() {

	    try {

	        System.setProperty("java.library.path", "lib/x64");

	        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
	        fieldSysPath.setAccessible(true);
	        fieldSysPath.set(null, null);

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException(ex);
	    }

	}

}
