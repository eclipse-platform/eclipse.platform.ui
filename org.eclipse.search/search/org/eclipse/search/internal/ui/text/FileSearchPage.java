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
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

import org.eclipse.search.internal.ui.SearchPlugin;


/**
 * @author Thomas Mäder
 *
 */
public class FileSearchPage extends AbstractTextSearchViewPage {
	private ActionGroup fActionGroup;
	private FileContentProvider fContentProvider;
	private SortAction fCurrentSortAction;
	private SortAction fSortByNameAction;
	private SortAction fSortByPathAction;
	
	public FileSearchPage() {
		fSortByNameAction= new SortAction("Name", this, FileLabelProvider.SHOW_LABEL_PATH);
		fSortByPathAction= new SortAction("Path", this, FileLabelProvider.SHOW_PATH_LABEL);
		fCurrentSortAction= fSortByNameAction;
	}

	protected void configureViewer(StructuredViewer viewer) {
		viewer.setLabelProvider(new DelegatingLabelProvider(this, new FileLabelProvider(FileLabelProvider.SHOW_LABEL)));
		if (viewer instanceof TreeViewer)
			viewer.setContentProvider(new FileTreeContentProvider((TreeViewer) viewer));
		else {
			viewer.setContentProvider(new FileTableContentProvider((TableViewer) viewer));
			setSortOrder(fCurrentSortAction);
		}
		fContentProvider= (FileContentProvider) viewer.getContentProvider();
	}

	protected void showMatch(Object element, int offset, int length) throws PartInitException {
		IFile file= (IFile) element;
		IWorkbenchPage page= SearchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor= IDE.openEditor(page, file, false);
		if (!(editor instanceof ITextEditor))
			return;
		ITextEditor textEditor= (ITextEditor) editor;
		textEditor.selectAndReveal(offset, length);
	}
	protected void fillContextMenu(IMenuManager mgr) {
		super.fillContextMenu(mgr);
		addSortActions(mgr);
		fActionGroup.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		fActionGroup.fillContextMenu(mgr);
	}
	
	private void addSortActions(IMenuManager mgr) {
		if (!isFlatMode())
			return;
		MenuManager sortMenu= new MenuManager("Sort By");
		sortMenu.add(fSortByNameAction);
		sortMenu.add(fSortByPathAction);
		
		fSortByNameAction.setChecked(fCurrentSortAction == fSortByNameAction);
		fSortByPathAction.setChecked(fCurrentSortAction == fSortByPathAction);
		
		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, sortMenu);
	}

	public void setViewPart(ISearchResultViewPart part) {
		super.setViewPart(part);
		fActionGroup= new NewTextSearchActionGroup(part);
	}
	
	public void dispose() {
		fActionGroup.dispose();
		super.dispose();
	}

	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}

	protected void clear() {
		if (fContentProvider != null)
			fContentProvider.clear();
	}

	public void setSortOrder(SortAction action) {
		fCurrentSortAction= action;
		StructuredViewer viewer= getViewer();
		DelegatingLabelProvider lpWrapper= (DelegatingLabelProvider) viewer.getLabelProvider();
		((FileLabelProvider)lpWrapper.getLabelProvider()).setOrder(action.getSortOrder());
		if (action.getSortOrder() == FileLabelProvider.SHOW_LABEL_PATH) {
			viewer.setSorter(new NameSorter());
		} else {
			viewer.setSorter(new PathSorter());
		}
	}
}
