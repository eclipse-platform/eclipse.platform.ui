package org.eclipse.update.internal.ui.views;

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.operations.*;

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

			boolean isConfigured = adapter.isConfigured();
			IOperation toggleOperation =
				isConfigured
					? UpdateManager
						.getOperationsManager()
						.createUnconfigOperation(
						adapter.getInstallConfiguration(),
						adapter.getConfiguredSite(),
						adapter.getFeature(null))
					: UpdateManager.getOperationsManager().createConfigOperation(
						adapter.getInstallConfiguration(),
						adapter.getConfiguredSite(),
						adapter.getFeature(null));

			boolean restartNeeded = toggleOperation.execute(null, null);			
			if (restartNeeded)
				UpdateUI.informRestartNeeded();

		} catch (CoreException e) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				e.getStatus());
		} catch (InvocationTargetException e) {
			// This should not happen
			UpdateManager.logException(e.getTargetException());
		}
	}

	private boolean confirm(boolean isConfigured) {
		String message =
			isConfigured
				? "Do you want to disable this feature?"
				: "Do you want to enable this feature?";
		return MessageDialog.openConfirm(
			UpdateUI.getActiveWorkbenchShell(),
			"Update Manager",
			message);
	}

}
