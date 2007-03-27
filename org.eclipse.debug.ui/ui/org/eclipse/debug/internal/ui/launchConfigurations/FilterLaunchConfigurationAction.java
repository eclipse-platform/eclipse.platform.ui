/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * provides the implementation of the filtering action for the launch configuration view within the 
 * Launch Configuration Dialog
 * @since 3.2
 */
public class FilterLaunchConfigurationAction extends Action {

	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_FILTER_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_FILTER_ACTION"; //$NON-NLS-1$
	
	/**
	 * the menu for this drop down style action
	 */
	private FilterDropDownMenuCreator fMenuCreator;
	
	/**
	 * Constructor
	 * @param text the text for the action
	 * @param viewer the viewer the action acts upon
	 * @param mode the mode
	 */
	public FilterLaunchConfigurationAction() {
		super(LaunchConfigurationsMessages.FilterLaunchConfigurationAction_0, IAction.AS_DROP_DOWN_MENU);
		fMenuCreator = new FilterDropDownMenuCreator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		SWTFactory.showPreferencePage("org.eclipse.debug.ui.LaunchConfigurations"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getDescription()
	 */
	public String getDescription() {
		return LaunchConfigurationsMessages.LaunchConfigurationsDialog_4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_FILTER_CONFIGS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_FILTER_CONFIGS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getMenuCreator()
	 */
	public IMenuCreator getMenuCreator() {
		return fMenuCreator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	public String getToolTipText() {
		return LaunchConfigurationsMessages.LaunchConfigurationsDialog_4;
	}
}
