/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.browser;
import org.eclipse.help.browser.IBrowser;

/**
 * Implmentation of IBrowser interface using the windows default browser
 */
public class SimpleBrowserAdapter implements IBrowser {
	String[] cmdarray;
	/**
	 * Adapter constructor.
	 */
	public SimpleBrowserAdapter() {
	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public void displayURL(String url) throws Exception {
		if (System.getProperty("os.name").startsWith("Win")) {
			Runtime.getRuntime().exec("cmd /c start "+url);
		}
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