/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.preferences;


import java.util.Arrays;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.ui.internal.model.AntUIImages;
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.ant.ui.internal.model.IAntUIHelpContextIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page for setting global Ant user properties.
 * All properties specified here will be set as user properties on the 
 * project for any Ant build
 */
public class AntPropertiesPage implements IAntBlockContainer {
	
	private AntPropertiesBlock antPropertiesBlock= new AntPropertiesBlock(this);
	private AntRuntimePreferencePage preferencePage;
	
	/**
	 * Creates an instance.
	 */
	public AntPropertiesPage(AntRuntimePreferencePage preferencePage) {
		this.preferencePage= preferencePage;
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	protected TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntPropertiesPage.title")); //$NON-NLS-1$
		item.setImage(AntUIImages.getImage(IAntUIConstants.IMG_PROPERTY));
		item.setData(this);
		item.setControl(createContents(folder));
		return item;
	}
	
	protected Composite createContents(Composite parent) {
		Font font = parent.getFont();
		
		Composite top = new Composite(parent, SWT.NONE);
		top.setFont(font);
		WorkbenchHelp.setHelp(top, IAntUIHelpContextIds.ANT_PROPERTIES_PAGE);
		GridLayout layout = new GridLayout();
		layout.numColumns= 2;
		top.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		top.setLayoutData(gridData); 
				
		antPropertiesBlock.createControl(top, AntPreferencesMessages.getString("AntPropertiesPage.&Global_properties__1"), AntPreferencesMessages.getString("AntPropertiesPage.Glo&bal_property_files__2")); //$NON-NLS-1$ //$NON-NLS-2$
		
		return top;
	}
	
	/**
	 * Sets the contents of the tables on this page.
	 */
	protected void initialize() {
		antPropertiesBlock.setPropertiesInput(AntCorePlugin.getPlugin().getPreferences().getCustomProperties());
		antPropertiesBlock.setPropertyFilesInput(AntCorePlugin.getPlugin().getPreferences().getCustomPropertyFiles());
		antPropertiesBlock.update();
	}
	
	protected void performDefaults() {
		antPropertiesBlock.populatePropertyViewer(null);
		antPropertiesBlock.setPropertyFilesInput(new String[0]);
		antPropertiesBlock.update();
	}
	
	/**
	 * Returns the specified property files
	 * 
	 * @return String[]
	 */
	protected String[] getPropertyFiles() {
		Object[] elements = antPropertiesBlock.getPropertyFiles();
		String[] files= new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			files[i] = (String)elements[i];
		}
		return files;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		preferencePage.setMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		preferencePage.setErrorMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public Button createPushButton(Composite parent, String buttonText) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(buttonText);
		preferencePage.setButtonLayoutData(button);
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#update()
	 */
	public void update() {
	}
	
	protected List getProperties() {
		return Arrays.asList(antPropertiesBlock.getProperties());
	}
}