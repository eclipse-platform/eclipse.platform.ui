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
package org.eclipse.help.internal.browser;
import org.eclipse.help.browser.*;
/**
 * Wrapper for individual browsers contributed through extension point.
 */
public class CurrentBrowser implements IBrowser {
	private IBrowser browserAdapter;
	private String browserAdapterId;
	/**
	 * new adapter selected in preferences but not yet shown
	 */
	private IBrowser newBrowserAdapter = null;
	private String newBrowserAdapterId = null;
	private boolean locationSet = false;
	private boolean sizeSet = false;
	private int x;
	private int y;
	private int width;
	private int height;
	boolean external;
	public CurrentBrowser(IBrowser browserImpl, String browserAdapterId,
			boolean externalBrowser) {
		this.browserAdapter = browserImpl;
		this.browserAdapterId = browserAdapterId;
		this.external = externalBrowser;
	}
	/**
	 * @see org.eclipse.help.browser.IBrowser#close()
	 */
	public void close() {
		browserAdapter.close();
	}
	/**
	 * @see org.eclipse.help.browser.IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return browserAdapter.isCloseSupported();
	}
	/**
	 * @see org.eclipse.help.browser.IBrowser#displayURL(java.lang.String)
	 */
	public void displayURL(String url) throws Exception {
		checkDefaultAdapter();
		if (newBrowserAdapter != null) {
			browserAdapter.close();
			browserAdapter = newBrowserAdapter;
			newBrowserAdapter = null;
			browserAdapterId = newBrowserAdapterId;
			newBrowserAdapterId = null;
			if (locationSet) {
				browserAdapter.setLocation(x, y);
			}
			if (sizeSet) {
				browserAdapter.setSize(width, height);
			}
		}
		browserAdapter.displayURL(url);
	}
	/**
	 * @see org.eclipse.help.browser.IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		checkDefaultAdapter();
		if (newBrowserAdapterId == null) {
			return browserAdapter.isSetLocationSupported();
		}
		return browserAdapter.isSetLocationSupported()
				|| newBrowserAdapter.isSetLocationSupported();
	}
	/**
	 * @see org.eclipse.help.browser.IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		checkDefaultAdapter();
		if (newBrowserAdapterId == null) {
			return browserAdapter.isSetSizeSupported();
		}
		return browserAdapter.isSetSizeSupported()
				|| newBrowserAdapter.isSetSizeSupported();
	}
	/**
	 * @see org.eclipse.help.browser.IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
		checkDefaultAdapter();
		browserAdapter.setLocation(x, y);
		locationSet = true;
		this.x = x;
		this.y = y;
	}
	/**
	 * @see org.eclipse.help.browser.IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		checkDefaultAdapter();
		browserAdapter.setSize(width, height);
		sizeSet = true;
		this.width = width;
		this.height = height;
	}
	/*
	 * Checks wheter default adapter has changed. If yes, sets the
	 * newBrowserAdapterId field
	 */
	private void checkDefaultAdapter() {
		if (external) {
			if (browserAdapterId != BrowserManager.getInstance()
					.getCurrentBrowserID()) {
				newBrowserAdapter = BrowserManager.getInstance().createBrowser(
						true);
				newBrowserAdapterId = BrowserManager.getInstance()
						.getCurrentBrowserID();
			}
		} else {
			if (browserAdapterId != BrowserManager.getInstance()
					.getCurrentInternalBrowserID()) {
				newBrowserAdapter = BrowserManager.getInstance().createBrowser(
						false);
				newBrowserAdapterId = BrowserManager.getInstance()
						.getCurrentInternalBrowserID();
			}
		}
	}
}
