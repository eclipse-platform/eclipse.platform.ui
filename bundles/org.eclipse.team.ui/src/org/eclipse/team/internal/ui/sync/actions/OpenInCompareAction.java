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
package org.eclipse.team.internal.ui.sync.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.sync.compare.SyncInfoCompareInput;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Action to open a compare editor from a SyncInfo object.
 * 
 * @see SyncInfoCompareInput
 * @since 3.0
 */
public class OpenInCompareAction extends Action {
	
	private SynchronizeView viewer;
	
	public OpenInCompareAction(SynchronizeView viewer) {
		this.viewer = viewer;
		Utils.initAction(this, "action.openInCompareEditor."); //$NON-NLS-1$
	}

	public void run() {
		ISelection selection = viewer.getViewer().getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		SyncInfo info = getSyncInfo(obj);	
		openCompareEditor(viewer, info, true /* keep focus */);		
	}
	
	public static SyncInfoCompareInput openCompareEditor(SynchronizeView viewer, SyncInfo info, boolean keepFocus) {
		SyncInfoCompareInput input = getCompareInput(info);
		if(input != null) {
			if (!prefetchFileContents(viewer, info)) return null;
			IEditorPart editor = findReusableCompareEditor(viewer.getSite().getPage());
			if(editor != null && editor instanceof IReusableEditor) {
				CompareUI.reuseCompareEditor(input, (IReusableEditor)editor);
			} else {
				CompareUI.openCompareEditor(input);
			}
			
			if(keepFocus) {
				 SynchronizeView.showInActivePage(viewer.getSite().getPage());
			}
			return input;
		}
		return null;
	}

	/**
	 * Prefetching the file contents will cache them for use by the compare editor
	 * so that the compare editor doesn't have to perform file transfers. This will
	 * make the transfer cancellable.
	 */
	private static boolean prefetchFileContents(SynchronizeView viewer, SyncInfo info) {
		final IRemoteResource remote = info.getRemote();
		final IRemoteResource base = info.getBase();
		if (remote != null || base != null) {
			final boolean[] ok = new boolean[] { true };
			viewer.run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask(null, (remote == null ? 0 : 100) + (base == null ? 0 : 100));
						if (remote != null)
							remote.getContents(Policy.subMonitorFor(monitor, 100));
						if (base != null)
							base.getContents(Policy.subMonitorFor(monitor, 100));
						monitor.done();
					} catch (TeamException e) {
						ok[0] = false;
						// The sync viewer will show the error to the user so we need only abort the action
						throw new InvocationTargetException(e);
					}
				}
			});
			return ok[0];
		}
		return true;
	}
	
	/**
	 * Returns a SyncInfoCompareInput instance for the current selection.
	 */
	private static SyncInfoCompareInput getCompareInput(SyncInfo info) {
		if (info != null && info.getLocal() instanceof IFile) {
			return new SyncInfoCompareInput(info);								
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
			if(part != null && part.getEditorInput() instanceof SyncInfoCompareInput) {
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
	
	public static SyncInfo getSyncInfo(Object obj) {
		return (SyncInfo)TeamAction.getAdapter(obj, SyncInfo.class);
	}
}