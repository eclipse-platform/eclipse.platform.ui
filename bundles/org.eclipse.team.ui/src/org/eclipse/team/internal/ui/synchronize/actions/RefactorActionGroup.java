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

import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

/**
 * This action group is modeled after the class of the same name in 
 * the org.eclipse.ui.workbench plugin. We couldn't reuse that class
 * because of a hard dependency on the navigator.
 */
public class RefactorActionGroup extends ActionGroup {
    
	private CopyToClipboardAction copyAction;
	private MoveResourceAction moveAction;
	private RenameResourceAction renameAction;
	private ISynchronizePageSite site;
	private DeleteResourceAction deleteAction;
	
	public RefactorActionGroup(ISynchronizePageSite site) {
		this.site = site;
		makeActions();
	}

	public void fillContextMenu(IMenuManager parentMenu, String groupId) {

		final MenuManager menu = new MenuManager(Policy.bind("RefactorActionGroup.0")); //$NON-NLS-1$

		final IStructuredSelection selection= getSelection();
		final boolean anyResourceSelected =	!selection.isEmpty() && allResourcesAreOfType(selection, IResource.PROJECT | IResource.FOLDER | IResource.FILE);

		// Actions can work on non-resource selections
		copyAction.selectionChanged(getObjectSelection());
		menu.add(copyAction);
		
		if (anyResourceSelected) {		    
			deleteAction.selectionChanged(selection);
			moveAction.selectionChanged(selection);
			renameAction.selectionChanged(selection);
			menu.add(deleteAction);
			menu.add(moveAction);
			menu.add(renameAction);
		}
		parentMenu.appendToGroup(groupId, menu);
	}

 	public void fillActionBars(IActionBars actionBars) {
    	actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
    	actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
    	actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameAction);
    	actionBars.setGlobalActionHandler(ActionFactory.MOVE.getId(), moveAction);
    }

    public void updateActionBars() {
        final IStructuredSelection structuredSelection= getSelection();
    	copyAction.selectionChanged(getObjectSelection());
    	deleteAction.selectionChanged(structuredSelection);
    	moveAction.selectionChanged(structuredSelection);
    	renameAction.selectionChanged(structuredSelection);
    }

    protected void makeActions() {
    		
        final Shell shell = site.getShell();
        final ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        
        copyAction= new CopyToClipboardAction(shell);
        moveAction= new MoveResourceAction(shell);
        renameAction= new RenameResourceAction(shell);
        deleteAction = new DeleteResourceAction(shell) {
            protected List getSelectedResources() {
                return getSelection().toList();//Arrays.asList(Utils.getResources(getSelection().toArray()));
            }
        };
        
        copyAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        copyAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        
        deleteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        deleteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
    }
    
    private IStructuredSelection getSelection() {
        final ISelection selection= getContext().getSelection();

        if (!(selection instanceof IStructuredSelection)) 
            return new StructuredSelection();

    	return new StructuredSelection(Utils.getResources(((IStructuredSelection)selection).toArray()));
	}
    
    private IStructuredSelection getObjectSelection() {
        final ISelection selection= getContext().getSelection();

        if (!(selection instanceof IStructuredSelection)) 
            return new StructuredSelection();

    	return (IStructuredSelection)selection;
	}

	private boolean allResourcesAreOfType(IStructuredSelection selection, int resourceMask) {
		Iterator resources = selection.iterator();
		while (resources.hasNext()) {
			Object next = resources.next();
			IResource resource = null;
			if (next instanceof IResource) {
				resource = (IResource)next;
			} else if (next instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)next;
				resource = (IResource)adaptable.getAdapter(IResource.class);
			}
			if(resource == null) {
				IResource[] r = Utils.getResources(new Object[] {next});
				if(r.length == 1) {
					resource = r[0];
				}
			}
			if (resource == null || (resource.getType() & resourceMask) == 0) {
				return false;
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		super.dispose();
		copyAction.dispose();
	}
}
