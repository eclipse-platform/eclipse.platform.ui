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

import org.eclipse.compare.CompareEditorInput;
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

/**
 * Opens a compare editor on a selected SyncInfo object
 */
public class OpenInCompareAction extends Action {
	
	private SynchronizeView viewer;
	
	public OpenInCompareAction(SynchronizeView viewer) {
		this.viewer = viewer;
		Utils.initAction(this, "action.openInCompareEditor."); //$NON-NLS-1$
	}

	public void run() {	
		openEditor();
	}
	
	private void openEditor() {
		CompareEditorInput input = getCompareInput();
		if(input != null) {
			if (!prefetchFileContents()) return;
			IEditorPart editor = reuseCompareEditor((SyncInfoCompareInput)input);
			if(editor != null && editor instanceof IReusableEditor) {
				CompareUI.openCompareEditor(input);
				// should be enabled once  Bug 38770 is fixed
				//((IReusableEditor)editor).setInput(input);
			} else {
				CompareUI.openCompareEditor(input);
			}
			// This could be a user preference.
			//SynchronizeView.showInActivePage(viewer.getSite().getPage());
		}		
	}
	
	/*
	 * Prefetching the file contents will cache them for use by the compare editor
	 */
	private boolean prefetchFileContents() {
		ISelection selection = viewer.getViewer().getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		SyncInfo info = getSyncInfo(obj);
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
	
	private CompareEditorInput getCompareInput() {
		ISelection selection = viewer.getViewer().getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		SyncInfo info = getSyncInfo(obj);
		if (info != null && info.getLocal() instanceof IFile) {
			return new SyncInfoCompareInput(info);								
		}
		return null;
	}				

	private SyncInfo getSyncInfo(Object obj) {
		return (SyncInfo)TeamAction.getAdapter(obj, SyncInfo.class);
	}

	private IEditorPart reuseCompareEditor(SyncInfoCompareInput input) {
		IWorkbenchPage page = viewer.getSite().getPage();
		IEditorReference[] editorRefs = page.getEditorReferences();
		
		IEditorPart editor = page.findEditor(input);
		if(editor == null) {
			for (int i = 0; i < editorRefs.length; i++) {
				IEditorPart part = editorRefs[i].getEditor(true);
				if(part != null && part.getEditorInput() instanceof SyncInfoCompareInput) {
					if(! part.isDirty()) {	
						// should be removed once Bug 38770 is fixed					
						page.closeEditor(part, true /*save changes if required */);		
					}
				}
			}
		}
		return editor;
	}
	
	public static void closeCompareEditorFor(final SynchronizeView viewer, final IResource resource) {
		viewer.getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = viewer.getSite().getPage();
				IEditorReference[] editorRefs = page.getEditorReferences();
						
				for (int i = 0; i < editorRefs.length; i++) {
					final IEditorPart part = editorRefs[i].getEditor(false /* don't restore editor */);
					if(part != null) {
						IEditorInput input = part.getEditorInput();
						if(part != null && input instanceof SyncInfoCompareInput) {
							SyncInfo inputInfo = ((SyncInfoCompareInput)input).getSyncInfo();
							if(inputInfo.getLocal().equals(resource)) {
										page.closeEditor(part, true /*save changes if required */);
							}
						}
					}
				}
			}
		});				
	}
}