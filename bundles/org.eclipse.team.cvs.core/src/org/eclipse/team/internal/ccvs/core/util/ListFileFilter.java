package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileFilter;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import sun.audio.ContinuousAudioDataStream;

/**
 * This class is a default FileFilter, that is used to 
 * ex- or include certain files on a ioFile.listFiles.
 * 
 * @see java.io.FileFilter
 */
public class ListFileFilter implements FileFilter {
	
	private final String[] fileNameList;
	private final boolean exclude;
	private final boolean fileOnly;
	private final boolean folderOnly;
	
	/**
	 * @param fileNameList the list of fileNames you want to include or exclude
	 * @param exclude if true all files not in fileNameList are shown
	                   if false only files in fileNameList are shown
	 */
	public ListFileFilter(String[] fileNameList, boolean exclude) {
		this(fileNameList,exclude,false, false);
	}

	/**
	 * @param fileNameList the list of fileNames you want to include or exclude
	 * @param exclude if true all files not in fileNameList are shown
	                   if false only files in fileNameList are shown
	 * @param fileOnly if true only files are shown
	 * @param folderOnly if true only folders are shown
	 */
	public ListFileFilter(String[] fileNameList, boolean exclude, boolean fileOnly, boolean folderOnly) {
	
		Assert.isTrue(!fileOnly || !folderOnly);
		
		if (fileNameList == null) {
			fileNameList = new String[0];
		}
		
		this.fileNameList = fileNameList;
		this.exclude = exclude;
		this.fileOnly = fileOnly;
		this.folderOnly = folderOnly;
	}
	
	/**
	 * @see FileFilter#accept(File)
	 */
	public boolean accept(File file) {
		
		boolean result = exclude;
		String fileName = file.getName();
		
		// If the resource is of the wrong type reject it
		if (fileOnly && file.isDirectory()) {
			return false;
		}			
		if (folderOnly && file.isFile()) {
			return false;
		}
		
		for (int i=0; i<fileNameList.length; i++) {
			// use starts-with because we do not want to be 
			// messed up by an following seperator							
			if (fileName.startsWith(fileNameList[i])) {
				result = !result;
				break;
			}
		}

		return result;
	}
}

