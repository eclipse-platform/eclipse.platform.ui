/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.keys;

/**
 * A cache for formatters. It keeps a bunch of formatters around for use within
 * the application.
 */
public class FormatManager {

	/**
	 * An integer constant used to ask for a <code>FormalKeyFormatter</code>.
	 * This formatter is used for persistence.
	 */
	public static final int FORMAL = 0;

	/**
	 * A formal formatter instance.
	 */
	private static final KeyFormatter FORMAL_FORMATTER = new FormalKeyFormatter();

	/**
	 * An integer constant used to ask for a <code>NativeKeyFormatter</code>.
	 * This formatter is used for the default menu layout.
	 */
	public static final int NATIVE = 1;

	/**
	 * A native formatter instance.
	 */
	private static final KeyFormatter NATIVE_FORMATTER = new NativeKeyFormatter();

	/**
	 * Retrieves a formatter for the given <code>format</code>. This uses
	 * some caching mechanism to avoid constructing multiple instances of the
	 * same formatter.
	 */
	public static final KeyFormatter getFormatter(int format) {
		switch (format) {
			case NATIVE :
				return NATIVE_FORMATTER;

			case FORMAL :
			default :
				return FORMAL_FORMATTER;
		}
	}

	/**
	 * This class should never be instantied.
	 */
	private FormatManager() {
		// This class should not be instantiated.
	}
}
