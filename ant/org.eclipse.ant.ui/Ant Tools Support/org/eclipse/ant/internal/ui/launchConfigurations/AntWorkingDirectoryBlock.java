package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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

	public void setDefaultWorkingDirPath(String defaultWorkingDirPath) {
		fDefaultWorkingDirPath = defaultWorkingDirPath;
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
		fLocalDirButton.setSelection(true);
		fWorkspaceDirButton.setSelection(false);		
	}
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		setLaunchConfiguration(configuration);
		try {			
			String wd = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null); //$NON-NLS-1$
			fWorkspaceDirText.setText(""); //$NON-NLS-1$
			fWorkingDirText.setText(""); //$NON-NLS-1$
			if (wd == null) {
				try {
					fDefaultWorkingDirPath= ExternalToolsUtil.getLocation(configuration).removeLastSegments(1).toOSString();
				} catch (CoreException e) {
					AntUIPlugin.log(e);
				}
				fUseDefaultWorkingDirButton.setSelection(true);
			} else {
				IPath path = new Path(wd);
				if (path.isAbsolute()) {
					fWorkingDirText.setText(wd);
					fLocalDirButton.setSelection(true);
					fWorkspaceDirButton.setSelection(false);
				} else {
					fWorkspaceDirText.setText(wd);
					fWorkspaceDirButton.setSelection(true);
					fLocalDirButton.setSelection(false);
				}
				fUseDefaultWorkingDirButton.setSelection(false);
			}
			handleUseDefaultWorkingDirButtonSelected();
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.getString("JavaArgumentsTab.Exception_occurred_reading_configuration___15") + e.getStatus().getMessage()); //$NON-NLS-1$
			JDIDebugUIPlugin.log(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String wd = null;
		if (isLocalWorkingDirectory()) {
			wd = getAttributeValueFrom(fWorkingDirText);
		} else {
			IPath path = new Path(fWorkspaceDirText.getText());
			path = path.makeRelative();
			wd = path.toString();
		} 
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, wd);
	}
}
