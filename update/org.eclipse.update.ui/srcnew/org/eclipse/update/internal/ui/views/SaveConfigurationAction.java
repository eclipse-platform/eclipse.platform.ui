package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUI;

public class SaveConfigurationAction extends Action {
	private IInstallConfiguration target;

	public SaveConfigurationAction(String text) {
		super(text);
	}

	public void setConfiguration(IInstallConfiguration target) {
		this.target = target;
	}

	public void run() {
		if (target == null)
			return;
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			localSite.addToPreservedConfigurations(target);
			localSite.save();
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}
}
