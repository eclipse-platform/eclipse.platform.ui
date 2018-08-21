/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.testplugin;

/**
 * Utility class
 */
public class ProjectCreationDecorator {

	private static boolean fgIsJ2SE15Compatible = false;

	static {
		String version = System.getProperty("java.specification.version"); //$NON-NLS-1$
		if (version != null) {
			String[] nums = version.split("\\."); //$NON-NLS-1$
			if (nums.length == 2) {
				try {
					int major = Integer.parseInt(nums[0]);
					int minor = Integer.parseInt(nums[1]);
					if (major >= 1) {
						if (minor >= 5) {
							fgIsJ2SE15Compatible = true;
						}
					}
				}
				catch (NumberFormatException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * @return if the system property "java.specification.version" is 1.5 or greater
	 */
	public static boolean isJ2SE15Compatible() {
		return fgIsJ2SE15Compatible;
	}
}
