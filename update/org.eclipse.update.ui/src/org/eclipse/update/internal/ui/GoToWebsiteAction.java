package org.eclipse.update.internal.ui;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.custom.*;
import org.eclipse.update.internal.ui.model.*;


public class GoToWebsiteAction extends Action {
	
	SiteBookmark bookmark;
	
	public GoToWebsiteAction(SiteBookmark bookmark) {
		this.bookmark = bookmark;
		setText(bookmark.getLabel());
		setImageDescriptor(UpdateUIImages.DESC_WEB_SITE_OBJ);
	}
	
	public void run() {
		BusyIndicator.showWhile(UpdateUI.getActiveWorkbenchShell().getDisplay(), new Runnable() {
			public void run() {
				try {
					if (!UpdateUI.getDefault().isWebAppStarted())
						UpdateUI.getDefault().startWebApp();
					showURL(bookmark.getURL().toString());
				} catch (CoreException e) {
					UpdateUI.logException(e);
				}
			}
		});		
	}
	
	private void showURL(String url) {
		UpdateUI.showURL(url, true);
	}

}
