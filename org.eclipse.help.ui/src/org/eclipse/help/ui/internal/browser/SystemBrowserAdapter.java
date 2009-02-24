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
package org.eclipse.help.ui.internal.browser;

import org.eclipse.help.browser.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.osgi.util.NLS;
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
			HelpUIPlugin
					.logError(
							"Browser adapter for System Browser failed.  The system has no program registered for file " //$NON-NLS-1$
									+ url
									+ ".  Change the file association or choose a different help web browser in the preferences.", //$NON-NLS-1$
							null);
			ErrorUtil.displayErrorDialog(NLS.bind(Messages.SystemBrowser_noProgramForURL, url));
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
