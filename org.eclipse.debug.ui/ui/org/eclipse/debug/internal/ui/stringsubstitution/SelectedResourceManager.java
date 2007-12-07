/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Maintains the context used to expand variables. The context is based on
 * the selected resource.
 */
public class SelectedResourceManager  {

	// singleton
	private static SelectedResourceManager fgDefault;
	
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
	 * Returns the selection from the currently active part. If the active part is an
	 * editor a new selection of the editor part is made, otherwise the selection 
	 * from the parts' selection provider is returned if it is a structured selection. Otherwise
	 * and empty selection is returned, never <code>null</code>.
	 * <br>
	 * <p>
	 * This method is intended to be called from the UI thread.
	 * </p>
	 * 
	 * @return the <code>IStructuredSelection</code> from the current parts' selection provider, or
	 * a new <code>IStructuredSelection</code> of the current editor part, depending on what the current part
	 * is.
	 * 
	 * @since 3.3
	 */
	public IStructuredSelection getCurrentSelection() {
		if(DebugUIPlugin.getStandardDisplay().getThread().equals(Thread.currentThread())) {
			return getCurrentSelection0();
		}
		else {
			final IStructuredSelection[] selection = new IStructuredSelection[1];
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					selection[0] = getCurrentSelection0();
				}
			});
			return selection[0];
		}
	}
	
	/**
	 * Underlying implementation of <code>getCurrentSelection</code>
	 * @return the current selection
	 * 
	 * @since 3.4
	 */
	private IStructuredSelection getCurrentSelection0() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if(window != null) {
			IWorkbenchPage page  = window.getActivePage();
			if(page != null) {
				IWorkbenchPart part = page.getActivePart();
				if(part instanceof IEditorPart) {
					return new StructuredSelection(part);
				}
				else if(part != null) {
					IWorkbenchSite site = part.getSite();
					if(site != null) {
						ISelectionProvider provider = site.getSelectionProvider();
						if(provider != null) {
							ISelection selection = provider.getSelection();
							if(selection instanceof IStructuredSelection) {
								return (IStructuredSelection) provider.getSelection();
							}
						}
					}
				}
			}
		}
		return StructuredSelection.EMPTY;
	}
		
	/**
	 * Returns the currently selected resource in the active workbench window,
	 * or <code>null</code> if none. If an editor is active, the resource adapter
	 * associated with the editor is returned.
	 * 
	 * @return selected resource or <code>null</code>
	 */
	public IResource getSelectedResource() {
		if(DebugUIPlugin.getStandardDisplay().getThread().equals(Thread.currentThread())) {
			return getSelectedResource0();
		}
		else {
			final IResource[] resource = new IResource[1];
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					resource[0] = getSelectedResource0();
				}
			});
			return resource[0];
		}
	}
	
	/**
	 * Returns the currently selected resource from the active part, or <code>null</code> if one cannot be
	 * resolved.
	 * @return the currently selected <code>IResource</code>, or <code>null</code> if none.
	 * @since 3.3
	 */
	protected IResource getSelectedResource0() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		IResource resource = null;
		if(window != null) {
			IWorkbenchPage page  = window.getActivePage();
			if(page != null) {
				IWorkbenchPart part = page.getActivePart();
				if(part instanceof IEditorPart) {
					IEditorPart epart = (IEditorPart) part;
					resource = (IResource) epart.getEditorInput().getAdapter(IResource.class);
				}
				else if(part != null) {
					IWorkbenchPartSite site = part.getSite();
					if(site != null) {
						ISelectionProvider provider = site.getSelectionProvider();
						if(provider != null) {
							ISelection selection = provider.getSelection();
							if(selection instanceof IStructuredSelection) {
								IStructuredSelection ss = (IStructuredSelection) selection;
								if(!ss.isEmpty()) {
									Iterator iterator = ss.iterator();
									while (iterator.hasNext() && resource == null) {
										Object next = iterator.next();
										resource = (IResource) Platform.getAdapterManager().getAdapter(next, IResource.class);
									}
								}
							}
						}
					}
				}
			}
		}
		return resource;
	}
	
	/**
	 * Returns the current text selection as a <code>String</code>, or <code>null</code> if
	 * none.
	 * 
	 * @return the current text selection as a <code>String</code> or <code>null</code>
	 */
	public String getSelectedText() {
		if(DebugUIPlugin.getStandardDisplay().getThread().equals(Thread.currentThread())) {
			return getSelectedText0();
		}
		else {
			final String[] text = new String[1];
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					text[0] = getSelectedText0();
				}
			});
			return text[0];
		}
	}
	
	/**
	 * Returns the selected text from the most currently active editor. The editor does not have to 
	 * have focus at the time this method is called.
	 * @return the currently selected text in the most recent active editor.
	 * 
	 * @since 3.3
	 */
	protected String getSelectedText0() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if(window != null) {
			IWorkbenchPage page = window.getActivePage();
			if(page != null) {
				IEditorPart epart = page.getActiveEditor();
				if(epart != null) {
					IEditorSite esite = epart.getEditorSite();
					if(esite != null) {
						ISelectionProvider sprovider = esite.getSelectionProvider();
						if(sprovider != null) {
							ISelection selection = sprovider.getSelection();
							if(selection instanceof ITextSelection) {
								return ((ITextSelection)selection).getText();
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the active workbench window, or <code>null</code> if none.
	 * 
	 * @return the active workbench window, or <code>null</code> if none
	 * @since 3.2
	 */
	public IWorkbenchWindow getActiveWindow() {
		if(DebugUIPlugin.getStandardDisplay().getThread().equals(Thread.currentThread())) {
			return DebugUIPlugin.getActiveWorkbenchWindow();
		}
		else {
			final IWorkbenchWindow[] window = new IWorkbenchWindow[1];
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					window[0] = DebugUIPlugin.getActiveWorkbenchWindow();
				}
			});
			return window[0];
		}
	}

}
