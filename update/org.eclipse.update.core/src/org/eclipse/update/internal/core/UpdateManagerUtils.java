package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.InstallMonitor;

public class UpdateManagerUtils {
	
	/**
	 * 
	 */
	private static IOException CANCEL_EXCEPTION;

	/**
	 * 
	 */
	private static Map entryMap;
	private static Stack bufferPool;	
	private static final int BUFFER_SIZE = 1024;
	
	/**
	 * 
	 */
	public static final String EMPTY_STRING = "";
	public static final String FILE_URL_PROTOCOL = "file";


	/**
	 * Static block to initialize the possible CANCEL ERROR
	 * thrown when the USER cancels teh operation
	 */
	static {
		//	in case we throw a cancel exception
		CANCEL_EXCEPTION = new IOException("Install has been cancelled");
	}

	/**
	 * return the urlString if it is a absolute URL
	 * otherwise, return the default URL if the urlString is null
	 * The defaultURL may point ot a file, create a file URL then
	 * if teh urlString or the default URL are relatives, prepend the rootURL to it
	 */
	public static URL getURL(URL rootURL, String urlString, String defaultURL) throws MalformedURLException {
		URL url = null;

		// if no URL , provide Default
		if (urlString == null || urlString.trim().equals("")) {

			// no URL, no default, return right now...
			if (defaultURL == null || defaultURL.trim().equals(""))
				return null;
			else
				urlString = defaultURL;
		}

		// URL can be relative or absolute	
		if (urlString.startsWith("/") && urlString.length() > 1)
			urlString = urlString.substring(1);
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// the url is not an absolute URL
			// try relative
			url = new URL(rootURL,urlString);
		}
		return url;
	}

	/**
	 * return the url a relative String if the url contains the base url
	 * 
	 */
	public static String getURLAsString(URL rootURL, URL url) {
		String result = null;
		String rootString = rootURL.toExternalForm();

		// if no URL , return null
		if (url != null) {
			String urlString = url.toExternalForm();
			if (urlString.indexOf(rootString)!=-1){
				result = urlString.substring(rootString.length());
			} else {
				result = urlString;
			}
		}
		
		return result;			
	}


	/**
	 * returns a translated String
	 */
	public static String getResourceString(String infoURL, ResourceBundle bundle) {
		String result = null;
		if (infoURL != null) {
			result = UpdateManagerPlugin.getPlugin().getDescriptor().getResourceString(infoURL, bundle);
		}
		return result;
	};

	
	
	/**
	 * 
	 */
	public static URL copyToLocal(InputStream sourceContentReferenceStream, String localName, InstallMonitor monitor) throws MalformedURLException, IOException {
		URL result = null;

		// create the Dir is they do not exist
		// get the path from the File to resolve File.separator..
		// do not use the String as it may contain URL like separator
		File localFile = new File(localName);
		int index = localFile.getPath().lastIndexOf(File.separator);
		if (index != -1) {
			File dir = new File(localFile.getPath().substring(0, index));
			if (!dir.exists())
				dir.mkdirs();
		}

		// transfer teh content of the File
		if (!localFile.isDirectory()) {
			OutputStream localContentReferenceStream = new FileOutputStream(localFile);
			copy(sourceContentReferenceStream, localContentReferenceStream,monitor);
			localContentReferenceStream.close();
		}
		result = localFile.toURL();

		return result;
	}

	/**
	 * Returns a random file name for the local system
	 * attempt to conserve the extension if there is a '.' in the path
	 * and no File.Seperator after the '.'
	 * 
	 * \a\b\c.txt -> c987659385.txt
	 * c.txt -> c3854763.txt
	 * c	-> c953867549
	 */
	public static String getLocalRandomIdentifier(String remotePath, Date date) {
		int dotIndex = remotePath.lastIndexOf(".");
		int fileIndex = remotePath.lastIndexOf(File.separator);
		
		// if there is a separator after the dot
		// do not consider it as an extension
		// FIXME: LINUX ???
		//String ext = (dotIndex != -1 && fileIndex < dotIndex) ? "." + remotePath.substring(dotIndex) : "";
		String ext = (dotIndex != -1 && fileIndex < dotIndex) ? remotePath.substring(dotIndex) : "";
		
		// the name is teh string between the separator and the dot
		// if there is no separator, it is the string up to the dot		
		// if there is no dot, go to the end of the string 
		if (fileIndex==-1) fileIndex=0;
		if (dotIndex==-1) dotIndex=remotePath.length();
		// if I have a separator and no dot: /a/b/c -> c
		// if my separator is the last /a/b/c/, fileIndex and dotIndex are the same, so it will return teh default temp name
		String name = (fileIndex < dotIndex) ? remotePath.substring(fileIndex, dotIndex) : "Eclipse_Update_TMP_";

		String result = name + date.getTime() + ext;
		
		return result;
	}

	
	/**
	 * remove a file or directory from the file system.
	 * used to clean up install
	 */
	public static void removeFromFileSystem(File file) {
		if (!file.exists())
			return;
			
		if (file.isDirectory()) {
			String[] files = file.list();
			if (files != null) // be careful since file.list() can return null
				for (int i = 0; i < files.length; ++i)
					removeFromFileSystem(new File(file, files[i]));
		}
		if (!file.delete()) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING,id,IStatus.OK,"cannot remove: " + file.getPath()+" from the filesystem",new Exception());
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * Returns the plugin entries that are in source array and
	 * missing from target array
	 */
	public static IPluginEntry[] substract(IPluginEntry[] sourceArray, IPluginEntry[] targetArray) {

		// No pluginEntry to Install, return Nothing to instal
		if (sourceArray == null || sourceArray.length == 0) {
			return new IPluginEntry[0];
		}

		// No pluginEntry installed, Install them all
		if (targetArray == null || targetArray.length == 0) {
			return sourceArray;
		}

		// if a IPluginEntry from sourceArray is NOT in
		// targetArray, add it to the list
		List list1 = Arrays.asList(targetArray);
		List result = new ArrayList(0);
		for (int i = 0; i < sourceArray.length; i++) {
			if (!list1.contains(sourceArray[i]))
				result.add(sourceArray[i]);
		}

		IPluginEntry[] resultEntry = new IPluginEntry[result.size()];
		if (result.size() > 0)
			result.toArray(resultEntry);

		return resultEntry;
	}

	
		
	/**
	 * Copies specified input stream to the output stream.
	 * 
	 * @since 2.0
	 */	
	public static void copy(InputStream is, OutputStream os, InstallMonitor monitor) throws IOException {
		byte[] buf = getBuffer();
		try {
			long currentLen = 0;
			int len = is.read(buf);
			while(len != -1) {
				currentLen += len;
				os.write(buf,0,len);
				if (monitor != null)
					monitor.setCopyCount(currentLen);
				len = is.read(buf);
			}
		} finally {
			freeBuffer(buf);
		}
	}


	/**
	 * @since 2.0
	 */
	private static synchronized byte[] getBuffer() {
		if (bufferPool == null) {
			return new byte[BUFFER_SIZE];
		}
		
		try {
			return (byte[]) bufferPool.pop();
		} catch (EmptyStackException e) {
			return new byte[BUFFER_SIZE];
		}
	}


	/**
	 * @since 2.0
	 */
	private static synchronized void freeBuffer(byte[] buf) {
		if (bufferPool == null)
			bufferPool = new Stack();
		bufferPool.push(buf);
	}


	/**
	 * Create a local file with the specified name in temporary area
	 * and associate it with the specified key. If name is not specified
	 * a temporary name is created. If key is not specified no 
	 * association is made.
	 * 
	 * @since 2.0
	 */
	// VK: needs to be API (and lookupLocalFile(), removeLocalFile() ??)
	public static synchronized File createLocalFile(File tmpDir, String key, String name) throws IOException {
			
		// create the local file
		File temp;
		String filePath;
		if (name != null) {
			// create file with specified name
			filePath = name.replace('/',File.separatorChar);
			if (filePath.startsWith(File.separator))
				filePath = filePath.substring(1);
			temp = new File(tmpDir, filePath);
		} else {
			// create file with temp name
			temp = File.createTempFile("eclipse",null,tmpDir);
		}
		temp.deleteOnExit();
		verifyPath(temp, true);
		
		// create file association 
		if (key != null) {
			if (entryMap == null)
				entryMap = new HashMap();
			entryMap.put(key,temp);
		}
		
		return temp;
	}
	
	/**
	 * Returns local working directory (in temporary area).
	 * 
	 * @since 2.0
	 */	
	public static synchronized File createWorkingDirectory() throws IOException {
		String tmpName = System.getProperty("java.io.tmpdir");
		// in Linux, returns '/tmp', we must add '/'
		if (!tmpName.endsWith(File.separator)) tmpName += File.separator;
		tmpName += "eclipse" + File.separator + ".update" + File.separator + Long.toString((new Date()).getTime()) + File.separator;
		File tmpDir = new File(tmpName);
		verifyPath(tmpDir, false);
		if (!tmpDir.exists())
			throw new FileNotFoundException(tmpName);
		return tmpDir;
	}
	
	/**
	 * Returns local file (in temporary area) matching the
	 * specified key. Returns null if the entry does not exist.
	 * 
	 * @since 2.0
	 */	
	public static synchronized File lookupLocalFile(String key) {
		if (entryMap == null)
			return null;
		return (File) entryMap.get(key);
	}

	/**
	 * Removes the specified key from the local file map. The file is
	 * not actually deleted until VM termination.
	 * 
	 * @since 2.0
	 */	
	public static synchronized void removeLocalFile(String key) {
		if (entryMap != null)
			entryMap.remove(key);
	}


	private static void verifyPath(File path, boolean isFile) {
		// if we are expecting a file back off 1 path element
		if (isFile) {
			if (path.getAbsolutePath().endsWith(File.separator)) { // make sure this is a file
				path = path.getParentFile();
				isFile = false;
			}
		}
		
		// already exists ... just return
		if (path.exists())
			return;

		// does not exist ... ensure parent exists
		File parent = path.getParentFile();
		verifyPath(parent,false);
		
		// ensure directories are made. Mark files or directories for deletion
		if (!isFile)
			path.mkdir();
		path.deleteOnExit();			
	}

	/**
	 * Returns the plugin entries that are in source array and
	 * missing from target array
	 */
	public static IPluginEntry[] intersection(IPluginEntry[] sourceArray, IPluginEntry[] targetArray) {

		// No pluginEntry to Install, return Nothing to instal
		if (sourceArray == null || sourceArray.length == 0) {
			return new IPluginEntry[0];
		}

		// No pluginEntry installed, Install them all
		if (targetArray == null || targetArray.length == 0) {
			return sourceArray;
		}

		// if a IPluginEntry from sourceArray is NOT in
		// targetArray, add it to the list
		List list1 = Arrays.asList(targetArray);
		List result = new ArrayList(0);
		for (int i = 0; i < sourceArray.length; i++) {
			if (!list1.contains(sourceArray[i]))
				result.add(sourceArray[i]);
		}

		IPluginEntry[] resultEntry = new IPluginEntry[result.size()];
		if (result.size() > 0)
			result.toArray(resultEntry);

		return resultEntry;
	}



}