/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.model.AdaptableList;

/**
 * Provides a status handler to prompt the user to delete any launch configurations
 * for the associated project that is about to be deleted, for more information
 * see <code>LaunchManager#preDelete(IProject)</code>.
 * 
 * @since 3.2
 *
 */
public class DeleteLaunchConfigurationStatusHandler implements IStatusHandler {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		String pref = DebugUIPlugin.getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_DELETE_CONFIGS_ON_PROJECT_DELETE);
		if(source instanceof IProject[]) {
			IProject[] projects = (IProject[])source;
			ArrayList configs =  new ArrayList();
			ArrayList elements = null;
			HashMap map = new HashMap();
			for (int i = 0; i < projects.length; i++) {
				elements = collectAssociatedLaunches(projects[i]);
				if(!elements.isEmpty()) {
					map.put(projects[i], elements);
					configs.addAll(elements);
				}
			}
			if(configs.size() > 0) {
				if(pref.equals(MessageDialogWithToggle.PROMPT)) {
					DeleteAssociatedLaunchConfigurationsDialog lsd = new DeleteAssociatedLaunchConfigurationsDialog(DebugUIPlugin.getShell(),
							new AdaptableList(configs),
							LaunchConfigurationsMessages.DeleteLaunchConfigurations_0, 
							map);
					lsd.setInitialSelections(configs.toArray());
					lsd.setTitle(LaunchConfigurationsMessages.DeleteLaunchConfigurations_1);
					if(lsd.open() == IDialogConstants.OK_ID) {
						doDelete(lsd.getResult());
					}
				}
				else if(pref.equals(MessageDialogWithToggle.ALWAYS)){
					doDelete(configs.toArray());
				}
			}
		}
		return null;
	}

	/**
	 * Gets the launch configuration associated with the specified project.
	 * This method relies on the resource mapping existing, if no such mapping 
	 * exists the launch configuration is ignored.
	 * 
	 * @param project the project to collect launch configurations for
	 * @return the list of associated launch configurations
	 */
	private ArrayList collectAssociatedLaunches(IProject project) {
		ArrayList list = new ArrayList();
		try { 
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
			IResource[] resources = null;
			for(int i = 0; i < configs.length; i++) {
				if(configs[i].isLocal()) {
					resources = configs[i].getMappedResources();
					if(resources != null) {
						for(int j = 0; j < resources.length; j++){
							if(resources[j].equals(project) || project.getFullPath().isPrefixOf(resources[j].getFullPath())) {
								list.add(configs[i]);
							}
						}
					}
				}
			}
		}
		catch (CoreException e) {
		    DebugUIPlugin.log(e);
        }
		return list;
	}
	
	/**
	 * Actually performs the deletion of the launch configurations.
	 * @param launches the launch configurations to delete
	 */
	private void doDelete(Object[] launches) {
		try {
			for(int i = 0; i < launches.length; i++) {
				((ILaunchConfiguration)launches[i]).delete();
			}
		}
		catch (CoreException e) {
            DebugUIPlugin.log(e);
		}
	}
	
}
