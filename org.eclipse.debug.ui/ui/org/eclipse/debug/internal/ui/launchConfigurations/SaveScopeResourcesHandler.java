/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Status handler to prompt for saving of resources prior to launching.
 * <p>
 * This class provides a behavior breaking function from 3.1. We now perform pre-launch saving for resources
 * scoped to the affected projects of the launch instead of all unsaved resources from within the current workspace.
 * </p>
 * <p>
 * The 'breaking' occurs as the saving is moved from <code>DebugUIPlugin</code> to the launch configuration delegate, which will require
 * implementors of <code>LaunchConfigurationDelegate</code> to incorporate the use of this status handler to perform any pre-launch saving. 
 * </p>
 * @since 3.2
 */
public class SaveScopeResourcesHandler implements IStatusHandler {
	
	/**
	 * The objects to save (if any)
	 */
	Object[] fSaves = null;
	
	/* (non-Javadoc)
	 * 
	 * Source object is an array - a launch configuration and an array of projects to save resources for.
	 * 
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		// retrieve config and projects
		ILaunchConfiguration config = null;
		IProject[] projects = null;
		if (source instanceof Object[]) {
			Object[] objects = (Object[]) source;
			if (objects.length == 2) {
				 config = (ILaunchConfiguration) objects[0];
				 projects = (IProject[]) objects[1];
			}
		}
        if (config != null) {
            if (DebugUITools.isPrivate(config)) {
                return Boolean.TRUE;
            }
        } 
        if (projects != null) {
            IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
            String save = store.getString(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
            boolean prompt = save.equals(MessageDialogWithToggle.PROMPT);
            if(prompt || save.equals(MessageDialogWithToggle.ALWAYS)) {
	            if(IDE.saveAllEditors(getScopedDirtyResources(projects), prompt)) {
	            	return Boolean.TRUE;
	            }
            }
            else {
            	return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } 
        else {
            boolean cancel = DebugUIPlugin.preLaunchSave();
            return Boolean.valueOf(cancel);
        }
    }
	
	/**
	 * 
	 * Builds the list of editors that apply to this build that need to be saved
	 * 
	 * @param projects the projects involved in this build, used to scope the searching process
	 * @return the list of dirty editors for this launch to save, never null
	 */
	protected IResource[] getScopedDirtyResources(IProject[] projects) {
		HashSet dirtyres = new HashSet();
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for(int l = 0; l < windows.length; l++) {
			IWorkbenchPage[] pages = windows[l].getPages();
			for(int i = 0; i < pages.length; i++) {
				IEditorPart[] eparts = pages[i].getDirtyEditors();
				for(int j = 0; j < eparts.length; j++) {
					IResource resource = (IResource)eparts[j].getEditorInput().getAdapter(IResource.class);
					if(resource != null) {
						for(int k = 0; k < projects.length; k++) {
							if(projects[k].equals(resource.getProject()) && !dirtyres.contains(resource)) {
								dirtyres.add(resource);
							}
						}
					}
				}
			}
		}
		return (IResource[])dirtyres.toArray(new IResource[dirtyres.size()]);
	}
}
