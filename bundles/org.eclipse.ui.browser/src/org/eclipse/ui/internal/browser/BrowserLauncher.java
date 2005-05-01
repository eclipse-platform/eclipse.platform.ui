package org.eclipse.ui.internal.browser;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class BrowserLauncher implements IEditorLauncher {

	public BrowserLauncher() {
		// do nothing
	}

	public void open(IPath file) {
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			support.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR,
					DefaultBrowserSupport.SHARED_ID, null, null).openURL(file.toFile().toURL());
		}
		catch (MalformedURLException e) {
			// ignore
		}
		catch (PartInitException e) {
			//TODO Report this exception
		}
	}
}