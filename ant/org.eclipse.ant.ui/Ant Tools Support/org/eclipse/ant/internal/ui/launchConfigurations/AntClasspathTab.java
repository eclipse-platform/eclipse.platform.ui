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
package org.eclipse.ant.internal.ui.launchConfigurations;


import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.internal.ui.model.AntUtil;
import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.preferences.AntClasspathBlock;
import org.eclipse.ant.internal.ui.preferences.ClasspathModel;
import org.eclipse.ant.internal.ui.preferences.IAntBlockContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntClasspathTab extends AbstractLaunchConfigurationTab implements IAntBlockContainer {

	private ClasspathModel model;
	private AntClasspathBlock antClasspathBlock= new AntClasspathBlock(true);
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font= parent.getFont();
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.setFont(font);

		setControl(top);
		WorkbenchHelp.setHelp(top, IAntUIHelpContextIds.ANT_CLASSPATH_TAB);
		antClasspathBlock.setContainer(this);
		antClasspathBlock.createContents(top);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		String antHomeString= null;
		String defaultAntHome= AntCorePlugin.getPlugin().getPreferences().getAntHome();
		try {
			antHomeString= configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_HOME, defaultAntHome);
		} catch (CoreException e) {
		}
		
		createClasspathModel(configuration, !antHomeString.equals(defaultAntHome));
		antClasspathBlock.setInput(model);
		
		antClasspathBlock.initializeAntHome(antHomeString, model.getAntHomeEntry() != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		
		String dfltAntHome= AntCorePlugin.getPlugin().getPreferences().getDefaultAntHome();
		boolean defaultAntHome= antClasspathBlock.getAntHome().equals(dfltAntHome);
		if (defaultAntHome) {
			configuration.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_HOME, (String)null);
		} else {
			configuration.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_HOME, antClasspathBlock.getAntHome());
		}
		
		String classpath= model.serializeClasspath(defaultAntHome);
		if (classpath.equals(AntUtil.ANT_GLOBAL_CLASSPATH_PLACEHOLDER + AntUtil.ATTRIBUTE_SEPARATOR + AntUtil.ANT_GLOBAL_USER_CLASSPATH_PLACEHOLDER)) {
			classpath= null;
		}
		
		if (classpath != null) {
			configuration.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, classpath);
		} else {
			configuration.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String)null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("AntClasspathTab.Classpath_6"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return antClasspathBlock.getClasspathImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (antClasspathBlock.isValidated()) {
			return getErrorMessage() == null;
		}
		setErrorMessage(null);
		setMessage(null);
		boolean valid= true;
		if (antClasspathBlock.isAntHomeEnabled()) {
			valid= antClasspathBlock.validateAntHome(); 
		}
		if (valid){
			valid= antClasspathBlock.validateToolsJAR();
		}
		if (valid) {
			String vmTypeID= null;
			try {
				vmTypeID = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			} catch (CoreException ce) {		
			}
			
			valid= antClasspathBlock.validateXerces(vmTypeID == null);
			antClasspathBlock.setValidated();
		}

		if (valid) {
			return super.isValid(launchConfig);
		} else {
			return valid;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		super.setMessage(message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		super.setErrorMessage(message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IAntBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public Button createPushButton(Composite parent, String buttonText) {
		return super.createPushButton(parent, buttonText, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IAntBlockContainer#update()
	 */
	public void update() {
		updateTargetsTab();
		updateLaunchConfigurationDialog();
	}

	private void updateTargetsTab() {
		//the classpath has changed...set the targets tab to 
		//need to be recomputed
		ILaunchConfigurationTab[] tabs=  getLaunchConfigurationDialog().getTabs();
		for (int i = 0; i < tabs.length; i++) {
			ILaunchConfigurationTab tab = tabs[i];
			if (tab instanceof AntTargetsTab) {
				((AntTargetsTab)tab).setDirty(true);
				break;
			}
		}
	}
	
	private void createClasspathModel(ILaunchConfiguration configuration, boolean customAntHome) {
		String classpathString= null;
		try {
			classpathString = configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String)null);
		} catch (CoreException e) {
		}
		if (classpathString == null) {
			model= new ClasspathModel();
			AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
			model.setAntHomeEntries(prefs.getAntHomeClasspathEntries());
			model.setGlobalEntries(prefs.getAdditionalClasspathEntries());
		} else {
			model= new ClasspathModel(classpathString, customAntHome);
		}
	}
}