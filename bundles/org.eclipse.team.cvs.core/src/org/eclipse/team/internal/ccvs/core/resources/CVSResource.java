package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSResource;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * CVSResource, CVSFile, CVSFolder implement the 
 * ICVSResource, ICVSFile, ICVSFolder interfaces that are needed
 * to use the cvs-client.
 * 
 * Just call CVSFolder.createFolderFromPath(String path) to create an 
 * ICVSFolder and pass it to the client.
 * 
 * @see CVSFolder#createFolderFromPath(String)
 * @see CVSFolder
 * @see CVSFile
 * @see CVSResource
 * @see ICVSFolder
 * @see ICVSFile
 * @see ICVSResource
 */
abstract class CVSResource implements ICVSResource, Comparable {

	public static final String PLATFORM_NEWLINE = System.getProperty("line.separator");

	File ioResource;
	
	CVSResource(String path) {
		this(new File(path));
	}


	CVSResource(File ioResource) {
		this.ioResource = ioResource;
	}	


	/**
	 * @see ICVSResource#getName()
	 */
	public String getName() {
		
//		String path;
//		int lastFileSeperatorPos;
//		String name;
//		
//		path = ioResource.getAbsolutePath();
//		lastFileSeperatorPos = path.lastIndexOf(File.separator);
//		// check that
//		name = path.substring(lastFileSeperatorPos + 1);
		
		return ioResource.getName();
	}
	
	/**
	 * @see ICVSResource#getPath()
	 */
	public String getPath() {
		return ioResource.getAbsolutePath();
	}


	/**
	 * @see ICVSResource#delete()
	 */
	public void delete() {
		ioResource.delete();
	}


	/**
	 * @see ICVSResource#getParent()
	 */
	public ICVSFolder getParent() {
		
		try {
			return CVSFolder.createInternalFolderFrom(ioResource.getParentFile());
		} catch (CVSException e) {
			// This should not happen, because the canonical Path of
			// a parent should be O.K.
			Util.logError(Policy.bind("CVSFolder.invalidPath"),e);
			Assert.isTrue(false);
			return null;
		}
	}

	/**
	 * Equals is equals on the abstract pathnames
	 */
	public boolean equals(Object obj) {
		
		if (!(obj instanceof CVSResource)) {
			return false;
		} else {
			return ((CVSResource) obj).getPath().equals(getPath());
		}
	}
	
	/**
	 * Generate a Standard CVSException for an
	 * IOException
	 */
	protected static CVSException wrapException(IOException e) {
		return new CVSException(IStatus.ERROR,
								CVSException.IO_FAILED,
								"An IOException occured while using your file-system.",
								e);
	}
	
	/** Clean up fileName. "/" and "\" are both replaced for
	 * File.seperator
	 */
	protected static String convertSeparator(String path) {
		if (File.separatorChar == '/') {
			return path.replace('\\','/');
		} else {
			return path.replace('/','\\');
		}			
	}

	/**
	 * Give the pathname back
	 */
	public String toString() {
		return getPath();
	}

	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}


	/**
	 * @see ICVSResource#exists()
	 */
	public boolean exists() {
		return ioResource.exists();
	}
	
	/**
	 * throw an exeption, if the underlying resource
	 * does not exist.
	 */
	void exceptionIfNotExist() throws CVSFileNotFoundException {
		
		if (!exists()) {
			throw new CVSFileNotFoundException(ioResource + " not found");
		}
	}
	
	/**
	 * Comparing for the work with the sets,
	 * 
	 * The coparison is done by the paths.
	 */
	public int compareTo(Object obj) {
		
		if (!(obj instanceof CVSResource)) {
			return -1;
		} else {
			return getPath().compareTo(((CVSResource)obj).getPath());
		}
	}

	/**
	 * Write String[] to file as lines
	 * 
	 * @param file has to be non-null
	 * @param content has to be non-null
	 */
	protected static void writeToFile(File file, String[] content)
		throws CVSException {
		writeToFile(file, content, PLATFORM_NEWLINE);
		/*
		BufferedWriter fileWriter;
		
		try {
			fileWriter = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i<content.length; i++) {
				fileWriter.write(content[i]);
				fileWriter.newLine();
			}
			fileWriter.close();
		} catch (IOException e) {
			throw rapIOtoCVS(e);
		}
		*/
	}
	
	/**
	 * Write String[] to file as lines
	 * 
	 * @param file has to be non-null
	 * @param content has to be non-null
	 */
	protected static void writeToFile(File file, String[] content, String delim)
		throws CVSException {
		
		BufferedWriter fileWriter;

		deleteIfProtected(file);
		
		try {
			fileWriter = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i<content.length; i++) {
				fileWriter.write(content[i]);
				fileWriter.write(delim);
			}
			fileWriter.close();
		} catch (IOException e) {
			throw wrapException(e);
		}
	}
	
	/**
	 * Delete the file if it is WriteProtected in order to be able to 
	 * write new content in this place
	 */
	protected static void deleteIfProtected(File ioFile) throws CVSException {
		
		boolean sucess;
		
		// If the file is read-only we need to delete it before
		// we can write it new
		if (!ioFile.canWrite()) {
			sucess = ioFile.delete();
			if (!sucess && ioFile.exists()) {
				throw new CVSException("Not able to delete file");
			}
		}			
	}
	
	/**
	 * load file in lines to String[]
	 * 
	 * @param file has to be non-null and file.exists() == true
	 */
	protected static String[] readFromFile(File file)
		throws CVSException {

		BufferedReader fileReader;
		List fileContentStore = new ArrayList();
		String line;
		
		try {
			fileReader = new BufferedReader(new FileReader(file));
			while ((line = fileReader.readLine()) != null) {
				fileContentStore.add(line);
			}
			fileReader.close();
		} catch (IOException e) {
			throw wrapException(e);
		}
			
		return (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
	}	
	
	/**
	 * This is to be used by the ResourceFactory only
	 */
	public File getIOResource() {
		return ioResource;
	}

	/**
	 * Implement the hashcode on the underlying strings, like it
	 * is done in the equals.
	 */
	public int hashCode() {
		return getPath().hashCode();
	}	
}


