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
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.views.navigator.ResourceNavigatorMessages;

/**
 * This is the action group for the open actions. It contains open
 * actions for 
 */
public class OpenWithActionGroup extends ActionGroup {

	private OpenFileInSystemEditorAction openFileAction;
	private OpenInCompareAction openInCompareAction;
	private String name;
	private ISynchronizePageSite site;

	public OpenWithActionGroup(ISynchronizePageSite site, String name) {
		this.name = name;
		this.site = site;
		makeActions();
	}

	protected void makeActions() {
		IWorkbenchSite ws = site.getWorkbenchSite();
		if (ws != null) {
			openFileAction = new OpenFileInSystemEditorAction(ws.getPage());
			openInCompareAction = new OpenInCompareAction(site, name);
		}
	}

	public void fillContextMenu(IMenuManager menu, String groupId) {
		ISelection selection = site.getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			fillOpenWithMenu(menu, groupId, (IStructuredSelection)selection);
		}
	}

	/**
	 * Adds the OpenWith submenu to the context menu.
	 * 
	 * @param menu the context menu
	 * @param selection the current selection
	 */
	private void fillOpenWithMenu(IMenuManager menu, String groupId, IStructuredSelection selection) {

		// Only supported if exactly one file is selected.
		if (selection == null || selection.size() != 1)
			return;
		Object element = selection.getFirstElement();
		IResource resources[] = Utils.getResources(new Object[] {element});
		IResource resource = null;		
		if(resources.length == 0) {
			return;
		}
		resource = resources[0];
		
		if(resource.getType() != IResource.FILE) return;
		
		menu.appendToGroup(groupId, openInCompareAction);
		
		if(!((resource.exists()))) {
			return;
		}
		
		if (openFileAction != null) {
			openFileAction.selectionChanged(selection);
			menu.appendToGroup(groupId, openFileAction);
		}
		
		IWorkbenchSite ws = site.getWorkbenchSite();
		if (ws != null) {
			MenuManager submenu =
				new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.openWith")); //$NON-NLS-1$
			submenu.add(new OpenWithMenu(ws.getPage(), resource));
			menu.appendToGroup(groupId, submenu);
		}
	}

	public void openInCompareEditor() {
		openInCompareAction.run();		
	}
}
