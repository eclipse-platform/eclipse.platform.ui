/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;
/**
 * Helper class to route trace output.
 */
public class Trace {
	public static int CONFIG = 0;
	public static int WARNING = 2;
	public static int SEVERE = 3;
	public static int FINER = 4;
	public static int FINEST = 5;

	/**
	 * Trace constructor comment.
	 */
	private Trace() {
		super();
	}

	/**
	 * Trace the given text.
	 *
	 * @param s java.lang.String
	 */
	public static void trace(int level, String s) {
		Trace.trace(level, s, null);
	}

	/**
	 * Trace the given message and exception.
	 *
	 * @param s java.lang.String
	 * @param t java.lang.Throwable
	 */
	public static void trace(int level, String s, Throwable t) {
		if (!WebBrowserUIPlugin.getInstance().isDebugging()) {
			return;
		}

		System.out.println(s);
		if (t != null) {
			t.printStackTrace();
		}
	}
}