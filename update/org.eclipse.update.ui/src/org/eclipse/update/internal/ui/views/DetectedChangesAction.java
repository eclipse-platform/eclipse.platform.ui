package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUI;


public class DetectedChangesAction extends Action {

	public DetectedChangesAction(String text) {
		super(text);
	}
	
	public void run() {
		BusyIndicator
			.showWhile(UpdateUI.getActiveWorkbenchShell().getDisplay(), new Runnable() {
			public void run() {
				try {
					UpdateUI.setRemindOnCancel(false);
					SiteManager.handleNewChanges();
				} catch (CoreException e) {
					UpdateUI.logException(e);
				}
			}
		});
	}
}
