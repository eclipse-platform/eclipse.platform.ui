/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.help.internal.browser.macosx;

import java.io.*;

import org.eclipse.help.browser.*;
import org.eclipse.help.internal.base.*;

public class DefaultBrowserAdapter implements IBrowser {

	private static DefaultBrowserAdapter fgInstance;

	static DefaultBrowserAdapter getInstance() {
		if (fgInstance == null)
			fgInstance = new DefaultBrowserAdapter();
		return fgInstance;
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#close()
	 */
	@Override
	public void close() {
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#displayURL(String)
	 */
	@Override
	public void displayURL(String url) {
		/*
		 * Code from Marc-Antoine Parent
		 */
		try {
			Runtime.getRuntime().exec(new String[] { "/usr/bin/osascript", //$NON-NLS-1$
					"-e", //$NON-NLS-1$
					"open location \"" + url + "\"" }); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException ioe) {
			HelpBasePlugin.logError("Launching \"osascript\" has failed.", ioe); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#isCloseSupported()
	 */
	@Override
	public boolean isCloseSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#isSetLocationSupported()
	 */
	@Override
	public boolean isSetLocationSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#isSetSizeSupported()
	 */
	@Override
	public boolean isSetSizeSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#setLocation(int, int)
	 */
	@Override
	public void setLocation(int x, int y) {
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#setSize(int, int)
	 */
	@Override
	public void setSize(int width, int height) {
	}
}
