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
package org.eclipse.help.ui.internal.browser;
import org.eclipse.help.browser.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.swt.program.*;
/**
 * Implmentation of IBrowser interface, using org.eclipse.swt.Program
 */
public class SystemBrowserAdapter implements IBrowser {
	String[] cmdarray;
	/**
	 * Adapter constructor.
	 */
	public SystemBrowserAdapter() {
	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public void displayURL(String url) {
		//		if (Constants.WS_WIN32.equalsIgnoreCase(Platform.getOS())) {
		if (!Program.launch(url)) {
			ErrorUtil.displayErrorDialog(HelpUIResources.getString(
					"SystemBrowser.noProgramForURL", //$NON-NLS-1$
					url));
		}
		//		} else {
		//			Program b = Program.findProgram("html");
		//			if (b == null || !b.execute(url)) {
		//				ErrorUtil.displayErrorDialog(
		//					HelpUIResources.getString(
		//						"SystemBrowser.noProgramForHTML",
		//						url));
		//			}
		//		}
	}
	/*
	 * @see IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return false;
	}
	/*
	 * @see IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		return false;
	}
	/*
	 * @see IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return false;
	}
	/*
	 * @see IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
	}
}
