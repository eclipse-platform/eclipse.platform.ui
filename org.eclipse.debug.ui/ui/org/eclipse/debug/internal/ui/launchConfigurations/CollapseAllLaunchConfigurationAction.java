/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Provides a collapse all button for the launch configuration viewer toolbar
 * @since 3.2
 */
public class CollapseAllLaunchConfigurationAction extends Action {

	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_COLLAPSEALL_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_COLLAPSEALL_ACTION"; //$NON-NLS-1$
	
	/**
	 * the viewer to perform the collapse all on 
	 */
	private TreeViewer fViewer = null;
	
	/**
	 * Constructor
	 * @param viewer the viewer to perform the collapse all on
	 */
	public CollapseAllLaunchConfigurationAction(TreeViewer viewer) {
		fViewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_COLLAPSE_ALL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getText()
	 */
	public String getText() {
		return LaunchConfigurationsMessages.CollapseAllLaunchConfigurationAction_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	public String getToolTipText() {
		return LaunchConfigurationsMessages.CollapseAllLaunchConfigurationAction_1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_COLLAPSE_ALL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		fViewer.collapseAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getDescription()
	 */
	public String getDescription() {
		return LaunchConfigurationsMessages.CollapseAllLaunchConfigurationAction_2;
	}	
}
