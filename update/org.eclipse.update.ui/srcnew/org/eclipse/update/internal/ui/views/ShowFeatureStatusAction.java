package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;

public class ShowFeatureStatusAction extends Action {
	private static final String KEY_MISSING_OPTIONAL_STATUS =
		"ConfigurationView.missingOptionalStatus";
	private static final String KEY_MISSING_STATUS =
		"ConfigurationView.missingStatus";
	private static final String KEY_STATUS_TITLE =
		"ConfigurationView.statusTitle";
	private static final String KEY_STATUS_DEFAULT =
		"ConfigurationView.statusDefault";

	private IFeatureAdapter adapter;

	public ShowFeatureStatusAction(String text) {
		super(text);
	}

	public void setFeature(IFeatureAdapter adapter) {
		this.adapter = adapter;
	}

	public void run() {
		try {
			if (adapter != null) {
				IFeature feature = adapter.getFeature(null);
				showFeatureStatus(feature);
			}
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}

	private void showFeatureStatus(IFeature feature) throws CoreException {
		IStatus status;
		if (feature instanceof MissingFeature) {
			MissingFeature missingFeature = (MissingFeature) feature;
			String msg;
			int severity;

			if (missingFeature.isOptional()) {
				severity = IStatus.INFO;
				msg = UpdateUI.getString(KEY_MISSING_OPTIONAL_STATUS);
			} else {
				severity = IStatus.ERROR;
				msg = UpdateUI.getString(KEY_MISSING_STATUS);
			}
			status =
				new Status(severity, UpdateUI.PLUGIN_ID, IStatus.OK, msg, null);
		} else {
			status = SiteManager.getLocalSite().getFeatureStatus(feature);
		}
		String title = UpdateUI.getString(KEY_STATUS_TITLE);
		int severity = status.getSeverity();
		String message = status.getMessage();

		if (severity == IStatus.ERROR
			&& status.getCode() == IFeature.STATUS_UNHAPPY) {
			//see if this is a false alarm
			int code = getStatusCode(feature, status);
			if (code == IFeature.STATUS_HAPPY) {
				// This was a patch referencing a
				// subsumed patch - change to happy.
				severity = IStatus.INFO;
				message = null;
			}
		}

		switch (severity) {
			case IStatus.ERROR :
				ErrorDialog.openError(
					UpdateUI.getActiveWorkbenchShell(),
					title,
					null,
					status);
				break;
			case IStatus.WARNING :
				MessageDialog.openWarning(
					UpdateUI.getActiveWorkbenchShell(),
					title,
					status.getMessage());
				break;
			case IStatus.INFO :
			default :
				if (message == null || message.length() == 0)
					message = UpdateUI.getString(KEY_STATUS_DEFAULT);
				MessageDialog.openInformation(
					UpdateUI.getActiveWorkbenchShell(),
					title,
					message);
		}
	}

	public static int getStatusCode(IFeature feature, IStatus status) {
		int code = status.getCode();
		if (code == IFeature.STATUS_UNHAPPY) {
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				for (int i = 0; i < children.length; i++) {
					IStatus child = children[i];
					if (child.isMultiStatus())
						return code;
					if (child.getCode() != IFeature.STATUS_DISABLED)
						return code;
				}
				// If we are here, global status is unhappy
				// because one or more included features
				// is disabled.
				if (UpdateManager.hasObsoletePatches(feature)) {
					// The disabled included features
					// are old patches that are now
					// subsumed by better versions of
					// the features they were designed to
					// patch.
					return IFeature.STATUS_HAPPY;
				}
			}
		}
		return code;
	}
}
