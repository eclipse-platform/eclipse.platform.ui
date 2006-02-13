/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/*
 * Utility methods for working with files.
 */
public class FileUtil {

	/**
	 * Gets the contents of the file with the given relative path in the given bundle,
	 * as a String (file must be encoded as UTF-8).
	 */
	public static String getContents(Bundle bundle, String relativePath) throws IOException {
		URL url = bundle.getEntry(relativePath);
		if (url != null) {
			return readString(url.openStream());
		}
		return null;
	}

	/**
	 * Gets the contents of the file with the given absolute path as a String (file must
	 * be encoded as UTF-8).
	 */
	public static String getContents(String absolutePath) throws IOException {
		return readString(new FileInputStream(absolutePath));
	}
	
	/**
	 * Generates a filename with path to the result file that will be generated
	 * for the intro xml referred to by the string.
	 */
	public static String getResultFile(String in) {
		return getResultFile(in, false);
	}

	/**
	 * Same as above, but gives the option of appending os, ws, and arch. For example,
	 * myfile_serialized_macosx_carbon_ppc.txt.
	 */
	public static String getResultFile(String in, boolean env) {
		StringBuffer buf = new StringBuffer();
		buf.append(in.substring(0, in.lastIndexOf('.')) + "_serialized");
		if (env) {
			buf.append('_');
			buf.append(Platform.getOS());
			buf.append('_');
			buf.append(Platform.getWS());
			buf.append('_');
			buf.append(Platform.getOSArch());
		}
		buf.append(".txt");
		return buf.toString();
	}
	
	/**
	 * Gets the contents of the result file with the given original relative path in
	 * the given bundle, as a String (file must be encoded as UTF-8).
	 */
	public static String getResultFileContents(Bundle bundle, String absolutePath) throws IOException {
		/*
		 * Try [filename]_serialized_os_ws_arch.txt. If it's not there, try
		 * [filename]_serialized.txt.
		 * 
		 * We use different files for os/ws/arch combinations in order to test dynamic content,
		 * specifically filtering. Some of the files have filters by os, ws, and arch so the
		 * result is different on each combination.
		 */
		String contents = null;
		try {
			contents = getContents(bundle, getResultFile(absolutePath, true));
		}
		catch(Exception e) {
			// didn't find the _serialized_os_ws_arch.txt file, try just _serialized.txt
		}
		if (contents == null) {
			try {
				contents = getContents(bundle, getResultFile(absolutePath, false));
			}
			catch(IOException e) {
				throw e;
			}
		}
		return contents;
	}

	/**
	 * Gets the contents of the result file with the given original absolute path as
	 * a String (file must be encoded as UTF-8).
	 */
	public static String getResultFileContents(String absolutePath) throws IOException {
		/*
		 * Try [filename]_serialized_os_ws_arch.txt. If it's not there, try
		 * [filename]_serialized.txt.
		 * 
		 * We use different files for os/ws/arch combinations in order to test dynamic content,
		 * specifically filtering. Some of the files have filters by os, ws, and arch so the
		 * result is different on each combination.
		 */
		String contents = null;
		try {
			contents = getContents(getResultFile(absolutePath, true));
		}
		catch(Exception e) {
			// didn't find the _serialized_os_ws_arch.txt file, try just _serialized.txt
		}
		if (contents == null) {
			try {
				contents = getContents(getResultFile(absolutePath, false));
			}
			catch(IOException e) {
				throw e;
			}
		}
		return contents;
	}
	
	/**
	 * Reads the contents of the input stream as UTF-8 and constructs and returns
	 * as a String.
	 */
	private static String readString(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int num;
		while ((num = in.read(buffer)) > 0) {
			out.write(buffer, 0, num);
		}
		String result = new String(out.toByteArray(), "UTF-8");
		if (result != null) {
			result = result.replaceAll("\r", "");
		}
		return result;
	}
}
