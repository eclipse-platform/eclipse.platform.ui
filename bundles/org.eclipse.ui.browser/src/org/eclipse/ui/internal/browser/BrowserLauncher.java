package org.eclipse.ui.internal.browser;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class BrowserLauncher implements IEditorLauncher {

	public BrowserLauncher() {
	}

	public void open(IPath file) {
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			support.createBrowser(DefaultBrowserSupport.SHARED_ID).openURL(file.toFile().toURL());
		}
		catch (MalformedURLException e) {
		}
		catch (PartInitException e) {
			//TODO Report this exception
		}
	}
}