package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;

public class FeatureStateAction extends Action {
	private ConfiguredFeatureAdapter adapter;

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
			if (adapter == null || !confirm(adapter.isConfigured()))
				return;
				
			boolean restartNeeded =
				UpdateManager.getOperationsManager().toggleFeatureState(
					adapter.getInstallConfiguration(),
					adapter.getConfiguredSite(),
					adapter.getFeature(null),
					adapter.isConfigured(),
					adapter);

			if (restartNeeded)
				UpdateUI.informRestartNeeded();

		} catch (CoreException e) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				e.getStatus());
		}
	}
	
	private boolean confirm(boolean isConfigured) {
		String message = isConfigured ? "Do you want to disable this feature?" : "Do you want to enable this feature?";
		return MessageDialog.openConfirm(UpdateUI.getActiveWorkbenchShell(), "Update Manager", message); 
	}


}
