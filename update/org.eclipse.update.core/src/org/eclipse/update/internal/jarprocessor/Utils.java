/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.jarprocessor;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

/**
 * @author aniefer
 *
 */
public class Utils {
	public static final String PACK200_PROPERTY = "org.eclipse.update.jarprocessor.pack200"; //$NON-NLS-1$
	public static final String JRE = "@jre"; //$NON-NLS-1$
	public static final String PATH = "@path"; //$NON-NLS-1$
	public static final String NONE = "@none"; //$NON-NLS-1$

	public static final String PACKED_SUFFIX = ".pack.gz"; //$NON-NLS-1$
	public static final String JAR_SUFFIX = ".jar"; //$NON-NLS-1$

	public static final FileFilter JAR_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().endsWith(".jar"); //$NON-NLS-1$
		}
	};

	public static final FileFilter PACK_GZ_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().endsWith(PACKED_SUFFIX);
		}
	};

	public static void close(Object stream) {
		if (stream != null) {
			try {
				if (stream instanceof InputStream)
					((InputStream) stream).close();
				else if (stream instanceof OutputStream)
					((OutputStream) stream).close();
				else if (stream instanceof JarFile)
					((JarFile) stream).close();
			} catch (IOException e) {
				//ignore
			}
		}
	}

	/**
	 * get the set of commands to try to execute pack/unpack 
	 * @param cmd, the command, either "pack200" or "unpack200"
	 * @return String [] or null
	 */
	public static String[] getPack200Commands(String cmd) {
		String[] locations = null;
		String prop = System.getProperty(PACK200_PROPERTY);
		String javaHome = System.getProperty("java.home"); //$NON-NLS-1$
		if (NONE.equals(prop)) {
			return null;
		} else if (JRE.equals(prop)) {
			locations = new String[] {javaHome + "/bin/" + cmd}; //$NON-NLS-1$
		} else if (PATH.equals(prop)) {
			locations = new String[] {cmd};
		} else if (prop == null) {
			locations = new String[] {javaHome + "/bin/" + cmd, cmd}; //$NON-NLS-1$ 
		} else {
			locations = new String[] {prop + "/" + cmd}; //$NON-NLS-1$
		}
		return locations;
	}

	/**
	 * Transfers all available bytes from the given input stream to the given
	 * output stream. Closes both streams if close == true, regardless of failure. 
	 * Flushes the destination stream if close == false
	 * 
	 * @param source
	 * @param destination
	 * @param close 
	 * @throws IOException
	 */
	public static void transferStreams(InputStream source, OutputStream destination, boolean close) throws IOException {
		source = new BufferedInputStream(source);
		destination = new BufferedOutputStream(destination);
		try {
			byte[] buffer = new byte[8192];
			while (true) {
				int bytesRead = -1;
				if ((bytesRead = source.read(buffer)) == -1)
					break;
				destination.write(buffer, 0, bytesRead);
			}
		} finally {
			if (close) {
				close(source);
				close(destination);
			} else {
				destination.flush();
			}
		}
	}

	/**
	 * Deletes all the files and directories from the given root down (inclusive).
	 * Returns false if we could not delete some file or an exception occurred
	 * at any point in the deletion.
	 * Even if an exception occurs, a best effort is made to continue deleting.
	 */
	public static boolean clear(java.io.File root) {
		boolean result = clearChildren(root);
		try {
			if (root.exists())
				result &= root.delete();
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * Deletes all the files and directories from the given root down, except for 
	 * the root itself.
	 * Returns false if we could not delete some file or an exception occurred
	 * at any point in the deletion.
	 * Even if an exception occurs, a best effort is made to continue deleting.
	 */
	public static boolean clearChildren(java.io.File root) {
		boolean result = true;
		if (root.isDirectory()) {
			String[] list = root.list();
			// for some unknown reason, list() can return null.  
			// Just skip the children If it does.
			if (list != null)
				for (int i = 0; i < list.length; i++)
					result &= clear(new java.io.File(root, list[i]));
		}
		return result;
	}

	public static Set getPackExclusions(Properties properties) {
		if (properties == null)
			return Collections.EMPTY_SET;

		String packExcludes = properties.getProperty("pack.excludes"); //$NON-NLS-1$
		if (packExcludes != null) {
			String[] excludes = packExcludes.split(",\\s*"); //$NON-NLS-1$
			Set packExclusions = new HashSet();
			for (int i = 0; i < excludes.length; i++) {
				packExclusions.add(excludes[i]);
			}
			return packExclusions;
		}
		return Collections.EMPTY_SET;
	}

	public static Set getSignExclusions(Properties properties) {
		if (properties == null)
			return Collections.EMPTY_SET;
		String signExcludes = properties.getProperty("sign.excludes"); //$NON-NLS-1$
		if (signExcludes != null) {
			String[] excludes = signExcludes.split(",\\s*"); //$NON-NLS-1$
			Set signExclusions = new HashSet();
			for (int i = 0; i < excludes.length; i++) {
				signExclusions.add(excludes[i]);
			}
			return signExclusions;
		}
		return Collections.EMPTY_SET;
	}
	
	public static String concat(String [] array){
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			if( i > 0 )
				buffer.append(' ');
			buffer.append(array[i]);
		}
		return buffer.toString();
	}
}
