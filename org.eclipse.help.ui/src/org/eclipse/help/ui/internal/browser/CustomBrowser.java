/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;

import org.eclipse.help.internal.util.Logger;
import org.eclipse.help.ui.browser.IBrowser;
import org.eclipse.help.ui.internal.WorkbenchHelpPlugin;
import org.eclipse.help.ui.internal.util.*;

/**
 * 
 */
public class CustomBrowser implements IBrowser {
	public static final String CUSTOM_BROWSER_PATH_KEY = "custom_browser_path";

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#close()
	 */
	public void close() {
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#displayURL(java.lang.String)
	 */
	public void displayURL(String url) {
		String path =
			WorkbenchHelpPlugin.getDefault().getPluginPreferences().getString(
				CustomBrowser.CUSTOM_BROWSER_PATH_KEY);
		try {
			Process pr = Runtime.getRuntime().exec(new String[] { path, url });
			Thread outConsumer = new StreamConsumer(pr.getInputStream());
			outConsumer.setName("Custom browser adapter output reader");
			outConsumer.start();
			Thread errConsumer = new StreamConsumer(pr.getErrorStream());
			errConsumer.setName("Custom browser adapter error reader");
			errConsumer.start();
		} catch (Exception e) {
			Logger.logError(
				WorkbenchResources.getString(
					"CustomBrowser.errorLaunching",
					url,
					path),
				e);
			ErrorUtil.displayErrorDialog(
				WorkbenchResources.getString(
					"CustomBrowser.errorLaunching",
					url,
					path));
		}
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
	}

}
