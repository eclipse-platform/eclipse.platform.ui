/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.swt.program.Program;
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
		if (System.getProperty("os.name").startsWith("Win")) {
			if (!Program.launch(url)) {
				ErrorUtil.displayErrorDialog(
					WorkbenchResources.getString(
						"SystemBrowser.noProgramForURL",
						url));
			}
		} else {
			Program b = Program.findProgram("html");
			if (b == null || !b.execute(url)) {
				ErrorUtil.displayErrorDialog(
					WorkbenchResources.getString(
						"SystemBrowser.noProgramForHTML",
						url));
			}
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