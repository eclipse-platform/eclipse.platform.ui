
package org.eclipse.update.core;

import java.io.*;
import java.util.*;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 
 
 
/**
 * Class to copy files into a temporary directory
 */
public class CopyHelper {
	private static Map entryMap;

	private static Stack bufferPool;	

	private static final int BUFFER_SIZE = 1024;

	private static File tmpDir;

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
	protected static synchronized byte[] getBuffer() {
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
	protected static synchronized void freeBuffer(byte[] buf) {
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
	public static synchronized File createLocalFile(String key, String name) throws IOException {
		
		// ensure we have a temp directory
		if (tmpDir == null) {		
			String tmpName = System.getProperty("java.io.tmpdir");
			// in Linux, return '/tmp', we must add '/'
			if (!tmpName.endsWith(File.separator)) tmpName += File.separator;
			tmpName += "eclipse" + File.separator + ".update" + File.separator + Long.toString((new Date()).getTime()) + File.separator;
			tmpDir = new File(tmpName);
			verifyPath(tmpDir, false);
			if (!tmpDir.exists())
				throw new FileNotFoundException(tmpName);
		}
		
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

 // per-feature temp root


}
