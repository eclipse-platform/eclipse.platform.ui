package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.forms.ActivityConstraints;
import org.eclipse.update.internal.ui.model.ConfiguredFeatureAdapter;

public class FeatureStateAction extends Action {
	private ConfiguredFeatureAdapter adapter;
	private IFeature feature;
	private IConfiguredSite site;
	
	public void setFeature(ConfiguredFeatureAdapter adapter) {
		this.adapter = adapter;
		if (adapter.isConfigured()) {
			setText("Disable");
		} else {
			setText("Enable");
		}
	}
	
	public void run() {
		try {
			if (adapter == null)
				return;
				
			feature = adapter.getFeature(null);
			site = adapter.getConfiguredSite();
			boolean isConfigured = adapter.isConfigured();
			if (isConfigured) {
				site.unconfigure(feature);
			} else {
				site.configure(feature);
			}
			IStatus status = ActivityConstraints.validateCurrentState();
			if (status != null) {
				revert(isConfigured);
				ErrorDialog.openError(
					UpdateUI.getActiveWorkbenchShell(),
					null,
					null,
					status);
			} else {
				// do a restart
				try {
					UpdateUI.informRestartNeeded();
					SiteManager.getLocalSite().save();
					UpdateUI.getDefault().getUpdateModel().fireObjectChanged(adapter, "");
				} catch (CoreException e) {
					revert(isConfigured);
					UpdateUI.logException(e);
				}
			}

		} catch (CoreException e) {
		}

	}
	private void revert(boolean originallyConfigured) throws CoreException {
		if (originallyConfigured) {
			site.configure(feature);
		} else {
			site.unconfigure(feature);
		}
	}

}
