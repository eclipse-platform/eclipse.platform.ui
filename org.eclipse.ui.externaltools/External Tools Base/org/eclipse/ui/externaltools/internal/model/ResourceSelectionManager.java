package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Tracks the selected resource, such that it can be accessed from a non-ui
 * thread.
 */
public class ResourceSelectionManager implements IWindowListener, ISelectionListener {

	// singleton
	private static ResourceSelectionManager fgDefault;
	
	private IResource fSelectedResource = null;
	
	private ResourceSelectionManager() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.addWindowListener(this);
		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			windowActivated(activeWindow);
		} 
	}
	
	/**
	 * Returns the singleton resource selection manager
	 * 
	 * @return ResourceSelectionManager	 */
	public static ResourceSelectionManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new ResourceSelectionManager(); 
		}
		return fgDefault;
	}
	
	/**
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IResource selectedResource = null;
		if (selection instanceof IStructuredSelection) {
			Object result = ((IStructuredSelection)selection).getFirstElement();
			if (result instanceof IResource) {
				selectedResource = (IResource) result;
			} else if (result instanceof IAdaptable) {
				selectedResource = (IResource)((IAdaptable) result).getAdapter(IResource.class);
			}
		}
		
		if (selectedResource == null) {
			// If the active part is an editor, get the file resource used as input.
			if (part instanceof IEditorPart) {
				IEditorPart editorPart = (IEditorPart) part;
				IEditorInput input = editorPart.getEditorInput();
				selectedResource = (IResource) input.getAdapter(IResource.class);
			} 
		}
		
		fSelectedResource = selectedResource;
	}
	
	/**
	 * Returns the active resource.
	 * 
	 * @return IResource	 */
	public IResource getActiveResource() {
		return fSelectedResource;
	}

}
