/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;
import org.eclipse.help.ui.browser.*;

/**
 * Creates browser by delegating
 * to appropriate browser adapter
 * @deprecated Use org.eclipse.help.interna
 */
public class BrowserManager {
	private static BrowserManager instance;
	
	/**
	 * Private Constructor
	 */
	private BrowserManager() {
	}
	/**
	 * Obtains singleton instance.
	 */
	public static BrowserManager getInstance() {
		if (instance == null)
			instance = new BrowserManager();
		return instance;
	}

	/**
	 * Creates web browser
	 */
	public IBrowser createBrowser() {
		return null;
	}
	
	/**
	 * Closes all browsers created
	 */
	public void closeAll() {
	}
}