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

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.UpdateUIMessages;

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
		addProperty(composite, UpdateUIMessages.ConfiguredSitePropertyPage_path, csite.getSite().getURL().toString()); 
		addProperty(composite, UpdateUIMessages.ConfiguredSitePropertyPage_type, getLocationType(csite)); 
		addProperty(composite, UpdateUIMessages.ConfiguredSitePropertyPage_enabled, csite.isEnabled()?UpdateUIMessages.ConfiguredSitePropertyPage_yes:UpdateUIMessages.ConfiguredSitePropertyPage_no); 
		return composite;
	}
	
	private String getLocationType(IConfiguredSite csite) {
		if (csite.isExtensionSite())
			return UpdateUIMessages.ConfiguredSitePropertyPage_extension; 
		if (csite.isProductSite())
			return UpdateUIMessages.ConfiguredSitePropertyPage_product; 
		return UpdateUIMessages.ConfiguredSitePropertyPage_unknown; 
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
