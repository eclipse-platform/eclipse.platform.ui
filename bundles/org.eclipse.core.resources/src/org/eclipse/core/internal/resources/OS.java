/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.Arrays;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Captures platform specific attributes relevant to the core resources plugin.  This
 * class is not intended to be instantiated.
 */
public abstract class OS {
	private static final String INSTALLED_PLATFORM;

	public static final char[] INVALID_RESOURCE_CHARACTERS;
	public static final String[] INVALID_RESOURCE_NAMES;

	static {
		//find out the OS being used
		//setup the invalid names
		char[] chars = null;
		String[] names = null;
		INSTALLED_PLATFORM = Platform.getOS();
		if (INSTALLED_PLATFORM.equals(Constants.OS_WIN32)) {
			//list taken from http://support.microsoft.com/support/kb/articles/q177/5/06.asp
			chars = new char[] {'\\', '/', ':', '*', '?', '"', '<', '>', '|'};

			//list taken from http://support.microsoft.com/support/kb/articles/Q216/6/54.ASP
			names = new String[] {"..", ".", "aux", "clock$", "com1", "com2", "com3", "com4", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
					"com5", "com6", "com7", "com8", "com9", "con", "lpt1", "lpt2", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
					"lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "nul", "prn"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		} else {
			//only front slash and null char are invalid on UNIXes
			//taken from http://www.faqs.org/faqs/unix-faq/faq/part2/section-2.html
			chars = new char[] {'/', '\0',};
			//'.' and '..' have special meaning, and so can't be used as resource names
			names = new String[] {".", ".."}; //$NON-NLS-1$ //$NON-NLS-2$
		}
		INVALID_RESOURCE_CHARACTERS = chars == null ? new char[0] : chars;
		Arrays.sort(names);
		INVALID_RESOURCE_NAMES = names;
	}

	/**
	 * Returns true if the given name is a valid resource name on this operating system,
	 * and false otherwise.
	 */
	public static boolean isNameValid(String name) {
		if (INSTALLED_PLATFORM.equals(Constants.OS_WIN32)) {
			//on windows, filename suffixes are not relevant to name validity
			int dot = name.indexOf('.');
			name = dot == -1 ? name : name.substring(0, dot);
		}
		return Arrays.binarySearch(INVALID_RESOURCE_NAMES, name.toLowerCase()) < 0;
	}
}