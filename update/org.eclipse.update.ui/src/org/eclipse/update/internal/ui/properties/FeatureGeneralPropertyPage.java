/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.UpdateUIMessages;

public class FeatureGeneralPropertyPage
	extends PropertyPage
	implements IWorkbenchPropertyPage {
		
	public FeatureGeneralPropertyPage() {
		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent) {
		try {
			IFeatureAdapter adapter = (IFeatureAdapter) getElement();
			IFeature feature = adapter.getFeature(null);

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.verticalSpacing = 15;
			composite.setLayout(layout);
			
			addGeneralSection(feature, composite);	
			addSupportedPlatformsSection(feature, composite);
			addDescription(feature, composite);
			
			Dialog.applyDialogFont(parent);
			
			return composite;

		} catch (CoreException e) {
		}

		return null;
	}

	private void addGeneralSection(IFeature feature, Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		addField(composite, UpdateUIMessages.FeatureGeneralPropertyPage_name, feature.getLabel()); 
		addField(
			composite,
			UpdateUIMessages.FeatureGeneralPropertyPage_id, 
			feature.getVersionedIdentifier().getIdentifier());
		addField(
			composite,
			UpdateUIMessages.FeatureGeneralPropertyPage_version, 
			feature.getVersionedIdentifier().getVersion().toString());
		addField(composite, UpdateUIMessages.FeatureGeneralPropertyPage_provider, feature.getProvider()); 
		long size = feature.getInstallSize();
		if (size != ContentEntryModel.UNKNOWN_SIZE)
			addField(composite, UpdateUIMessages.FeatureGeneralPropertyPage_size, new Long(size).toString() + " " + UpdateUIMessages.FeatureGeneralPropertyPage_Kilobytes); 

	}
	
	private void addSupportedPlatformsSection(IFeature feature, Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(UpdateUIMessages.FeatureGeneralPropertyPage_platforms); 

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText(UpdateUIMessages.FeatureGeneralPropertyPage_os + extractValue(feature.getOS())); 

		label = new Label(group, SWT.NONE);
		label.setText(UpdateUIMessages.FeatureGeneralPropertyPage_ws + extractValue(feature.getWS())); 

		label = new Label(group, SWT.NONE);
		label.setText(UpdateUIMessages.FeatureGeneralPropertyPage_arch + extractValue(feature.getOSArch())); 

		label = new Label(group, SWT.NONE);
		label.setText(UpdateUIMessages.FeatureGeneralPropertyPage_nl + extractValue(feature.getNL())); 
	}

	private void addField(Composite parent, String property, String value) {

		if (value != null && value.length() > 0) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(property);

			label = new Label(parent, SWT.NONE);
			label.setText(getEscapedString(value));
		}

	}
	private String extractValue(String value) {
		if (value == null || value.equals("*")) //$NON-NLS-1$
			return UpdateUIMessages.FeatureGeneralPropertyPage_all; 
		return value;
	}

	private void addDescription(IFeature feature, Composite parent) {
		IURLEntry description = feature.getDescription();
		if (description != null) {
			String annotation = description.getAnnotation();
			if (annotation != null && annotation.length() > 0) {
				Group group = new Group(parent, SWT.NONE);
				group.setText(UpdateUIMessages.FeatureGeneralPropertyPage_desc); 
				group.setLayout(new GridLayout());
				group.setLayoutData(new GridData(GridData.FILL_BOTH));

				Text text = new Text(group, SWT.MULTI | SWT.WRAP);
				GridData gd = new GridData(GridData.FILL_BOTH);
				gd.heightHint = 200;
				gd.widthHint = 350;
				text.setEditable(false);
				text.setText(annotation);
				text.setLayoutData(gd);
			}
		}
	}
	
	private String getEscapedString(String value) {
		return value.replaceAll("&", "&&");
	}
}
