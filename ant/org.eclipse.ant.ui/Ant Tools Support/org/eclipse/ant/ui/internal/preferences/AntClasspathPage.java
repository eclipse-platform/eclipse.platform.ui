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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
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
 * Sub-page that allows the user to enter custom classpaths
 * to be used when running Ant build files.
 */
public class AntClasspathPage implements IAntBlockContainer {

	private AntClasspathBlock antClasspathBlock= new AntClasspathBlock();
	private AntRuntimePreferencePage preferencePage;
	
	/**
	 * Creates an instance.
	 */
	public AntClasspathPage(AntRuntimePreferencePage preferencePage) {
		this.preferencePage = preferencePage;
	}
	
	/**
	 * Returns the specified user classpath URLs
	 * 
	 * @return List
	 */
	protected List getUserURLs() {
		return antClasspathBlock.getUserURLs();
	}
	
	/**
	 * Returns the currently listed objects in the table.
	 */
	protected List getAntURLs() {
		return antClasspathBlock.getAntURLs();
	}
	
	protected String getAntHome() {
		return antClasspathBlock.getAntHome();
	}
	
	/**
	 * Sets the contents of the tables on this page.
	 */
	protected void initialize() {
		
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		antClasspathBlock.setAntTableInput(prefs.getAntURLs());
		antClasspathBlock.setUserTableInput(Arrays.asList(prefs.getCustomURLs()));
		antClasspathBlock.setEnabled(true);
		String antHomeString= prefs.getAntHome();
		if (antHomeString != null && antHomeString.length() == 0) {
			antHomeString= null;
		}
		antClasspathBlock.initializeAntHome(antHomeString);
		
		preferencePage.setErrorMessage(null);
		preferencePage.setValid(true);
	}
	
	protected void performDefaults() {
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		antClasspathBlock.setAntTableInput(Arrays.asList(prefs.getDefaultAntURLs()));
		antClasspathBlock.setUserTableInput(new ArrayList(0));
		antClasspathBlock.initializeAntHome(null);
		antClasspathBlock.setEnabled(true);
		update();
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	protected TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntClasspathPage.title")); //$NON-NLS-1$;
		item.setImage(antClasspathBlock.getClasspathImage());
		item.setData(this);
		item.setControl(createContents(folder));
		return item;
	}
	
	/**
	 * Creates this page's controls
	 */
	protected Composite createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IAntUIHelpContextIds.ANT_CLASSPATH_PAGE);
		Font font = parent.getFont();
		
		Composite top = new Composite(parent, SWT.NONE);
		top.setFont(font);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);

		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		antClasspathBlock.setContainer(this);
		antClasspathBlock.createContents(top);
		
		return top;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#update()
	 */
	public void update() {
		setMessage(null);
		setErrorMessage(null);
		boolean valid= true;
		if (antClasspathBlock.isAntHomeEnabled()) {
			valid= antClasspathBlock.validateAntHome();
		}
		preferencePage.setValid(valid);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		preferencePage.setMessage(message);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		preferencePage.setErrorMessage(message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.IAntClasspathBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public Button createPushButton(Composite parent, String buttonText) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(buttonText);
		preferencePage.setButtonLayoutData(button);
		return button;
	}
}
