package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * A FilePropertiesContainer stores informations about the files
 * of a folder. It cares about loading saving this information 
 * in the folder the container belongs to.
 */
public class FilePropertiesContainer {
	
	private ICVSFolder cvsFolder;

	private static final String seperator = "/";
	private static final String FOLDER_ENTRY = "D/";
	private static final String ENTRIES = FileProperties.ENTRIES;
	
	/** 
	 * Construct a container for the file-infos for
	 * the files of this folder
	 */
	public FilePropertiesContainer(ICVSFolder cvsFolder, boolean autoSave) {
		this.cvsFolder = cvsFolder;
	}

	/**
	 * Costruct a FileProperties-Object for the file.
	 * The file has to be child of the folder.
	 * 
	 * Changing the FileProperties does not change anything 
	 * in the FilePropertiesContainer. You have to set the
	 * fileInfo to acctually change something.
	 */	
	public FileProperties getFileInfo(String fileName) throws CVSException {
		
		FileProperties fileProperties = new FileProperties();
		String key;
		String property;
		boolean foundFile = false;
		
		if (!cvsFolder.isCVSFolder()) {
			return null;
		}
		
		// Go through all the keys that we want to load from 
		// our cvsFolder 
		// take them from cvsFolder and put them into our 
		// new FileProperties
		for (Iterator i = fileProperties.keySet().iterator(); i.hasNext();) {
			key = (String) i.next();
			property = getProperty(key,fileName);
			
			if (property != null) {
				fileProperties.putProperty(key,property);
				foundFile = true;
			}
		}
		
		// If there was nothing to load at all, then we give null back
		// istead of the empty FileProperties
		// I do not know if we really need that
		if (foundFile) {
			return fileProperties;
		} else {
			return null;
		}
	}

	/**
	 * Set the fileinfo into the container.
	 */	
	public void setFileInfo(String fileName, FileProperties fileProperties) throws CVSException {
		
		Assert.isNotNull(fileName);
		Assert.isTrue(fileProperties == null || fileName.equals(fileProperties.getName()));
		
		String key;
		
		// If we want to "unset" the file, then we just create a
		// new FileProperties, what is going to have null in all
		// arguments, and null removes the value.
		if (fileProperties == null) {
			fileProperties = new FileProperties();
		}
		
		for (Iterator i = fileProperties.keySet().iterator(); i.hasNext();) {
			key = (String)i.next();			
			putProperty(key,fileName,fileProperties.getProperty(key));
		}
	}
	
	/**
	 * Set a property of a file to the value. Value null removes the 
	 * information. Saves this information in the cvsFolder.
	 */
	private void putProperty(String key,String fileName,String value) throws CVSException {
		
		String[] data;
		Map mapData = new HashMap();
		int start;
		int end;
		boolean noDirectories = false;
		
		data = cvsFolder.getProperty(key);
		
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				start = data[i].indexOf(seperator)+1;
				end = data[i].indexOf(seperator,start+1);
				// The entry may contain a D indicating that there are no directories. ignore the D.
				if (start != -1 && end != -1)
					mapData.put(data[i].substring(start,end),data[i]);
			}
		}
		
		if (value == null) {
			mapData.remove(fileName);
		} else {
			mapData.put(fileName,formatProperty(fileName,value));
		}
		
		cvsFolder.setProperty(key,
							  (String[])mapData.values().toArray(new String[mapData.size()]));
	}
	
	/**
	 * Get a property for a file.
	 */
	private String getProperty(String key, String fileName) throws CVSException {

		String[] data = cvsFolder.getProperty(key);
		String fileKey = seperator + fileName + seperator;
					
		if (data == null) {
			return null;
		}
		
		for (int j = 0; j < data.length; j++) {
			if (data[j].startsWith(fileKey) || data[j].substring(1).startsWith(fileKey)) {
				return data[j];
			}
		}
		
		return null;
	}
	
	/**
	 * Bring a value into a valid property-format. If the 
	 * value is not in the "{something}/filename/{something}"-format 
	 * "/filename/" is the new prefix of the String.
	 */
	private String formatProperty(String fileName,String value) {
		
		int start;
		int end;
		String fileKey = seperator + fileName + seperator;

		if (value == null) {
			return null;
		}

		start = value.indexOf(seperator)+1;
		end = value.indexOf(seperator,start+1);
		
		if (start != -1 && end != -1 &&
			value.substring(start,end).equals(fileName)) {
			return value;
		} else {
			return fileKey + value;
		}
	}

	
	/**
	 * Adds folders to the container
	 * 
	 * @throws CVSException if autoSave & !folder.exists()
	 */
	public void addFolder(String name) throws CVSException {	
		String entryLine;
		entryLine = FOLDER_ENTRY + name + "////";
		putProperty(ENTRIES,name,entryLine);
	}
	
	/**
	 * Removes folders from the conatainer
	 */
	public void removeFolder(String name) throws CVSException {
		putProperty(ENTRIES,name,null);
	}
	
	/**
	 * Is the folder in the container ?
	 */
	public boolean containsFolder(String fileName) throws CVSException {	
		return getProperty(ENTRIES,fileName) != null;
	}

	/**
	 * This gives a list of all files stated in the
	 * entries. These files do not have to exist on 
	 * the filesystem.
	 */
	public ICVSFile[] getEntriesFileList() throws CVSException {
		
		List fileList = new ArrayList();
		String fileName;
		String[] entries;
		
		// If we are not in an cvs-folder or the entries are empty we do not have
		// entry-files.
		// NOTE: We are setting the entries-variable in that momnet
		if (!cvsFolder.isCVSFolder() ||
			(entries = cvsFolder.getProperty(ENTRIES)) == null) {
			return new ICVSFile[0];
		}
		
		for (int i=0; i<entries.length; i++) {
			if (entries[i].startsWith(seperator)) {
				
				// get the name of the file with the help of the
				// of the FileProperties Object. We need to do that in 
				// another way sometime
				fileName = (new FileProperties(entries[i],null)).getName();
				fileList.add(cvsFolder.createFile(fileName));
			}
		}
		
		return (ICVSFile[]) fileList.toArray(new ICVSFile[fileList.size()]);
	}
	
	/**
	 * This gives a list of all folders stated in the
	 * entries. These files do not have to exist on 
	 * the filesystem.
	 */
	public ICVSFolder[] getEntriesFolderList() throws CVSException {
		
		List folderList = new ArrayList();
		String folderName;
		
		String[] entries;
		
		// If we are not in an cvs-folder or the entries are empty we do not have
		// entry-files.
		// NOTE: We are setting the entries in that momnet
		if (!cvsFolder.isCVSFolder() ||
			(entries = cvsFolder.getProperty(ENTRIES)) == null) {
			return new ICVSFolder[0];
		}
		
		for (int i=0; i<entries.length; i++) {
			if (entries[i].startsWith(FOLDER_ENTRY)) {
				folderName = entries[i].substring(FOLDER_ENTRY.length());
				folderName = folderName.substring(0,folderName.indexOf(seperator));
				folderList.add(cvsFolder.createFolder(folderName));
			}
		}
		
		return (ICVSFolder[]) folderList.toArray(new ICVSFolder[folderList.size()]);	
	}
}

