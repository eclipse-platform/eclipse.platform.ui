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
package org.eclipse.help.ui.internal.browser.embedded;
import org.eclipse.help.browser.*;
/**
 * Web browser.
 */
public class EmbeddedBrowserAdapter implements IBrowser {
	private EmbeddedBrowser browser;
	/**
	 * Adapter constructor.
	 */
	public EmbeddedBrowserAdapter() {
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public synchronized void displayURL(String url) {
		close();
		getBrowser().displayUrl(url);
	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
		if (browser != null && !browser.isDisposed())
			browser.close();
	}
	/**
	 *  
	 */
	private EmbeddedBrowser getBrowser() {
		if (browser == null || browser.isDisposed()) {
			browser = new EmbeddedBrowser();
		}
		return browser;
	}
	/*
	 * @see IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return true;
	}
	/*
	 * @see IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		return true;
	}
	/*
	 * @see IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return true;
	}
	/*
	 * @see IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
		getBrowser().setLocation(x, y);
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		getBrowser().setSize(width, height);
	}
}