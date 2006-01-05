/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import org.eclipse.swt.widgets.Display;

/*
 * Utility methods for working with Displays.
 */
public class DisplayUtil {

	/*
	 * Flushes and events in the UI thread queue.
	 */
	public static void flush() {
		while(Display.getDefault().readAndDispatch()) {
		}
	}
}
