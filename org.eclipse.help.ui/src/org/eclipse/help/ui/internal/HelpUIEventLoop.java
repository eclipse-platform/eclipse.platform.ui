/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.help.ui.internal;
import org.eclipse.help.internal.base.*;
import org.eclipse.swt.widgets.*;
public class HelpUIEventLoop {
	private static boolean running = false;
	/**
	 * Called by base in stand-alone help since it cannot run event loop
	 */
	public static void run() {
		Display display = Display.getCurrent();
		if (display == null)
			display = new Display();
		try {
			running = true;
			while (HelpApplication.isRunning()) {
				try {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				} catch (Throwable t) {
					HelpBasePlugin.logError(t.getMessage(), t);
				}
			}
			display.dispose();
		} finally {
			running = false;
		}

	}
	/**
	 * @return Returns if loop is running.
	 */
	public static boolean isRunning() {
		return running;
	}
}