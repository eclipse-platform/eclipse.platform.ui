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
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard to create/launch a config
 * 
 * @since 3.1
 */
public class LaunchWizard extends Wizard {
	
	private ILaunchGroup fGroup;
	private ILaunchConfigurationType fType;
	private ILaunchConfiguration fConfiguration;
	private IStructuredSelection fInitialSelection;
	
	/**
	 * Creates a new launch wizard on the given launch group.
	 * 
	 * @param group launch group
	 */
	public LaunchWizard(ILaunchGroup group) {
	    this(group, null);
	}	
	
	/**
	 * Creates a new launch wizard on the given launch group, that opens on the
	 * specified selection.
	 * 
	 * @param group launch group
	 * @param selection selectino of launch configs, config types, or <code>null</code>
	 */
	public LaunchWizard(ILaunchGroup group, IStructuredSelection selection) {
		super();
		fGroup = group;
		fInitialSelection = selection;
		addPage(new ApplicationPage(group.getLabel(), group.getBannerImageDescriptor()));
		addPage(new ConfigurationPage(group.getLabel()));
		setWindowTitle(DebugUIPlugin.removeAccelerators(group.getLabel()));
		setNeedsProgressMonitor(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		getConfigurationPage().saveIfNeeded();
		Runnable r = new Runnable() {
            public void run() {
                DebugUITools.launch(getLaunchConfiguration(), getMode());
            }
        };
        DebugUIPlugin.getStandardDisplay().asyncExec(r); // launch after closing
		return true;
	}
	
	/**
	 * Returns the page used to edit a launch configuration
	 * 
	 * @return the wizard page used to edit a launch configuration
	 */
	protected ConfigurationPage getConfigurationPage() {
	    return (ConfigurationPage) getPages()[1];
	}
	
	/**
	 * Returns the launch group this wizard was opened on.
	 * 
	 * @return the launch group this wizard was opened on
	 */
	protected ILaunchGroup getLaunchGroup() {
	    return fGroup;
	}
	
	/**
	 * Returns the launch mode this wizard was opened in
	 * 
	 * @return the launch mode this wizard was opened in
	 */
	protected String getMode() {
	    return getLaunchGroup().getMode();
	}	

    /**
     * Sets the current launch configuration
     * 
     * @param configuration launch configuration
     */
    protected void setLaunchConfiguration(ILaunchConfiguration configuration) {
        fConfiguration = configuration;
    }
    
    /**
     * Returns the current launch configuration, or <code>null</code>
     * if none.
     * 
     * @return the current launch configuration, or <code>null</code>
     */
    protected ILaunchConfiguration getLaunchConfiguration() {
        return fConfiguration;
    }
    
    /**
     * Sets the current launch configuration type, possibly <code>null</code>
     * 
     * @param type launch configuration type or <code>null</code>
     */
    protected void setLaunchConfigurationType(ILaunchConfigurationType type) {
        fType = type;
    }
    
    /**
     * Returns the current launch configuration type, or <code>null</code>
     * if none.
     * 
     * @return the current launch configuration type, or <code>null</code>
     */
    protected ILaunchConfigurationType getLaunchConfigurationType() {
        return fType;
    }    
    
    /**
     * Returns the items that should be selected on opening, or <code>null</code>
     * 
     * @return the items that should be selected on opening, or <code>null</code>
     */
    protected IStructuredSelection getInitialSelection() {
        return fInitialSelection;
    }
    
    public boolean performCancel() {
        return getConfigurationPage().performCancel();
    }
}
