/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class FindSupport {

	private static String[] WS_JAR_VARIANTS = buildWSVariants();
	private static String[] OS_JAR_VARIANTS = buildOSVariants();
	private static String[] NL_JAR_VARIANTS = buildNLVariants(InternalPlatform.getDefault().getEnvironmentInfoService().getNL());
	private static String[] JAR_VARIANTS = buildVanillaVariants();

	private static String[] buildWSVariants() {
		ArrayList result = new ArrayList();
		result.add("ws/" + InternalPlatform.getDefault().getEnvironmentInfoService().getWS()); //$NON-NLS-1$
		result.add(""); //$NON-NLS-1$
		return (String[]) result.toArray(new String[result.size()]);
	}
	private static String[] buildVanillaVariants() {
		return new String[] { "" }; //$NON-NLS-1$
	}
	private static String[] buildOSVariants() {
		ArrayList result = new ArrayList();
		result.add("os/" + InternalPlatform.getDefault().getEnvironmentInfoService().getOS() + "/" + InternalPlatform.getDefault().getEnvironmentInfoService().getOSArch()); //$NON-NLS-1$ //$NON-NLS-2$
		result.add("os/" + InternalPlatform.getDefault().getEnvironmentInfoService().getOS()); //$NON-NLS-1$
		result.add(""); //$NON-NLS-1$
		return (String[]) result.toArray(new String[result.size()]);
	}
	private static String[] buildNLVariants(String nl) {
		ArrayList result = new ArrayList();
		IPath base = new Path("nl"); //$NON-NLS-1$

		IPath path = new Path(nl.replace('_', '/'));
		while (path.segmentCount() > 0) {
			result.add(base.append(path).toString());
			// for backwards compatibility only, don't replace the slashes
			if (path.segmentCount() > 1)
				result.add(base.append(path.toString().replace('/', '_')).toString());
			path = path.removeLastSegments(1);
		}

		return (String[]) result.toArray(new String[result.size()]);
	}
	
	/**
	 * Returns a URL for the given path.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * 
	 * @param path file path relative to plug-in installation location
	 * @param override map of override substitution arguments to be used for
	 * any $arg$ path elements. The map keys correspond to the substitution
	 * arguments (eg. "$nl$" or "$os$"). The resulting
	 * values must be of type java.lang.String. If the map is <code>null</code>,
	 * or does not contain the required substitution argument, the default
	 * is used.
	 * @return a URL for the given path or <code>null</code>
	 *
	 */ 
	private String getFileFromURL(URL target) {
		String protocol = target.getProtocol();
		if (protocol.equals(PlatformURLHandler.FILE))
			return target.getFile();
		if (protocol.equals(PlatformURLHandler.JAR)) {
			// strip off the jar separator at the end of the url then do a recursive call
			// to interpret the sub URL.
			String file = target.getFile();
			file = file.substring(0, file.length() - PlatformURLHandler.JAR_SEPARATOR.length());
			try {
				return getFileFromURL(new URL(file));
			} catch (MalformedURLException e) {
			}
		}
		return null;
	}
	public static URL find(Bundle bundle, IPath path) {
		return find(bundle, path, null);
	}
	/**
	 * See doc on @link Platform#find(Bundle, IPath) Platform#find(Bundle, IPath) 
	 */
	public static URL find(Bundle b, IPath path, Map override) {
		if (path == null)
			return null;

		URL result = null;

		// Check for the empty or root case first
		if (path.isEmpty() || path.isRoot()) {
			// Watch for the root case.  It will produce a new
			// URL which is only the root directory (and not the
			// root of this plugin).	
			result = findInPlugin(b, Path.EMPTY);
			if (result == null)
				result = findInFragments(b, Path.EMPTY);
			return result;
		}

		// Now check for paths without variable substitution
		String first = path.segment(0);
		if (first.charAt(0) != '$') {
			result = findInPlugin(b, path);
			if (result == null)
				result = findInFragments(b, path);
			return result;
		}

		// Worry about variable substitution
		IPath rest = path.removeFirstSegments(1);
		if (first.equalsIgnoreCase("$nl$")) //$NON-NLS-1$
			return findNL(b,  rest, override);
		if (first.equalsIgnoreCase("$os$")) //$NON-NLS-1$
			return findOS(b, rest, override);
		if (first.equalsIgnoreCase("$ws$")) //$NON-NLS-1$
			return findWS(b, rest, override);
		if (first.equalsIgnoreCase("$files$")) //$NON-NLS-1$
			return null;

		return null;
	}

	private static URL findOS(Bundle b, IPath path, Map override) {
		String os = null;
		if (override != null)
			try {
				// check for override
				os = (String) override.get("$os$"); //$NON-NLS-1$
			} catch (ClassCastException e) {
				// just in case
			}
			if (os == null)
				// use default
				os = InternalPlatform.getDefault().getEnvironmentInfoService().getOS();
			if (os.length() == 0)
				return null;

			// Now do the same for osarch
			String osArch = null;
			if (override != null)
				try {
					// check for override
					osArch = (String) override.get("$arch$"); //$NON-NLS-1$
				} catch (ClassCastException e) {
					// just in case
				}
				if (osArch == null)
					// use default
					osArch = InternalPlatform.getDefault().getEnvironmentInfoService().getOSArch();
				if (osArch.length() == 0)
					return null;

				URL result = null;
				IPath base = new Path("os").append(os).append(osArch); //$NON-NLS-1$
				// Keep doing this until all you have left is "os" as a path
				while (base.segmentCount() != 1) {
					IPath filePath = base.append(path);
					result = findInPlugin(b, filePath);
					if (result != null)
						return result;
					result = findInFragments(b, filePath);
					if (result != null)
						return result;
					base = base.removeLastSegments(1);
				}
				// If we get to this point, we haven't found it yet.
				// Look in the plugin and fragment root directories
				result = findInPlugin(b, path);
				if (result != null)
					return result;
				return findInFragments(b, path);
	}

	private static URL findWS(Bundle b, IPath path, Map override) {
		String ws = null;
		if (override != null)
			try {
				// check for override
				ws = (String) override.get("$ws$"); //$NON-NLS-1$
			} catch (ClassCastException e) {
				// just in case
			}
			if (ws == null)
				// use default
				ws = InternalPlatform.getDefault().getEnvironmentInfoService().getWS();
			IPath filePath = new Path("ws").append(ws).append(path); //$NON-NLS-1$
			// We know that there is only one segment to the ws path
			// e.g. ws/win32	
			URL result = findInPlugin(b, filePath);
			if (result != null)
				return result;
			result = findInFragments(b, filePath);
			if (result != null)
				return result;
			// If we get to this point, we haven't found it yet.
			// Look in the plugin and fragment root directories
			result = findInPlugin(b,path);
			if (result != null)
				return result;
			return findInFragments(b, path);
	}

	private static URL findNL(Bundle b, IPath path, Map override) {
		String nl = null;
		String[] nlVariants = null;
		if (override != null)
			try {
				// check for override
				nl = (String) override.get("$nl$"); //$NON-NLS-1$
			} catch (ClassCastException e) {
				// just in case
			}
			nlVariants = nl == null ? NL_JAR_VARIANTS : buildNLVariants(nl);
			if (nl != null && nl.length() == 0)
				return null;

			URL result = null;
			for (int i = 0; i < nlVariants.length; i++) {
				IPath filePath = new Path(nlVariants[i]).append(path);
				result = findInPlugin(b, filePath);
				if (result != null)
					return result;
				result = findInFragments(b, filePath);
				if (result != null)
					return result;
			}
			// If we get to this point, we haven't found it yet.
			// Look in the plugin and fragment root directories
			result = findInPlugin(b, path);
			if (result != null)
				return result;
			return findInFragments(b, path);
	}

	private static URL findInPlugin(Bundle b, IPath filePath) {
		try {
			return b.getEntry(filePath.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static URL findInFragments(Bundle b, IPath filePath) {
		Bundle[] fragments = b.getFragments();
		if (fragments == null)
			return null;

		URL fileURL = null;
		int i = 0;
		while (i < fragments.length && fileURL == null) {
			try {
				fileURL = fragments[i].getEntry(filePath.toString());
			} catch (IOException e) {
				//ignore
			}
			i++;
		}
		return fileURL;
	}

	/**
	 * Returns an input stream for the specified file. The file path
	 * must be specified relative to this plug-in's installation location.
	 * Optionally, the platform searches for the correct localized version
	 * of the specified file using the users current locale, and Java
	 * naming convention for localized resource files (locale suffix appended 
	 * to the specified file extension).
	 * <p>
	 * The caller must close the returned stream when done.
	 * </p>
	 *
	 * @param file path relative to plug-in installation location
	 * @param localized <code>true</code> for the localized version
	 *   of the file, and <code>false</code> for the file exactly
	 *   as specified
	 * @return an input stream
	 */
	public static final InputStream openStream(Bundle b, IPath file, boolean localized) throws IOException {
		URL url = b.getEntry(file.toString());
		if (url != null)
			return url.openStream();
		return null;
		//TODO Need to put support to do the localization. 
	}
	
}
