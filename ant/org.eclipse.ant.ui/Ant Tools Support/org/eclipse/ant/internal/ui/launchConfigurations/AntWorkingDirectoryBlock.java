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
package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;


public class AntWorkingDirectoryBlock extends WorkingDirectoryBlock {
	
	private String fDefaultWorkingDirPath;

	public String getDefaultWorkingDirPath() {
		return fDefaultWorkingDirPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock#setDefaultWorkingDir()
	 */
	protected void setDefaultWorkingDir() {
		if (fDefaultWorkingDirPath == null) {
			super.setDefaultWorkingDir();
			return;
		}
		fWorkingDirText.setText(fDefaultWorkingDirPath);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		setLaunchConfiguration(configuration);
		try {
			try {
				fDefaultWorkingDirPath= ExternalToolsUtil.getLocation(configuration).removeLastSegments(1).toOSString();
			} catch (CoreException e) {
				//no location
			}
			
			String wd = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null); //$NON-NLS-1$
			fWorkingDirText.setText(""); //$NON-NLS-1$
			if (wd == null || isSameAsDefault(wd)) {
				fUseDefaultWorkingDirButton.setSelection(true);
			} else {
				fWorkingDirText.setText(wd);
				fUseDefaultWorkingDirButton.setSelection(false);
			}
			handleUseDefaultWorkingDirButtonSelected();
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.getString("JavaArgumentsTab.Exception_occurred_reading_configuration___15") + e.getStatus().getMessage()); //$NON-NLS-1$
			JDIDebugUIPlugin.log(e);
		}
	}
	
	private boolean isSameAsDefault(String workingDir) {
		return workingDir == null || (workingDir.equals(fDefaultWorkingDirPath) || workingDir.equals(System.getProperty("user.dir"))); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String wd = getAttributeValueFrom(fWorkingDirText);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, wd);
	}
	
	public void setEnabled(boolean enabled) {
		fUseDefaultWorkingDirButton.setEnabled(enabled);
		boolean def = isDefaultWorkingDirectory();
		fUseDefaultWorkingDirButton.setSelection(def);
		enabled = enabled && !def;
		fWorkingDirText.setEnabled(enabled);
		fWorkspaceButton.setEnabled(enabled);
		fFileSystemButton.setEnabled(enabled);
		fVariablesButton.setEnabled(enabled);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		if (fUseDefaultWorkingDirButton.isEnabled()) { //is this block enabled
			return super.isValid(config);
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}
}
