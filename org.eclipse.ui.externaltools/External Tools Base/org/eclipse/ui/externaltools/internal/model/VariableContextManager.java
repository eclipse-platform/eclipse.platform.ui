package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Maintains the context used to expand variables. The context is based on
 * the selected resource, unless a build is in progress - in which case
 * the context is based on the project being built..
 */
public class VariableContextManager {

	// singleton
	private static VariableContextManager fgDefault;
	
	private boolean fBuilding = false;
	private IProject fProject = null;
	private int fKind;
	
	private VariableContextManager() {
	}
	
	/**
	 * Returns the singleton resource selection manager
	 * 
	 * @return VariableContextManager	 */
	public static VariableContextManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new VariableContextManager(); 
		}
		return fgDefault;
	}
	
	private IResource getSelectedResource() {
		IResource selectedResource= null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) { //may be running headless
			IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
			if (activeWindow == null) {
				return null;
			}
			ISelectionService service = activeWindow.getSelectionService();
			IWorkbenchPage page = activeWindow.getActivePage();
			if (page == null) {
				return null;
			}
			IWorkbenchPart part = page.getActivePart();
			if (part == null) {
				return null;
			}
			ISelection selection = service.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object result = ((IStructuredSelection)selection).getFirstElement();
				if (result instanceof IResource) {
					selectedResource = (IResource) result;
				} else if (result instanceof IAdaptable) {
					selectedResource = (IResource)((IAdaptable) result).getAdapter(IResource.class);
				}
			}
			if (selectedResource == null && part instanceof IEditorPart) {
					// If the active part is an editor, get the file resource used as input.
					IEditorPart editorPart = (IEditorPart) part;
					IEditorInput input = editorPart.getEditorInput();
					selectedResource = (IResource) input.getAdapter(IResource.class);
				}
			}
			
		return selectedResource;
	}
	
	/**
	 * Returns the active variable context. The build context is that of the
	 * seleted resource, or a project being built.
	 * 
	 * @return variable context	 */
	public ExpandVariableContext getVariableContext() {
		if (fBuilding) {
			return new ExpandVariableContext(fProject, fKind);
		} else {
			return new ExpandVariableContext(getSelectedResource());
		}
	}
	
	/**
	 * Notification that the given project is being built.
	 * 
	 * @param project	 * @param kind
	 * @see ExternalToolBuilder#build(int, Map, IProgressMonitor)	 */
	public void buildStarted(IProject project, int kind) {
		fBuilding = true;
		fProject = project;
		fKind = kind;
	}
	
	/**
	 * Notification the building the current project has completed.
	 * @see ExternalToolBuilder#build(int, Map, IProgressMonitor)
	 */
	public void buildEnded() {
		fBuilding = false;
	}
}
