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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.dialogs.PropertyDialogAction;

import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.SearchManager;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchResultView;
import org.eclipse.search.internal.ui.SearchResultViewer;

/**
 * Action group that adds the Text search actions to a context menu and
 * the global menu bar.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.1
 */
class TextSearchActionGroup extends ActionGroup {

	private ISelectionProvider fSelectionProvider;		
	private IWorkbenchPage fPage;
	private PropertyDialogAction fOpenPropertiesDialog;

	public TextSearchActionGroup(IViewPart part) {
		Assert.isNotNull(part);
		IWorkbenchPartSite site= part.getSite();
		fSelectionProvider= site.getSelectionProvider();
		fPage= site.getPage();
		fOpenPropertiesDialog= new PropertyDialogAction(site.getShell(), fSelectionProvider);

		ISelection selection= fSelectionProvider.getSelection();

		if (selection instanceof IStructuredSelection)
			fOpenPropertiesDialog.selectionChanged((IStructuredSelection)selection);
		else
			fOpenPropertiesDialog.selectionChanged(selection);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		if (!isTextSearch())
			return;

		// view must exist if we create a context menu for it.
		ISearchResultView view= SearchUI.getSearchResultView();
		IStructuredSelection selection= null;
		if (getContext().getSelection() instanceof IStructuredSelection)
			selection= (IStructuredSelection)getContext().getSelection();
		else
			selection= StructuredSelection.EMPTY;
		
		addOpenWithMenu(menu, selection);
			
		ReplaceAction replaceAll= new ReplaceAction(view.getSite(), getSearchResultEntries());
		if (replaceAll.isEnabled())
			menu.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceAll);
		ReplaceAction replaceSelected= new ReplaceAction(view.getSite(), selection);
		if (replaceSelected.isEnabled())
			menu.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceSelected);

		if (fOpenPropertiesDialog != null && fOpenPropertiesDialog.isEnabled() && selection != null &&fOpenPropertiesDialog.isApplicableForSelection(selection))
			menu.appendToGroup(IContextMenuConstants.GROUP_PROPERTIES, fOpenPropertiesDialog);
	}
	
	private List getSearchResultEntries() {
		SearchResultView view= (SearchResultView) SearchPlugin.getSearchResultView();
		// we can assume we have a view, how else would we be called?
		SearchResultViewer viewer= view.getViewer();
		Table table= viewer.getTable();
		TableItem[] items= table.getItems();
		List elements = new ArrayList(items.length);
		for (int i = 0; i < items.length; i++) {
			elements.add(items[i].getData());
		}
		return elements;
	}

	private boolean isTextSearch() {
		IRunnableWithProgress operation= SearchManager.getDefault().getCurrentSearch().getOperation();
		if (operation instanceof TextSearchOperation) {
			String pattern= ((TextSearchOperation)operation).getPattern();
			return pattern != null && pattern.length() > 0;
		}
		return false;
	}

	private void addOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {
		if (selection == null || selection.size() != 1)
			return;

		Object o= selection.getFirstElement();
		if (!(o instanceof ISearchResultViewEntry))
			return;

		Object resource= ((ISearchResultViewEntry)o).getResource();
		if (!(resource instanceof IFile))
			return; 

		// Create menu
		IMenuManager submenu= new MenuManager(SearchMessages.getString("OpenWithMenu.label")); //$NON-NLS-1$
		submenu.add(new OpenWithMenu(fPage, (IFile)resource));

		// Add the submenu.
		menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, submenu);
	}

	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setGlobalActionHandlers(actionBar);
	}
	
	private void setGlobalActionHandlers(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), fOpenPropertiesDialog);		
	}
}	
