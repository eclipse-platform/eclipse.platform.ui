package org.eclipse.update.internal.ui.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.operations.UpdateManager;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.ConfiguredFeatureAdapter;
import org.eclipse.update.internal.ui.model.MissingFeature;
import org.eclipse.update.operations.*;

/**
 * @see PropertyPage
 */
public class FeatureStatusPropertyPage
	extends PropertyPage
	implements IWorkbenchPropertyPage {
	private static final String KEY_MISSING_STATUS = "ConfigurationView.missingStatus";
	private static final String KEY_MISSING_OPTIONAL_STATUS =
		"ConfigurationView.missingOptionalStatus";
	/**
	 *
	 */
	public FeatureStatusPropertyPage() {
		noDefaultAndApplyButton();
	}

	/**
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent) {
		try {

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.verticalSpacing = 20;
			composite.setLayout(layout);

			Text message = new Text(composite, SWT.MULTI | SWT.WRAP);
			message.setEditable(false);
			GridData gd = new GridData();
			gd.widthHint = 350;
			message.setLayoutData(gd);
			
			ConfiguredFeatureAdapter adapter = (ConfiguredFeatureAdapter) getElement();
			IFeature feature = adapter.getFeature(null);
			
			if (OperationsManager.findPendingOperation(feature) != null) {
				message.setText("The feature has pending changes.  Therefore, its status cannot be determined until you restart the workbench.");
				return composite;
			}
			
			IStatus status = getStatus(feature);
			int severity = status.getSeverity();
			if (severity == IStatus.ERROR
				&& getStatusCode(feature, status) == IFeature.STATUS_HAPPY) {
				severity = IStatus.OK;
			}
			
			message.setText(status.getMessage());

			if (severity != IStatus.OK && status.isMultiStatus()) {
				String reason = getReason(status);
				if (reason.length() > 0) {
					Composite comp = new Composite(composite, SWT.NONE);
					comp.setLayout(new GridLayout());
					gd = new GridData(GridData.FILL_BOTH);
					comp.setLayoutData(gd);

					Label label = new Label(comp, SWT.NONE);
					label.setText("Reason:");

					Text text =
						new Text(comp, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
					text.setEditable(false);
					text.setText(reason);
					gd.widthHint = 350;
					text.setLayoutData(gd);
				}

			}
			
			Dialog.applyDialogFont(parent);
			
			return composite;

		} catch (CoreException e) {
		}

		return null;
	}

	private String getReason(IStatus status) {
		IStatus[] children = status.getChildren();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < children.length; i++) {
			String message = children[i].getMessage();
			if (message != null && message.length() > 0) {
				buffer.append(
					message
						+ System.getProperty("line.separator")
						+ System.getProperty("line.separator"));
			}
		}
		return buffer.toString();
	}

	private IStatus getStatus(IFeature feature) throws CoreException {
		if (feature instanceof MissingFeature) {
			int severity;
			String message = "";
			if (((MissingFeature) feature).isOptional()) {
				severity = IStatus.OK;
				message = UpdateUI.getString(KEY_MISSING_OPTIONAL_STATUS);
			} else {
				severity = IStatus.ERROR;
				message = UpdateUI.getString(KEY_MISSING_STATUS);
			}
			return new Status(severity, UpdateUI.PLUGIN_ID, IStatus.OK, message, null);
		}
		return SiteManager.getLocalSite().getFeatureStatus(feature);
	}

	private int getStatusCode(IFeature feature, IStatus status) {
		int code = status.getCode();
		if (code == IFeature.STATUS_UNHAPPY) {
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				for (int i = 0; i < children.length; i++) {
					IStatus child = children[i];
					if (child.isMultiStatus()
						|| child.getCode() != IFeature.STATUS_DISABLED)
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
