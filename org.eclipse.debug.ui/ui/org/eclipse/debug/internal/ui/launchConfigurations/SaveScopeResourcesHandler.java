/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;

/**
 * Status handler to prompt for saving of resources prior to launching.
 * <p>
 * This class provides a behavior breaking function from 3.1. We now perform pre-launch saving for resources
 * scoped to the affected projects of the launch instead of all unsaved resources from within the current workspace.
 * </p>
 * <p>
 * The 'breaking' occurs as the saving is moved from <code>DebugUIPlugin</code> to the launch configuration delegate, which will require
 * implementors of <code>LaunchConfigurationDelegate</code> to incorporate the use of this status handler to perform any prelaunch saving. 
 * </p>
 * @since 3.2
 */
public class SaveScopeResourcesHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if(source instanceof IProject[]) {
			IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
			String save = store.getString(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
			doSave((IProject[])source, !save.equals(MessageDialogWithToggle.NEVER), save.equals(MessageDialogWithToggle.PROMPT));
		}
		else {
			DebugUIPlugin.preLaunchSave();
		}
		return Boolean.TRUE;
	}
	
	/**
	 * 
	 * Builds the list of editors that apply to this build that need to be saved
	 * 
	 * @param projects the projects involved in this build, used to scope the searching process
	 * @return the list of dirty editors for this launch to save, never null
	 */
	protected IEditorPart[] getScopedDirtyEditors(IProject[] projects) {
		List dirtyparts = new ArrayList();
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for(int l = 0; l < windows.length; l++) {
			IWorkbenchPage[] pages = windows[l].getPages();
			for(int i = 0; i < pages.length; i++) {
				IEditorPart[] eparts = pages[i].getDirtyEditors();
				for(int j = 0; j < eparts.length; j++) {
					IResource resource = (IResource)eparts[j].getEditorInput().getAdapter(IResource.class);
					if(resource != null) {
						for(int k = 0; k < projects.length; k++) {
							if(projects[k].equals(resource.getProject()) & !dirtyparts.contains(eparts[j])) {
								dirtyparts.add(eparts[j]);
							}
						}
					}
				}
			}
		}
		return (IEditorPart[])dirtyparts.toArray(new IEditorPart[dirtyparts.size()]);
	}
	
	/**
	 * 
	 * Performs the save of the editor parts returned by getScopedResources
	 * 
	 * @param projects the list of projects to confine the save to
	 * @return
	 */
	protected void doSave(IProject[] projects, boolean save, boolean prompt) {
		if(save) {
			IEditorPart[] editors = getScopedDirtyEditors(projects);
			Object[] saves;
			if(prompt && (editors.length > 0)) {
				ListSelectionDialog lsd = new ListSelectionDialog(DebugUIPlugin.getShell(),
						new AdaptableList(editors),
						new WorkbenchContentProvider(),
						new WorkbenchPartLabelProvider(),
						LaunchConfigurationsMessages.SaveScopeResourcesHandler_2);
				lsd.setInitialSelections(editors);
				lsd.setTitle(LaunchConfigurationsMessages.SaveScopeResourcesHandler_3);
				lsd.open();
				saves = lsd.getResult();
			}
			else {
				saves = editors;
			}
			if(saves != null) {
				for (int i = 0; i < saves.length; i++) {
					((IEditorPart)saves[i]).doSave(new NullProgressMonitor());
				}
			}
		}
	}
}