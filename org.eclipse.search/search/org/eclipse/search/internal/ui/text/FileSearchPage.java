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

import java.util.HashMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;


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
		fSortByNameAction= new SortAction(SearchMessages.getString("FileSearchPage.sort_name.label"), this, FileLabelProvider.SHOW_LABEL_PATH); //$NON-NLS-1$
		fSortByPathAction= new SortAction(SearchMessages.getString("FileSearchPage.sort_path.label"), this, FileLabelProvider.SHOW_PATH_LABEL); //$NON-NLS-1$
		fCurrentSortAction= fSortByNameAction;
	}
	
	public StructuredViewer getViewer() {
		return super.getViewer();
	}

	protected void configureTableViewer(TableViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new DecoratingLabelProvider(new FileLabelProvider(this, FileLabelProvider.SHOW_LABEL), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		viewer.setContentProvider(new FileTableContentProvider(viewer));
		setSortOrder(fCurrentSortAction);
		fContentProvider= (FileContentProvider) viewer.getContentProvider();
	}

	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new DecoratingLabelProvider(new FileLabelProvider(this, FileLabelProvider.SHOW_LABEL), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		viewer.setContentProvider(new FileTreeContentProvider(viewer));
		fContentProvider= (FileContentProvider) viewer.getContentProvider();
	}

	protected void showMatch(Match match, int offset, int length) throws PartInitException {
		IFile file= (IFile) match.getElement();
		IWorkbenchPage page= SearchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor= IDE.openEditor(page, file, false);
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor= (ITextEditor) editor;
			textEditor.selectAndReveal(offset, length);
		} else if (editor != null){
			showWithMarker(editor, file, offset, length);
		}
	}
	private void showWithMarker(IEditorPart editor, IFile file, int offset, int length) throws PartInitException {
		try {
			IMarker marker= file.createMarker(SearchUI.SEARCH_MARKER);
			HashMap attributes= new HashMap(4);
			attributes.put(IMarker.CHAR_START, new Integer(offset));
			attributes.put(IMarker.CHAR_END, new Integer(offset + length));
			marker.setAttributes(attributes);
			IDE.gotoMarker(editor, marker);
			marker.delete();
		} catch (CoreException e) {
			throw new PartInitException(SearchMessages.getString("FileSearchPage.error.marker"), e); //$NON-NLS-1$
		}
	}

	protected void fillContextMenu(IMenuManager mgr) {
		super.fillContextMenu(mgr);
		addSortActions(mgr);
		fActionGroup.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		fActionGroup.fillContextMenu(mgr);
		FileSearchQuery query= (FileSearchQuery) getInput().getQuery();
		if (!"".equals(query.getSearchString())) { //$NON-NLS-1$
		ReplaceAction2 replaceAction= new ReplaceAction2(this, (IStructuredSelection) getViewer().getSelection());
		if (replaceAction.isEnabled())
			mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceAction);
				
		ReplaceAction2 replaceAll= new ReplaceAction2(this);
		if (replaceAll.isEnabled())
			mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceAll);
	}
	}
	
	private void addSortActions(IMenuManager mgr) {
		if (getLayout() != FLAG_LAYOUT_FLAT)
			return;
		MenuManager sortMenu= new MenuManager(SearchMessages.getString("FileSearchPage.sort_by.label")); //$NON-NLS-1$
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
		DecoratingLabelProvider lpWrapper= (DecoratingLabelProvider) viewer.getLabelProvider();
		((FileLabelProvider)lpWrapper.getLabelProvider()).setOrder(action.getSortOrder());
		if (action.getSortOrder() == FileLabelProvider.SHOW_LABEL_PATH) {
			viewer.setSorter(new NameSorter());
		} else {
			viewer.setSorter(new PathSorter());
		}
	}
}
