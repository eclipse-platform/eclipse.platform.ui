/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.help.internal.util.HelpPreferences;
import org.eclipse.help.ui.browser.IBrowser;
import org.eclipse.swt.widgets.Display;
public class IEBrowserAdapter implements IBrowser {
	IEHost ieHost;
	int x, y;
	int width, height;
	boolean setLocationPending;
	boolean setSizePending;
	public IEBrowserAdapter() {
		Display display = new Display();
		display.setAppName(WorkbenchResources.getString("browserTitle"));
		setLocationPending = false;
		setSizePending = false;
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public void displayURL(String url) {
		if (ieHost == null || ieHost.isDisposed()) {
			// browser not opened yet
			ieHost = new IEHost();
			if (setLocationPending) {
				// use given location
				ieHost.setLocation(x, y);
			} else {
				// use saved locacation and size
				HelpPreferences pref = HelpSystem.getPreferences();
				int x = pref.getInt(IEHost.BROWSER_X);
				int y = pref.getInt(IEHost.BROWSER_Y);
				setLocation(x, y);
			}
			setLocationPending = false;
			if (setSizePending) {
				// use given size
				ieHost.setSize(width, height);
			} else {
				// use saved locacation and size
				HelpPreferences pref = HelpSystem.getPreferences();
				int w = pref.getInt(IEHost.BROWSER_WIDTH);
				int h = pref.getInt(IEHost.BROWSER_HEIGTH);
				if (w == 0 || h == 0) {
					// use defaults
					w = 640;
					h = 480;
				}
				setSize(w, h);
			}
			setSizePending = false;
		}
		ieHost.displayURL(url);
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
		this.x = x;
		this.y = y;
		if (ieHost != null && !ieHost.isDisposed()) {
			ieHost.setLocation(x, y);
		} else {
			setLocationPending = true;
		}
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		if (ieHost != null && !ieHost.isDisposed()) {
			ieHost.setSize(width, height);
		} else {
			setSizePending = true;
		}
	}
}