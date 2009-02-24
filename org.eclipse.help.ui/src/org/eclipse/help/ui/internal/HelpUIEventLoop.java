/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;
import org.eclipse.help.internal.base.*;
import org.eclipse.swt.widgets.*;
public class HelpUIEventLoop {
	/**
	 * Indicates whether run had a chance to execute and display got created
	 */
	private static boolean started = false;
	/**
	 * Indicates whether it is still running
	 */
	private static boolean running = false;
	private static Display display;
	/**
	 * Called by base in stand-alone help since it cannot run event loop
	 */
	public static void run() {
		try {
			if (display == null)
				display = Display.getCurrent();
			if (display == null)
				display = new Display();
		} finally {
			started = true;
		}
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
			display = null;
		} finally {
			running = false;
		}
	}
	public static void wakeup() {
		Display d = display;
		if (d != null)
			try {
				d.wake();
			} catch (Exception e) {
			}
	}
	/**
	 * Blocks until the loop is started (Display created)
	 */
	public static void waitFor() {
		while (!started && HelpApplication.isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
	}
	/**
	 * @return Returns if loop is running.
	 */
	public static boolean isRunning() {
		return running;
	}
}
