/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.OpenWithMenu;

/**
 * This is the action group for the open actions. It contains open
 * actions for 
 */
public class OpenWithActionGroup extends ActionGroup {

	private OpenFileInSystemEditorAction openFileAction;
	private OpenInCompareAction openInCompareAction;
	private ISynchronizePageSite site;
    private final ISynchronizeParticipant participant;

	public OpenWithActionGroup(ISynchronizePageSite site, ISynchronizeParticipant participant) {
		this.participant = participant;
		this.site = site;
		makeActions();
	}

	protected void makeActions() {
		IWorkbenchSite ws = site.getWorkbenchSite();
		if (ws != null) {
			openFileAction = new OpenFileInSystemEditorAction(ws.getPage());
			openInCompareAction = new OpenInCompareAction(site, participant);
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

        // Only supported if at least one file is selected.
        if (selection == null || selection.size() < 1)
            return;
        Object[] elements = selection.toArray();
        IResource resources[] = Utils.getResources(elements);       
        if(resources.length == 0) {
            return;
        }
        
        for (int i = 0; i < resources.length; i++) {
            if (resources[i].getType() != IResource.FILE) {
                // Only supported if all the items are files.
                return;
            }
        }       
        
        if (resources.length == 1) {
            // Only supported if exactly one file is selected.
            menu.appendToGroup(groupId, openInCompareAction);
        }
        
        for (int i = 0; i < resources.length; i++) {
            if (!((resources[i].exists()))) {
                // Only support non-compare actions if all files exist.
                return;
            }
        }
        
        if (openFileAction != null) {
            openFileAction.selectionChanged(selection);
            menu.appendToGroup(groupId, openFileAction);
        }
        
        if (resources.length == 1) {
            // Only support the "Open With..." submenu if exactly one file is selected.
            IWorkbenchSite ws = site.getWorkbenchSite();
            if (ws != null) {
                MenuManager submenu =
                    new MenuManager(TeamUIMessages.OpenWithActionGroup_0); 
                submenu.add(new OpenWithMenu(ws.getPage(), resources[0]));
                menu.appendToGroup(groupId, submenu);
            }
        }
    }

	public void openInCompareEditor() {
		openInCompareAction.run();		
	}
}
