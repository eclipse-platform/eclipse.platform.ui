package org.eclipse.ui.views.navigator;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved. This file is made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/cpl-v10.
 * html
 */
import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;

/**
 * Implements the go to resource navigate action. 
 * Opens a dialog and sets the navigator selection with the resource selected by
 * the user. The navigator view is made visible if it is not visible already. A
 * navigator view is opened if it is not open.
 * 
 * @since 2.1
 */
public class GoToResourceAction2 extends Action implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow workbenchWindow;
	
/**
 * Creates a new instance of the class.
 */
public GoToResourceAction2() {
	super();
	WorkbenchHelp.setHelp(this, INavigatorHelpContextIds.GOTO_RESOURCE_ACTION);
}
/**
 * Returns the resource navigator to operate on.
 * A navigator view is opened if it is not open already.
 * 
 * @return the resource navigator to operate on. */
private IResourceNavigator getNavigator() {
	IWorkbenchPage activePage = workbenchWindow.getActivePage();
	IViewPart view = activePage.findView(IPageLayout.ID_RES_NAV);
	IResourceNavigator navigator = null;
	
	if (view == null) {
		try {
			view = activePage.showView(IPageLayout.ID_RES_NAV);
		} catch (PartInitException exception) {
			DialogUtil.openError(
				workbenchWindow.getShell(),
				WorkbenchMessages.getString("GoToResource.showNavError"), //$NON-NLS-1$
				exception.getMessage(),
				exception);
		}	
	}
	if (view instanceof IResourceNavigator) {
		navigator = (IResourceNavigator) view;
	}
	return navigator;
}
/**
 * Collect all resources in the workbench and add them to the <code>resources</code>
 * list.
 */
private void collectAllResources(IContainer container,ArrayList resources,ResourcePatternFilter filter) {
	try {
		IResource members[] = container.members();
		for (int i = 0; i < members.length; i++){
			IResource r = members[i];
			if(filter.select(getNavigator().getViewer(),null,r))
				resources.add(r);
			if(r.getType() != IResource.FILE)
				collectAllResources((IContainer)r,resources,filter);
		}
	} catch (CoreException e) {
	}
}
/**
 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
 */
public void dispose() {
	// do nothing
}
/**
 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
 */
public void init(IWorkbenchWindow window) {
	workbenchWindow = window;
}
/**
 * Collect all resources in the workbench, open a dialog asking the user to
 * select a resource and change the selection in the navigator.
 */
public void run(IAction action) {
	IResourceNavigator navigator = getNavigator();
	
	if (navigator == null) 
		return;
		
	IContainer cont = (IContainer)navigator.getViewer().getInput();
	ArrayList resources = new ArrayList();
	collectAllResources(cont, resources, navigator.getPatternFilter());
	IResource resourcesArray[] = new IResource[resources.size()];
	resources.toArray(resourcesArray);
	GotoResourceDialog dialog = new GotoResourceDialog(workbenchWindow.getShell(), resourcesArray);
 	dialog.open();
	IResource selection = dialog.getSelection();
	if(selection == null)
		return;
	navigator.getViewer().setSelection(new StructuredSelection(selection), true);
	
	IWorkbenchPage activePage = workbenchWindow.getActivePage();
	activePage.activate(navigator);
}

/**
 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
 */
public void selectionChanged(IAction action, ISelection selection) {
	// do nothing
}

}
