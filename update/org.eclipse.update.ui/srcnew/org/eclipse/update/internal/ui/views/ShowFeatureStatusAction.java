package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.IImport;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;
import org.eclipse.update.internal.ui.model.MissingFeature;

public class ShowFeatureStatusAction extends Action {
	private static final String KEY_MISSING_OPTIONAL_STATUS =
		"ConfigurationView.missingOptionalStatus";
	private static final String KEY_MISSING_STATUS = "ConfigurationView.missingStatus";
	private static final String KEY_STATUS_TITLE = "ConfigurationView.statusTitle";
	private static final String KEY_STATUS_DEFAULT = "ConfigurationView.statusDefault";

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
			status = new Status(severity, UpdateUI.PLUGIN_ID, IStatus.OK, msg, null);
		} else {
			status = SiteManager.getLocalSite().getFeatureStatus(feature);
		}
		String title = UpdateUI.getString(KEY_STATUS_TITLE);
		int severity = status.getSeverity();
		String message = status.getMessage();

		if (severity == IStatus.ERROR && status.getCode() == IFeature.STATUS_UNHAPPY) {
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
				if (arePatchesObsolete(feature)) {
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
	
	private static boolean arePatchesObsolete(IFeature feature) {
		// Check all the included features that
		// are unconfigured, and see if their patch 
		// references are better than the original.
		try {
			IFeatureReference[] irefs = feature.getIncludedFeatureReferences();
			for (int i = 0; i < irefs.length; i++) {
				IFeatureReference iref = irefs[i];
				IFeature ifeature = iref.getFeature(null);
				IConfiguredSite csite = ifeature.getSite().getCurrentConfiguredSite();
				if (!csite.isConfigured(ifeature)) {
					if (!isPatchHappy(ifeature))
						return false;
				}
			}
		} catch (CoreException e) {
			return false;
		}
		// All checks went well
		return true;
	}
	
	private static boolean isPatchHappy(IFeature feature) throws CoreException {
		// If this is a patch and it includes 
		// another patch and the included patch
		// is disabled but the feature it was declared
		// to patch is now newer (and is presumed to
		// contain the now disabled patch), and
		// the newer patched feature is enabled,
		// a 'leap of faith' assumption can be
		// made:

		// Although the included patch is disabled,
		// the feature it was designed to patch
		// is now newer and most likely contains
		// the equivalent fix and more. Consequently,
		// we can claim that the status and the error
		// icon overlay are misleading because
		// all the right plug-ins are configured.
		IImport[] imports = feature.getImports();
		IImport patchReference = null;
		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			if (iimport.isPatch()) {
				patchReference = iimport;
				break;
			}
		}
		if (patchReference == null)
			return false;
		VersionedIdentifier refVid = patchReference.getVersionedIdentifier();

		// Find the patched feature and 
		IConfiguredSite csite = feature.getSite().getCurrentConfiguredSite();
		if (csite == null)
			return false;

		IFeatureReference[] crefs = csite.getConfiguredFeatures();
		for (int i = 0; i < crefs.length; i++) {
			IFeatureReference cref = crefs[i];
			VersionedIdentifier cvid = cref.getVersionedIdentifier();
			if (cvid.getIdentifier().equals(refVid.getIdentifier())) {
				// This is the one.
				if (cvid.getVersion().isGreaterThan(refVid.getVersion())) {
					// Bingo: we found the referenced feature
					// and its version is greater - 
					// we can assume that it contains better code
					// than the patch that referenced the
					// older version.
					return true;
				}
			}
		}
		return false;
	}

}
