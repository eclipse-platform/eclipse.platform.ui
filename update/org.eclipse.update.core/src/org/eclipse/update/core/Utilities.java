package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 *
 */
public class Utilities {
	/**
	 * 
	 */
	private static Map entryMap;

	private static Stack bufferPool;	

	private static final int BUFFER_SIZE = 1024;

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
			temp = File.createTempFile("eclipse",null,tmpDir); //$NON-NLS-1$
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
		String tmpName = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		// in Linux, returns '/tmp', we must add '/'
		if (!tmpName.endsWith(File.separator)) tmpName += File.separator;
		tmpName += "eclipse" + File.separator + ".update" + File.separator + Long.toString((new Date()).getTime()) + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
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

	public static CoreException newCoreException(String s, Throwable e) {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();		
		return new CoreException(new Status(IStatus.ERROR,id,0,s,e)); //$NON-NLS-1$
	}
	
	public static CoreException newCoreException(String s,String first, String second, Throwable e, Throwable e1) {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();		
		MultiStatus multi = new MultiStatus(id,IStatus.ERROR,s,null);
		multi.add(new Status(IStatus.ERROR,id,0,first,e));
		multi.add(new Status(IStatus.ERROR,id,0,second,e1));		
		return new CoreException(multi); //$NON-NLS-1$
	}
		
	public static void logException(String s, Throwable e) {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();		
		IStatus status = new Status(IStatus.ERROR,id,0,s,e);
		UpdateManagerPlugin.getPlugin().getLog().log(status);
	}	

}
