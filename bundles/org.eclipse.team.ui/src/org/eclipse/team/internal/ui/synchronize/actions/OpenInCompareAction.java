/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Action to open a compare editor from a SyncInfo object.
 * 
 * @see SyncInfoCompareInput
 * @since 3.0
 */
public class OpenInCompareAction extends Action {
	
	private String name;
	private ISynchronizePageSite site;
	
	public OpenInCompareAction(ISynchronizePageSite site, String name) {
		this.name = name;
		this.site = site;
		Utils.initAction(this, "action.openInCompareEditor."); //$NON-NLS-1$
	}

	public void run() {
		ISelection selection = site.getSelectionProvider().getSelection();
		if(selection instanceof IStructuredSelection) {
		Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof SyncInfoModelElement) {
				SyncInfo info = ((SyncInfoModelElement) obj).getSyncInfo();
				if (info != null) {
					openCompareEditor(site, name, info, false /* don't keep focus */);
				}
			}
		}
	}
	
	public static SyncInfoCompareInput openCompareEditor(ISynchronizePageSite site, String name, SyncInfo info, boolean keepFocus) {		
		SyncInfoCompareInput input = getCompareInput(name, info);
		if(input != null) {
			IWorkbenchSite ws = site.getWorkbenchSite();
			if (ws == null) return null;
			IWorkbenchPage wpage = ws.getPage();
			IEditorPart editor = findReusableCompareEditor(wpage);			
			
			if(editor != null) {
				IEditorInput otherInput = editor.getEditorInput();
				if(otherInput.equals(input)) {
					// simply provide focus to editor
					wpage.activate(editor);
				} else {
					// if editor is currently not open on that input either re-use existing
					CompareUI.reuseCompareEditor(input, (IReusableEditor)editor);
					wpage.activate(editor);
				}
			} else {
				CompareUI.openCompareEditor(input);
				editor = wpage.getActiveEditor();
			}
			
			if(keepFocus) {
				site.setFocus();
			}
			return input;
		}			
		return null;
	}
	
	/**
	 * Returns a SyncInfoCompareInput instance for the current selection.
	 */
	private static SyncInfoCompareInput getCompareInput(String name, SyncInfo info) {
		if (info != null && info.getLocal() instanceof IFile) {
			return new SyncInfoCompareInput(name, info);
		}
		return null;
	}				

	/**
	 * Returns an editor that can be re-used. An open compare editor that
	 * has un-saved changes cannot be re-used.
	 */
	public static IEditorPart findReusableCompareEditor(IWorkbenchPage page) {
		IEditorReference[] editorRefs = page.getEditorReferences();	
		for (int i = 0; i < editorRefs.length; i++) {
			IEditorPart part = editorRefs[i].getEditor(true);
			if(part != null && part.getEditorInput() instanceof SyncInfoCompareInput && part instanceof IReusableEditor) {
				if(! part.isDirty()) {	
					return part;	
				}
			}
		}
		return null;
	}
	
	/**
	 * Close a compare editor that is opened on the given IResource.
	 * 
	 * @param site the view site in which to close the editors 
	 * @param resource the resource to use to find the compare editor
	 */
	public static void closeCompareEditorFor(final IWorkbenchPartSite site, final IResource resource) {
		site.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IEditorPart editor = findOpenCompareEditor(site, resource);
				if(editor != null) {
					site.getPage().closeEditor(editor, true /*save changes if required */);
				}
			}
		});
	}

	/**
	 * Returns an editor handle if a SyncInfoCompareInput compare editor is opened on 
	 * the given IResource.
	 * 
	 * @param site the view site in which to search for editors
	 * @param resource the resource to use to find the compare editor
	 * @return an editor handle if found and <code>null</code> otherwise
	 */
	public static IEditorPart findOpenCompareEditor(IWorkbenchPartSite site, IResource resource) {
		IWorkbenchPage page = site.getPage();
		IEditorReference[] editorRefs = page.getEditorReferences();						
		for (int i = 0; i < editorRefs.length; i++) {
			final IEditorPart part = editorRefs[i].getEditor(false /* don't restore editor */);
			if(part != null) {
				IEditorInput input = part.getEditorInput();
				if(part != null && input instanceof SyncInfoCompareInput) {
					SyncInfo inputInfo = ((SyncInfoCompareInput)input).getSyncInfo();
					if(inputInfo.getLocal().equals(resource)) {
						return part;
					}
				}
			}
		}
		return null;
	}
}