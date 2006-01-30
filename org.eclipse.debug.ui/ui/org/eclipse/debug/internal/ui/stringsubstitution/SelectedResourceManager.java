/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Maintains the context used to expand variables. The context is based on
 * the selected resource.
 */
public class SelectedResourceManager implements IWindowListener, ISelectionListener {

	// singleton
	private static SelectedResourceManager fgDefault;
	
	private IResource fSelectedResource = null;
	private ITextSelection fSelectedText = null;
	private Stack fWindowStack = new Stack();
	
	private SelectedResourceManager() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) { //may be running headless
			workbench.addWindowListener(this);
			IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
			if (activeWindow != null) {
				windowActivated(activeWindow);
			}
		} 
	}
	
	/**
	 * Returns the singleton resource selection manager
	 * 
	 * @return VariableContextManager
	 */
	public static SelectedResourceManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new SelectedResourceManager(); 
		}
		return fgDefault;
	}
	
	/**
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		fWindowStack.remove(window);
		fWindowStack.push(window);
		ISelectionService service = window.getSelectionService(); 
		service.addSelectionListener(this);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IWorkbenchPart part = page.getActivePart();
			if (part != null) {				
				ISelection selection = service.getSelection();
				if (selection != null) {
					selectionChanged(part, selection);
				}
			}
		}
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		ISelectionService selectionService = window.getSelectionService();
        selectionService.removeSelectionListener(this);
		fWindowStack.remove(window);
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
		windowActivated(window);
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
		if (fWindowStack.isEmpty() || !fWindowStack.peek().equals(window)) {
			// selection is not in the active window
			return;
		}
		IResource selectedResource = null;
		if (selection instanceof IStructuredSelection) {
			Object result = ((IStructuredSelection)selection).getFirstElement();
			if (result instanceof IResource) {
				selectedResource = (IResource) result;
			} else if (result instanceof IAdaptable) {
			    IAdaptable adaptable = (IAdaptable) result;
				selectedResource = (IResource)adaptable.getAdapter(IResource.class);
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
		
		if (selectedResource != null) {
			fSelectedResource = selectedResource;
		}
		
		if (selection instanceof ITextSelection) {
			fSelectedText = (ITextSelection)selection;
		}
	}
	
	/**
	 * Returns the currently selected resource in the active workbench window,
	 * or <code>null</code> if none. If an editor is active, the resource adapater
	 * associated with the editor is returned.
	 * 
	 * @return selected resource or <code>null</code>
	 */
	public IResource getSelectedResource() {
		return fSelectedResource;
	}
	
	/**
	 * Returns the current text selection as a <code>String</code>, or <code>null</code> if
	 * none.
	 * 
	 * @return the current text selection as a <code>String</code> or <code>null</code>
	 */
	public String getSelectedText() {
		return fSelectedText.getText();
	}
	
	/**
	 * Returns the active workbench window, or <code>null</code> if none.
	 * 
	 * @return the active workbench window, or <code>null</code> if none
	 * @since 3.2
	 */
	public IWorkbenchWindow getActiveWindow() {
		try {
			return (IWorkbenchWindow) fWindowStack.peek();
		} catch (EmptyStackException e) {
		}
		return null;
	}

}
