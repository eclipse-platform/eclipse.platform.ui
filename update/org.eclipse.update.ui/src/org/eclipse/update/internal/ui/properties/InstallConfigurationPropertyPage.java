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
import org.eclipse.update.configurator.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @see PropertyPage
 */
public class InstallConfigurationPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public InstallConfigurationPropertyPage() {
		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent)  {
		Composite composite = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		addProperty(composite, UpdateUI.getString("ConfiguredSitePropertyPage.path"), ConfiguratorUtils.getInstallURL().toString()); //$NON-NLS-1$
		return composite;
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
