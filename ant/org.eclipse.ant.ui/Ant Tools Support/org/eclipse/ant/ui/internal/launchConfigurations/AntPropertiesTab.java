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
package org.eclipse.ant.ui.internal.launchConfigurations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.Property;
import org.eclipse.ant.ui.internal.model.AntUIImages;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.AntUtil;
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.ant.ui.internal.model.IAntUIHelpContextIds;
import org.eclipse.ant.ui.internal.preferences.AntPropertiesBlock;
import org.eclipse.ant.ui.internal.preferences.IAntBlockContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Tab for setting Ant user properties per launch configuration. All properties
 * specified here will be set as user properties on the project for the
 * specified Ant build
 */
public class AntPropertiesTab extends AbstractLaunchConfigurationTab implements IAntBlockContainer {
	
	private AntPropertiesBlock antPropertiesBlock= new AntPropertiesBlock(this);
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setFont(parent.getFont());
		setControl(top);
		WorkbenchHelp.setHelp(getControl(), IAntUIHelpContextIds.ANT_PROPERTIES_TAB);

		GridLayout layout = new GridLayout();
		layout.numColumns= 2;
		top.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		top.setLayoutData(gridData);
		antPropertiesBlock.createControl(top, AntLaunchConfigurationMessages.getString("AntPropertiesTab.&Properties__6"), AntLaunchConfigurationMessages.getString("AntPropertiesTab.Property_f&iles__7")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_PROPERTY);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("AntPropertiesTab.P&roperties_8"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		Map properties= null;
		try {
			properties= configuration.getAttribute(IAntUIConstants.ATTR_ANT_PROPERTIES, (Map)null);
		} catch (CoreException ce) {
			AntUIPlugin.log(AntLaunchConfigurationMessages.getString("AntPropertiesTab.Error_reading_configuration_9"), ce); //$NON-NLS-1$
		}
		
		String propertyFiles= null;
		try {
			propertyFiles= configuration.getAttribute(IAntUIConstants.ATTR_ANT_PROPERTY_FILES, (String)null);
		} catch (CoreException ce) {
			AntUIPlugin.log(AntLaunchConfigurationMessages.getString("AntPropertiesTab.Error_reading_configuration_9"), ce); //$NON-NLS-1$
		}
		
		antPropertiesBlock.populatePropertyViewer(properties);
		
		String[] files= AntUtil.parseString(propertyFiles, ","); //$NON-NLS-1$
		antPropertiesBlock.setPropertyFilesInput(files);
		
		antPropertiesBlock.update();
	}
	
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		
		Object[] items= antPropertiesBlock.getProperties();
		Map properties= null;
		if (items.length > 0) {
			properties= new HashMap(items.length);
			for (int i = 0; i < items.length; i++) {
				Property property = (Property)items[i];
				properties.put(property.getName(), property.getValue());
			}
		}
		
		configuration.setAttribute(IAntUIConstants.ATTR_ANT_PROPERTIES, properties);
		
		items= antPropertiesBlock.getPropertyFiles();
		String files= null;
		if (items.length > 0) {
			StringBuffer buff= new StringBuffer();
			for (int i = 0; i < items.length; i++) {
				String path = (String)items[i];
				buff.append(path);
				buff.append(',');
			}
			files= buff.toString();
		}
		
		configuration.setAttribute(IAntUIConstants.ATTR_ANT_PROPERTY_FILES, files);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public void setMessage(String message) {
		super.setMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public void setErrorMessage(String message) {
		super.setErrorMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public Button createPushButton(Composite parent, String buttonText) {
		return super.createPushButton(parent, buttonText, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.preferences.IAntBlockContainer#createPushButton(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public void update() {
		updateLaunchConfigurationDialog();
	}
}
