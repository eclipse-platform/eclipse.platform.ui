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
			if (adapter == null)
				return;

			boolean restartNeeded =
				UpdateManager.getOperationsManager().toggleFeatureState(
					adapter.getConfiguredSite(),
					adapter.getFeature(null),
					adapter.isConfigured(),
					adapter);

			if (restartNeeded)
				UpdateUI.informRestartNeeded();

		} catch (CoreException e) {
			UpdateUI.logException(e);
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				e.getStatus());
		}

	}

}
