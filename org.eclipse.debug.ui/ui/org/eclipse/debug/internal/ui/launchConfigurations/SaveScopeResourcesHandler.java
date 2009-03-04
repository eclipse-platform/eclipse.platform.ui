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
import org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

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
	 * Opens a resizable dialog listing possible files to save, the user can select none, some or all of the files before pressing OK.
	 * @since 3.2
	 */
	class ScopedResourcesSelectionDialog extends AbstractDebugCheckboxSelectionDialog {

		private final String SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".SCOPED_SAVE_SELECTION_DIALOG"; //$NON-NLS-1$
		Button fSavePref;
		Object fInput;
		IStructuredContentProvider fContentProvider;
		ILabelProvider fLabelProvider;
		
		public ScopedResourcesSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider, ILabelProvider labelProvider) {
			super(parentShell);
			fInput = input;
			fContentProvider = contentProvider;
			fLabelProvider = labelProvider;
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setShowSelectAllButtons(true);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getContentProvider()
		 */
		protected IContentProvider getContentProvider() {
			return fContentProvider;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getLabelProvider()
		 */
		protected IBaseLabelProvider getLabelProvider() {
			return fLabelProvider;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
		 */
		protected String getDialogSettingsId() {
			return SETTINGS_ID;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
		 */
		protected String getHelpContextId() {
			return IDebugHelpContextIds.SELECT_RESOURCES_TO_SAVE_DIALOG;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
		 */
		protected Object getViewerInput() {
			return fInput;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
		 */
		protected String getViewerLabel() {
			return LaunchConfigurationsMessages.SaveScopeResourcesHandler_2;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#addCustomFooterControls(org.eclipse.swt.widgets.Composite)
		 */
		protected void addCustomFooterControls(Composite parent) {
			super.addCustomFooterControls(parent);
			fSavePref = new Button(parent, SWT.CHECK);
			fSavePref.setText(LaunchConfigurationsMessages.SaveScopeResourcesHandler_1);
			fSavePref.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getCheckBoxTableViewer().setAllChecked(fSavePref.getSelection());
				}
			});
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#okPressed()
		 */
		protected void okPressed() {
			IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
			String val = (fSavePref.getSelection() ? MessageDialogWithToggle.ALWAYS : MessageDialogWithToggle.PROMPT);
			store.setValue(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, val);
			super.okPressed();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
		 */
		protected void addViewerListeners(StructuredViewer viewer) {
			// Override to remove listener that affects the ok button
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog#isValid()
		 */
		protected boolean isValid() {
			return true;
		}
	}
	
	/**
	 * The objects to save (if any)
	 */
	IResource[] fSaves = null;
	
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
            int ret = showSaveDialog(projects, !save.equals(MessageDialogWithToggle.NEVER), save.equals(MessageDialogWithToggle.PROMPT));
            if(ret == IDialogConstants.OK_ID) {
            	doSave();
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
							if(projects[k].equals(resource.getProject())) {
								dirtyres.add(resource);
							}
						}
					}
				}
			}
		}
		return (IResource[])dirtyres.toArray(new IResource[dirtyres.size()]);
	}
	
	/**
	 * Performs the save of the editor parts returned by getScopedResources
	 */
	protected void doSave() {
		if(fSaves != null) {
			IDE.saveAllEditors(fSaves, false);
		}
	} 
	
	/**
	 * show the save dialog with a list of editors to save (if any)
	 * The dialog is also not shown if the the preference for automatically saving dirty before launch is set to always
	 * @param projects the projects to consider for the save
	 * @param save if we should save
	 * @param prompt if we should prompt to save or do it automatically
	 * @return the dialog status, to be propagated back to the <code>handleStatus</code> method
	 */
	protected int showSaveDialog(IProject[] projects, boolean save, boolean prompt) {
		if(save) {
			IResource[] resources = getScopedDirtyResources(projects);
			if(prompt && (resources.length > 0)) {
				ScopedResourcesSelectionDialog lsd = new ScopedResourcesSelectionDialog(DebugUIPlugin.getShell(),
						new AdaptableList(resources),
						new WorkbenchContentProvider(),
						new WorkbenchLabelProvider());
				lsd.setInitialSelections(resources);
				lsd.setTitle(LaunchConfigurationsMessages.SaveScopeResourcesHandler_3);
				if(lsd.open() == IDialogConstants.CANCEL_ID) {
					return IDialogConstants.CANCEL_ID;
				}
				Object[] objs = lsd.getResult();
				fSaves = new IResource[objs.length];
				for (int i = 0; i < objs.length; i++) {
					fSaves[i] = (IResource) objs[i];
				}
			}
			else {
				fSaves = resources;
			}
		}
		return IDialogConstants.OK_ID;
	}
}
