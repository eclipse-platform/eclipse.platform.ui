/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.ui.Search;
import org.eclipse.search.internal.ui.SearchManager;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchResultView;
import org.eclipse.search.internal.ui.SearchResultViewEntry;
import org.eclipse.search.internal.ui.util.ExceptionHandler;

/* package */ class ReplaceAction extends Action {
	
	private IWorkbenchSite fSite;
	private List fElements;
	
	public ReplaceAction(IWorkbenchSite site, List elements) {
		Assert.isNotNull(site);
		fSite= site;
		if (elements != null)
			fElements= elements;
		else
			fElements= new ArrayList(0);
		setText(SearchMessages.getString("ReplaceAction.label_all")); //$NON-NLS-1$
		setEnabled(!fElements.isEmpty());
	}
	
	public ReplaceAction(IWorkbenchSite site, IStructuredSelection selection) {
		Assert.isNotNull(site);
		fSite= site;
		setText(SearchMessages.getString("ReplaceAction.label_selected")); //$NON-NLS-1$
		fElements= selection.toList();
		setEnabled(!fElements.isEmpty());
	}
	
	public void run() {
		Search search= SearchManager.getDefault().getCurrentSearch();
		IRunnableWithProgress operation= search.getOperation();
		if (operation instanceof TextSearchOperation) {
			if (validateResources((TextSearchOperation) operation)) {
				ReplaceDialog dialog= new ReplaceDialog(fSite.getShell(), fElements, (TextSearchOperation)operation);
				dialog.open();
			}
		} else {
			MessageDialog.openError(fSite.getShell(), getDialogTitle(), SearchMessages.getString("ReplaceAction.error.only_on_text_search")); //$NON-NLS-1$
		}
	}
	
	private boolean validateResources(final TextSearchOperation operation) {
		final List outOfDateEntries= new ArrayList();
		for (Iterator elements = fElements.iterator(); elements.hasNext();) {
			SearchResultViewEntry entry = (SearchResultViewEntry) elements.next();
			if (isOutOfDate(entry)) {
				outOfDateEntries.add(entry);
			}
		}
	
		final List outOfSyncEntries= new ArrayList();
		for (Iterator elements = fElements.iterator(); elements.hasNext();) {
			SearchResultViewEntry entry = (SearchResultViewEntry) elements.next();
			if (isOutOfSync(entry)) {
				outOfSyncEntries.add(entry);
			}
		}
		
		if (outOfDateEntries.size() > 0 || outOfSyncEntries.size() > 0) {
			if (askForResearch(outOfDateEntries, outOfSyncEntries)) {
				ProgressMonitorDialog pmd= new ProgressMonitorDialog(fSite.getShell());
				try {
					pmd.run(true, true, new WorkspaceModifyOperation(null) {
						protected void execute(IProgressMonitor monitor) throws CoreException {
							research(monitor, outOfDateEntries, operation);
						}
					});
					return true;
				} catch (InvocationTargetException e) {
					ExceptionHandler.handle(e, fSite.getShell(), SearchMessages.getString("ReplaceAction.label"), SearchMessages.getString("ReplaceAction.research.error")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (InterruptedException e) {
					// canceled
				}
			}
			return false;
		}
		return true;
	}

	private void research(IProgressMonitor monitor, List outOfDateEntries, TextSearchOperation operation) throws CoreException {
		IStatus status= null;
		for (Iterator elements = outOfDateEntries.iterator(); elements.hasNext();) {
			SearchResultViewEntry entry = (SearchResultViewEntry) elements.next();
				status = research(operation, monitor, entry);
			if (status != null && !status.isOK()) {
				throw new CoreException(status);
			}
		}
	}

	private boolean askForResearch(List outOfDateEntries, List outOfSyncEntries) {
		SearchResultView view= (SearchResultView) SearchPlugin.getSearchResultView();
		ILabelProvider labelProvider= null;
		if (view != null)
			labelProvider= view.getLabelProvider();
		SearchAgainConfirmationDialog dialog= new SearchAgainConfirmationDialog(fSite.getShell(), labelProvider, outOfSyncEntries, outOfDateEntries);
		return dialog.open() == IDialogConstants.OK_ID;
	}

	private String getDialogTitle() {
		return SearchMessages.getString("ReplaceAction.dialog.title"); //$NON-NLS-1$
	}		
	
	private boolean isOutOfDate(SearchResultViewEntry entry) {
		IResource resource= entry.getResource();
		if (entry.getModificationStamp() != resource.getModificationStamp())
			return true;
		ITextFileBufferManager bm= FileBuffers.getTextFileBufferManager();
		ITextFileBuffer fb= bm.getTextFileBuffer(resource.getFullPath());
		if (fb != null && fb.isDirty())
			return true;
		return false;
	}

	private boolean isOutOfSync(SearchResultViewEntry entry) {
		return !entry.getResource().isSynchronized(IResource.DEPTH_ZERO); 
	}
		
	private IStatus research(TextSearchOperation operation, final IProgressMonitor monitor, SearchResultViewEntry entry) throws CoreException {
		List markers= new ArrayList();
		markers.addAll(entry.getMarkers());
		operation.searchInFile((IFile) entry.getResource(), new ITextSearchResultCollector() {
			public IProgressMonitor getProgressMonitor() {
				return monitor;
			}
			
			public void aboutToStart() {
				// nothing to do
			}
			
			public void accept(IResourceProxy proxy, String line, int start, int length, int lineNumber) throws CoreException {
				IFile file= (IFile)proxy.requestResource();
				if (start < 0 || length < 1)
					return;
				IMarker marker= file.createMarker(SearchUI.SEARCH_MARKER);
				HashMap attributes= new HashMap(4);
				attributes.put(SearchUI.LINE, line);
				attributes.put(IMarker.CHAR_START, new Integer(start));
				attributes.put(IMarker.CHAR_END, new Integer(start + length));
				attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
				marker.setAttributes(attributes);
			}
			
			public void done(){
				// nothing to do
			}
		});
		IStatus status = operation.getStatus();
		if (status == null || status.isOK()) {
			for (Iterator markerIter = markers.iterator(); markerIter.hasNext();) {
				IMarker marker = (IMarker) markerIter.next();
				marker.delete();
			}
		}
		return status;
	}
	
}
