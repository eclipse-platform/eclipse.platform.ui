/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.properties;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @see PropertyPage
 */
public class ConfiguredSitePropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public ConfiguredSitePropertyPage() {
		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent)  {
		IConfiguredSiteAdapter adapter = (IConfiguredSiteAdapter)getElement();
		IConfiguredSite csite = adapter.getConfiguredSite();
		Composite composite = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		addProperty(composite, UpdateUI.getString("ConfiguredSitePropertyPage.path"), csite.getSite().getURL().toString()); //$NON-NLS-1$
		addProperty(composite, UpdateUI.getString("ConfiguredSitePropertyPage.type"), getLocationType(csite)); //$NON-NLS-1$
		addProperty(composite, UpdateUI.getString("ConfiguredSitePropertyPage.enabled"), csite.isEnabled()?UpdateUI.getString("ConfiguredSitePropertyPage.yes"):UpdateUI.getString("ConfiguredSitePropertyPage.no")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return composite;
	}
	
	private String getLocationType(IConfiguredSite csite) {
		if (csite.isExtensionSite())
			return UpdateUI.getString("ConfiguredSitePropertyPage.extension"); //$NON-NLS-1$
		if (csite.isProductSite())
			return UpdateUI.getString("ConfiguredSitePropertyPage.product"); //$NON-NLS-1$
		return UpdateUI.getString("ConfiguredSitePropertyPage.unknown"); //$NON-NLS-1$
	}
	
	private void addProperty(Composite parent, String key, String value) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(key);
		
		label = new Label(parent, SWT.NULL);
		label.setText(value);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
	}
}
