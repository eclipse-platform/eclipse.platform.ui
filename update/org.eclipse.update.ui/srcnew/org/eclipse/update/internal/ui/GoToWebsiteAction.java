package org.eclipse.update.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.internal.browser.BrowserManager;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.program.Program;
import org.eclipse.update.internal.ui.model.SiteBookmark;


public class GoToWebsiteAction extends Action {
	
	SiteBookmark bookmark;
	
	public GoToWebsiteAction(SiteBookmark bookmark) {
		this.bookmark = bookmark;
		setText(bookmark.getLabel());
	}
	
	public void run() {
		BusyIndicator.showWhile(UpdateUI.getActiveWorkbenchShell().getDisplay(), new Runnable() {
			public void run() {
				try {
					if (!UpdateUI.getDefault().isWebAppStarted())
						UpdateUI.getDefault().startWebApp();
					showURL(bookmark.getURL().toString());
				} catch (CoreException e) {
				}
			}
		});		
	}
	
	private void showURL(String url) {
		if (SWT.getPlatform().equals("win32")) {
			Program.launch(url);
		} else {
			IBrowser browser = BrowserManager.getInstance().createBrowser();
			try {
				browser.displayURL(url);
			} catch (Exception e) {
				UpdateUI.logException(e);
			}
		}
	}

}
