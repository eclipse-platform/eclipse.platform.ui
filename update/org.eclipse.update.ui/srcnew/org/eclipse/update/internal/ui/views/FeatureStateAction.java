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
import org.eclipse.update.internal.ui.model.PendingChange;
import org.eclipse.update.internal.ui.model.UpdateModel;

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
					boolean restartNeeded = false;
					if (isConfigured) {
						restartNeeded =
							addPendingChange(
								PendingChange.UNCONFIGURE,
								PendingChange.CONFIGURE);
					} else {
						restartNeeded =
							addPendingChange(
								PendingChange.CONFIGURE,
								PendingChange.UNCONFIGURE);
					}
					if (restartNeeded)
						UpdateUI.informRestartNeeded();

					SiteManager.getLocalSite().save();
					UpdateUI.getDefault().getUpdateModel().fireObjectChanged(adapter, "");
				} catch (CoreException e) {
					revert(isConfigured);
					UpdateUI.logException(e);
				}
			}

		} catch (CoreException e) {
			UpdateUI.logException(e);
		}

	}

	private void revert(boolean originallyConfigured) throws CoreException {
		if (originallyConfigured) {
			site.configure(feature);
		} else {
			site.unconfigure(feature);
		}
	}

	private boolean addPendingChange(int newJobType, int obsoleteJobType) {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		PendingChange job = model.findPendingChange(feature);
		if (job != null && obsoleteJobType == job.getJobType()) {
			model.removePendingChange(job);
			return false;
		} else {
			model.addPendingChange(new PendingChange(feature, newJobType));
			return true;
		}
	}

}
