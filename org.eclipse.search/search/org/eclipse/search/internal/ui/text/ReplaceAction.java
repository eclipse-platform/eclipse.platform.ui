/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.search.internal.ui.Search;
import org.eclipse.search.internal.ui.SearchManager;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchResultViewEntry;
import org.eclipse.search.internal.ui.util.ListDialog;

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
		if (validateResources()) {
			Search search= SearchManager.getDefault().getCurrentSearch();
			IRunnableWithProgress operation= search.getOperation();
			if (operation instanceof TextSearchOperation) {
				ReplaceDialog dialog= new ReplaceDialog(fSite.getShell(), fElements, fSite.getWorkbenchWindow(), ((TextSearchOperation)operation).getPattern());
				dialog.open();
			} else {
				MessageDialog.openError(fSite.getShell(), getDialogTitle(), SearchMessages.getString("ReplaceAction.error.only_on_text_search")); //$NON-NLS-1$
			}
		}
	}
	
	private boolean validateResources() {
		List modifiedFiles= new ArrayList();
		for (Iterator iter = fElements.iterator(); iter.hasNext();) {
			SearchResultViewEntry entry= (SearchResultViewEntry) iter.next();
			IResource resource= entry.getResource();
			if (resource instanceof IFile && ((IFile)resource).getModificationStamp() != entry.getModificationStamp())
				modifiedFiles.add(resource);
		}
		if (!modifiedFiles.isEmpty()) {
			showModifiedFileDialog(modifiedFiles);
			return false;
		}
		IFile[] readOnlyFiles= getReadOnlyFiles();
		if (readOnlyFiles.length == 0)
			return true;
		Map currentStamps= createModificationStampMap(readOnlyFiles);
		IStatus status= ResourcesPlugin.getWorkspace().validateEdit(readOnlyFiles, fSite.getShell());
		if (!status.isOK()) {
			ErrorDialog.openError(fSite.getShell(), getDialogTitle(), SearchMessages.getString("ReplaceAction.error.unable_to_perform"), status); //$NON-NLS-1$
			return false;
		}
		modifiedFiles= new ArrayList();
		Map newStamps= createModificationStampMap(readOnlyFiles);
		for (Iterator iter= currentStamps.keySet().iterator(); iter.hasNext();) {
			IFile file= (IFile) iter.next();
			if (! currentStamps.get(file).equals(newStamps.get(file))) {
				modifiedFiles.add(file);
			}
		}
		if (!modifiedFiles.isEmpty()) {
			showModifiedFileDialog(modifiedFiles);
			return false;
		}
		return true;
	}

	private void showModifiedFileDialog(List modifiedFiles) {
		ListDialog dialog= new ListDialog(fSite.getShell(), modifiedFiles, getDialogTitle(), 
			SearchMessages.getString("ReplaceAction.error.changed_files"),  //$NON-NLS-1$
			new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					return ((List)inputElement).toArray();
				}
				public void dispose() {
				}
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
			}, 
			new WorkbenchLabelProvider());
		dialog.setCreateCancelButton(false);
		dialog.open();
	}
	
	private IFile[] getReadOnlyFiles() {
		List result= new ArrayList();
		for (Iterator iter = fElements.iterator(); iter.hasNext();) {
			IResource resource= ((SearchResultViewEntry) iter.next()).getResource();
			if (resource instanceof IFile && resource.isReadOnly())
				result.add(resource);
		}
		return (IFile[]) result.toArray(new IFile[result.size()]);
	}
	
	private static Map createModificationStampMap(IFile[] files){
		Map map= new HashMap();
		for (int i= 0; i < files.length; i++) {
			IFile file= files[i];
			map.put(file, new Long(file.getModificationStamp()));
		}
		return map;
	}
	
	private String getDialogTitle() {
		return SearchMessages.getString("ReplaceAction.dialog.title"); //$NON-NLS-1$
	}
}
