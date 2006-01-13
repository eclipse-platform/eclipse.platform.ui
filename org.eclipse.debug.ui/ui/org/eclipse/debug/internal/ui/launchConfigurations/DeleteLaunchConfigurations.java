/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * Provides a status handler to prompt the user to delete any launch configurations
 * for the associated project that is about to be deleted, for more information
 * see <code>LaunchManager#preDelete(IProject)</code>.
 * 
 * @since 3.2
 *
 */
public class DeleteLaunchConfigurations implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		String pref = DebugUIPlugin.getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_DELETE_CONFIGS_ON_PROJECT_DELETE);
		if(source instanceof IProject) {
			IProject project = (IProject)source;
			ILaunchConfiguration[] configs = collectAssociatedLaunches(project);
			if(configs.length > 0) {
				if(pref.equals(MessageDialogWithToggle.PROMPT)) {
					ListSelectionDialog lsd = new ListSelectionDialog(DebugUIPlugin.getShell(),
							new AdaptableList(configs),
							new WorkbenchContentProvider(),
							DebugUITools.newDebugModelPresentation(),
							LaunchConfigurationsMessages.DeleteLaunchConfigurations_0);
					lsd.setInitialSelections(configs);
					lsd.setTitle(LaunchConfigurationsMessages.DeleteLaunchConfigurations_1);
					if(lsd.open() == IDialogConstants.OK_ID) {
						doDelete(lsd.getResult());
					}
				}
				else if(pref.equals(MessageDialogWithToggle.ALWAYS)){
					doDelete(collectAssociatedLaunches(project));
				}
			}
		}
		return null;
	}

	/**
	 * Gets to launch configuration associated with the specified project.
	 * This method relies on the resource mapping existing, if no such mapping 
	 * exists the launch configuration is ignored.
	 * 
	 * @param project the project to collect launch configurations for
	 * @return the list of associated launch configurations
	 */
	private ILaunchConfiguration[] collectAssociatedLaunches(IProject project) {
		ArrayList list = new ArrayList();
		try { 
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
			IResource[] resources = null;
			for(int i = 0; i < configs.length; i++) {
				resources = configs[i].getMappedResources();
				if(resources != null) {
					for(int j = 0; j < resources.length; j++){
						if(resources[j].equals(project)) {
							list.add(configs[i]);
						}
					}
				}
			}
		}
		catch (CoreException e) {e.printStackTrace();}
		return (ILaunchConfiguration[])list.toArray(new ILaunchConfiguration[list.size()]);
	}
	
	/**
	 * Actually performs the deletion of the launch configurations.
	 * @param launch the launch configurations to delete
	 */
	private void doDelete(Object[] launches) {
		try {
			for(int i = 0; i < launches.length; i++) {
				((ILaunchConfiguration)launches[i]).delete();
			}
		}
		catch (CoreException e) {e.printStackTrace();}
	}
	
}
