package org.eclipse.ui.tests.datatransfer;

import java.io.File;

import org.eclipse.ui.tests.util.UITestCase;

public abstract class DataTransferTestCase extends UITestCase {

	public DataTransferTestCase(String testName) {
		super(testName);
	}
	/**
	 * Delete all files and folders in the given directory.
	 * This method does not delete the root folder of the
	 * given directory. 
	 * 
	 * @param file
	 */
	protected void deleteDirectory(File directory){
		if (directory.exists()){
			if (directory.isDirectory()){
				File[] filesAndDirs = directory.listFiles();
				if (filesAndDirs != null){
					for (int i = 0; i < filesAndDirs.length; i++){
						File f = filesAndDirs[i];
						if (f.isDirectory()){
							deleteDirectory(f);
							if (!f.delete())
								fail("Could not delete " + f.getAbsolutePath());
						}
						else{
							if (!f.delete())
								fail("Could not delete " + f.getAbsolutePath());
						}
					}
				}
			}
			else{
				if (!directory.delete())
					fail("Could not delete " + directory.getAbsolutePath());
			}
		}		
	}

}
