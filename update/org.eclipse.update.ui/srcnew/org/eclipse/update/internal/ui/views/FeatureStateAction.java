package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;

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
			IStatus status = UpdateManager.getValidator().validateCurrentState();
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
								PendingOperation.UNCONFIGURE,
								PendingOperation.CONFIGURE);
					} else {
						restartNeeded =
							addPendingChange(
								PendingOperation.CONFIGURE,
								PendingOperation.UNCONFIGURE);
					}
					
					SiteManager.getLocalSite().save();
					UpdateManager.getOperationsManager().fireObjectChanged(adapter, "");
					
					if (restartNeeded)
						UpdateUI.informRestartNeeded();

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
		OperationsManager opmgr = UpdateManager.getOperationsManager();
		PendingOperation job = opmgr.findPendingChange(feature);
		if (job != null && obsoleteJobType == job.getJobType()) {
			opmgr.removePendingChange(job);
			return false;
		} else {
			opmgr.addPendingChange(UpdateManager.getOperationsManager().createPendingChange(feature, newJobType));
			return true;
		}
	}

}
