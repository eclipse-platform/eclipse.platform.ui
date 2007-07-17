/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.AbstractDebugListSelectionDialog;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * This class provides a dialog to present the user with a list of of viable launch options in the event 
 * the plug-in that provides either a launch option or a contributed launch delegate is no longer available.
 * The user can select one of the launch mode/option configuration from this dialog and repair the option 
 * configuration state of the the current launch configuration
 * 
 *  @since 3.3
 */
public class SelectLaunchModesDialog extends AbstractDebugListSelectionDialog{

	/**
	 * Builds labels for list control
	 */
	class OptionsLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {return null;}
		public String getText(Object element) {
			Set modes = (Set) element;
			List names = LaunchConfigurationPresentationManager.getDefault().getLaunchModeNames(modes);
			return names.toString();
		}
		public void addListener(ILabelProviderListener listener) {}
		public void dispose() {}
		public boolean isLabelProperty(Object element, String property) {return false;}
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	private List fValidModes = null;
	
	/**
	 * Constructor
	 * @param parentShell the parent shell
	 * @param mode the current mode context
	 * @param configuration the current launch configuration context
	 * 
	 * @throws CoreException
	 */
	public SelectLaunchModesDialog(Shell parentShell, String mode, ILaunchConfiguration configuration) throws CoreException {
		super(parentShell);
		super.setTitle(LaunchConfigurationsMessages.SelectLaunchOptionsDialog_3);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fValidModes = new ArrayList();
		Set modes = configuration.getType().getSupportedModeCombinations();
		Set modeset = null;
		for(Iterator iter = modes.iterator(); iter.hasNext();) {
			modeset = (Set) iter.next();
			if(modeset.contains(mode)) {
				fValidModes.add(modeset);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
	 */
	protected String getDialogSettingsId() {
		return IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCH_MODES_DIALOG"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getLabelProvider()
	 */
	protected IBaseLabelProvider getLabelProvider() {
		return new OptionsLabelProvider();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
	 */
	protected Object getViewerInput() {
		return fValidModes.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SELECT_LAUNCH_MODES_DIALOG;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
	 */
	protected String getViewerLabel() {
		return LaunchConfigurationsMessages.SelectLaunchOptionsDialog_4;
	}
}
