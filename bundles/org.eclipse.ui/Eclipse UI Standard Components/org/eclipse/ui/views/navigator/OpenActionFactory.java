package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.*;

/**
 * @since 2.0
 * @deprecated use OpenActionGroup
 */
public class OpenActionFactory extends ActionFactory {

	protected OpenFileAction openFileAction;
	protected OpenSystemEditorAction openSystemEditorAction;
	protected IWorkbenchPartSite site;
	protected Shell shell;
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenWithSubMenu";//$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public OpenActionFactory(
		IWorkbenchPartSite partSite,
		Shell shell) {
		site = partSite;
	}

	/*
	 * @see ActionFactory#makeActions()
	 */
	public void makeActions() {
		openFileAction = new OpenFileAction(site.getPage());
		openSystemEditorAction = new OpenSystemEditorAction(site.getPage());

	}

	/*
	 * @see ActionFactory#fillPopUpMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillPopUpMenu(IMenuManager menu, IStructuredSelection selection) {
		
		selectionChanged(selection);

		boolean anyResourceSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		boolean onlyFilesSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FILE);

		if (onlyFilesSelected)
			menu.add(openFileAction);

		if (anyResourceSelected) {
			fillOpenWithMenu(menu, selection);
			fillOpenToMenu(menu, selection);
		}
	}

	/**
	 * Add "open to" actions to the context sensitive menu.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	protected void fillOpenToMenu(IMenuManager menu, IStructuredSelection selection) {

		// Only supported if one and only one container
		// resource selected (i.e project or folder).		
		if (selection.size() != 1)
			return;
		IAdaptable element = (IAdaptable) selection.getFirstElement();
		if (!(element instanceof IContainer))
			return;

		// Add the open New Window action
		// Create a menu flyout.
		menu.add(new OpenInNewWindowAction(site.getWorkbenchWindow(), element));
	}
	
	/**
	 * Add "open with" actions to the context sensitive menu.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	protected void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {

		// If one file is selected get it.
		// Otherwise, do not show the "Open With" menu.
		if (selection.size() != 1)
			return;

		Object element = selection.getFirstElement();
		if (!(element instanceof IFile))
			return;

		// Create a menu flyout.
		MenuManager submenu =
			new MenuManager(
				ResourceNavigatorMessages.getString("ResourceNavigator.openWith"),ID);//$NON-NLS-1$
		submenu.add(new OpenWithMenu(site.getPage(), (IFile) element));

		// Add the submenu.
		menu.add(submenu);
	}

	
	/**
	 * Handles double clicks in viewer.
	 */
	public void handleDoubleClick(IStructuredSelection selection) {
		openFileAction.handleDoubleClick(selection);

	}
	
	/**
	 * Update the selection for new selection
	 */
	public void selectionChanged(IStructuredSelection selection) {
		//Update the selections of those who need a refresh before filling
		openFileAction.selectionChanged(selection);
		openSystemEditorAction.selectionChanged(selection);
	}
}