/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.properties;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.operations.*;

/**
 * @see PropertyPage
 */
public class FeatureStatusPropertyPage
	extends PropertyPage
	implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public FeatureStatusPropertyPage() {
		noDefaultAndApplyButton();
	}

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
				message.setText(UpdateUIMessages.FeatureStatusPropertyPage_pendingChanges); 
				return composite;
			}
			
			IStatus status = getStatus(feature);
			int severity = status.getSeverity();
			if (severity == IStatus.ERROR
				&& getStatusCode(feature, status) == IFeature.STATUS_HAPPY) {
				severity = IStatus.OK;
				message.setText(UpdateUIMessages.FeatureStatusPropertyPage_goodConfiguration); 
			} else {
				message.setText(status.getMessage());
			}
			if (severity != IStatus.OK && status.isMultiStatus()) {
				String reason = getReason(status);
				if (reason.length() > 0) {
					Composite comp = new Composite(composite, SWT.NONE);
					comp.setLayout(new GridLayout());
					gd = new GridData(GridData.FILL_BOTH);
					comp.setLayoutData(gd);

					Label label = new Label(comp, SWT.NONE);
					label.setText(UpdateUIMessages.FeatureStatusPropertyPage_reason); 

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
						+ System.getProperty("line.separator") //$NON-NLS-1$
						+ System.getProperty("line.separator")); //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}

	private IStatus getStatus(IFeature feature) throws CoreException {
		if (feature instanceof MissingFeature) {
			int severity;
			String message = ""; //$NON-NLS-1$
			if (((MissingFeature) feature).isOptional()) {
				severity = IStatus.OK;
				message = UpdateUIMessages.FeatureStatusPropertyPage_missingOptional; 
			} else {
				severity = IStatus.ERROR;
				message = UpdateUIMessages.FeatureStatusPropertyPage_missing; 
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
				if (UpdateUtils.hasObsoletePatches(feature)) {
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
