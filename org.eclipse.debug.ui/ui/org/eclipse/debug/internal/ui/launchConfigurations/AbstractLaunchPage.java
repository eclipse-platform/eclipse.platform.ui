/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;

/**
 * AbstractLaunchPage
 */
public abstract class AbstractLaunchPage extends WizardPage {
    
    /**
     * Contructs a page with the given name, title, and banner image
     * @param pageName page name
     * @param title title or <code>null</code>
     * @param image banner image or <code>null</code>
     */
    public AbstractLaunchPage(String pageName, String title, ImageDescriptor image) {
        super(pageName, title, image);
    }
    
    /**
     * Returns the launch group this page was opened on.
     * 
     * @return the launch group this page was opened on
     */
    protected ILaunchGroup getLaunchGroup() {
        return getLaunchWizard().getLaunchGroup();
    }
    
	/**
	 * Returns the launch mode this page was opened in
	 * 
	 * @return the launch mode this page was opened in
	 */
	protected String getMode() {
	    return getLaunchGroup().getMode();
	}
	
	/**
	 * Returns the current launch configuration or <code>null</code> if none.
	 * 
	 * @return the current launch configuration or <code>null</code>
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
	    return getLaunchWizard().getLaunchConfiguration();
	}
	
	/**
	 * Sets the current launch configuration, possibly <code>null</code>.
	 * 
	 * @param configuration the current launch configuration or <code>null</code>
	 */
	protected void setLaunchConfiguration(ILaunchConfiguration configuration) {
	    getLaunchWizard().setLaunchConfiguration(configuration);
	}	
    
	/**
	 * Sets the current launch configuration type, possibly <code>null</code>.
	 * 
	 * @param configuration the current launch configuration type or <code>null</code>
	 */	
	protected void setLaunchConfigurationType(ILaunchConfigurationType type) {
	    getLaunchWizard().setLaunchConfigurationType(type);
	}

	/**
	 * Returns the current launch configuration type or <code>null</code> if none.
	 * 
	 * @return the current launch configuration type or <code>null</code>
	 */
	protected ILaunchConfigurationType getLaunchConfigurationType() {
	    return getLaunchWizard().getLaunchConfigurationType();
	}
	
	/**
	 * Returns the page used to edit configs in this wizard
	 * 
	 * @return the page used to edit configs in this wizard
	 */
	protected ConfigurationPage getConfigurationPage() {
	    return getLaunchWizard().getConfigurationPage();
	}
	
	/**
	 * Returns the launch wizard
	 * 
	 * @return the launch wizard
	 */
	protected LaunchWizard getLaunchWizard() {
	    return (LaunchWizard)getWizard();
	}
	
	/**
	 * Returns the description of the given configuration type
	 * in the current mode.
	 * 
	 * @param configType the config type
	 * @return the description of the given configuration type or <code>null</code>
	 */
	protected String getDescription(ILaunchConfigurationType configType) {
		String description = null;
		if(configType != null) {
			description = LaunchConfigurationPresentationManager.getDefault().getDescription(configType, getMode());
		}	
		if (description == null)
			description = ""; //$NON-NLS-1$
		return description;
	}		
}
