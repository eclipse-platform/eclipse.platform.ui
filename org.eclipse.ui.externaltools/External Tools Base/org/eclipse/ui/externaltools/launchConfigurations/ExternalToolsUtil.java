package org.eclipse.ui.externaltools.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Utilities for external tool launch configurations.
 * <p>
 * This class it not intended to be instantiated.
 * </p>
 */
public class ExternalToolsUtil {
	
	/**
	 * Not to be instantiated.	 */
	private ExternalToolsUtil() {
	};
	
	/**
	 * If the give launch configuration specifies that dirty editors should be
	 * saved, the user is prompted to save any ditry editors.
	 * 
	 * @param configuration launch configuration
	 * @exception CoreException if unable to read the associated launch
	 * configuration attribute
	 */
	public static void saveDirtyEditors(ILaunchConfiguration configuration) throws CoreException {
		boolean save = configuration.getAttribute(IExternalToolConstants.ATTR_SAVE_DIRTY_EDITORS, false);
		if (save) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				IWorkbenchPage[] pages = windows[i].getPages();
				for (int j = 0; j < pages.length; j++) {
					pages[j].saveAllEditors(false);
				}
			}
		}
	}		
	
	/**
	 * Returns the resource associated with the selection or active editor in
	 * the active workbench window, or <code>null</code> if none.
	 * 
	 * @return returns the resource associated with the selection or active editor in
	 * the active workbench window, or <code>null</code> if none	 */
	public static IResource getActiveResource() {
		IWorkbenchWindow window = ExternalToolsPlugin.getActiveWorkbenchWindow();
		IResource selectedResource = null;
		if (window != null) {
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				Object result = ((IStructuredSelection)selection).getFirstElement();
				if (result instanceof IResource) {
					selectedResource = (IResource) result;
				} else if (result instanceof IAdaptable) {
					selectedResource = (IResource)((IAdaptable) result).getAdapter(IResource.class);
				}
			}
			
			if (selectedResource == null) {
				IWorkbenchPart activePart = window.getPartService().getActivePart();
				// If the active part is an editor, get the file resource used as input.
				if (activePart instanceof IEditorPart) {
					IEditorPart editorPart = (IEditorPart) activePart;
					IEditorInput input = editorPart.getEditorInput();
					selectedResource = (IResource) input.getAdapter(IResource.class);
				} 
			}
		}
		return selectedResource;
	}	

}
