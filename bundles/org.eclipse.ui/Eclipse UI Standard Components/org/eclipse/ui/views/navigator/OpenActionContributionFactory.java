package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.*;

public class OpenActionContributionFactory extends ActionContributionFactory {

	protected OpenFileAction openFileAction;
	protected OpenSystemEditorAction openSystemEditorAction;
	protected IWorkbenchPartSite site;
	protected Shell shell;

	public OpenActionContributionFactory(
		IWorkbenchPartSite partSite,
		Shell shell) {
		site = partSite;
	}

	/*
	 * @see ActionContributionFactory#updateActions(IStructuredSelection)
	 */
	public void updateActions(IStructuredSelection selection) {
		openFileAction.selectionChanged(selection);
		openSystemEditorAction.selectionChanged(selection);
	}

	/*
	 * @see ActionContributionFactory#makeActions()
	 */
	public void makeActions() {
		openFileAction = new OpenFileAction(site.getPage());
		openSystemEditorAction = new OpenSystemEditorAction(site.getPage());

	}

	/*
	 * @see ActionContributionFactory#fillMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillMenu(IMenuManager menu, IStructuredSelection selection) {

		boolean anyResourceSelected =
			!selection.isEmpty()
				&& SelectionUtil.allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		boolean onlyFilesSelected =
			!selection.isEmpty()
				&& SelectionUtil.allResourcesAreOfType(selection, IResource.FILE);

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
	protected void fillOpenToMenu(
		IMenuManager menu,
		IStructuredSelection selection) {
		// If one file is selected get it.
		// Otherwise, do not show the "open with" menu.
		if (selection.size() != 1)
			return;
		IAdaptable element = (IAdaptable) selection.getFirstElement();
		if (!(element instanceof IContainer))
			return;

		// Create a menu flyout.
		MenuManager submenu =
			new MenuManager(
				ResourceNavigatorMessages.getString("ResourceNavigator.openPerspective"));
		//$NON-NLS-1$
		submenu.add(new OpenPerspectiveMenu(site.getWorkbenchWindow(), element));
		menu.add(submenu);

	}
	/**
	 * Add "open with" actions to the context sensitive menu.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	protected void fillOpenWithMenu(
		IMenuManager menu,
		IStructuredSelection selection) {

		// If one file is selected get it.
		// Otherwise, do not show the "open with" menu.
		if (selection.size() != 1)
			return;

		Object element = selection.getFirstElement();
		if (!(element instanceof IFile))
			return;

		// Create a menu flyout.
		MenuManager submenu =
			new MenuManager(
				ResourceNavigatorMessages.getString("ResourceNavigator.openWith"));
		//$NON-NLS-1$
		submenu.add(new OpenWithMenu(site.getPage(), (IFile) element));

		// Add the submenu.
		menu.add(submenu);
	}

	/**
	 * Add in a mouse listener for any actions that listen
	 * to the mouse events.
	 */

	protected void addMouseListeners(StructuredViewer viewer) {

		viewer.addDoubleClickListener(openFileAction);
	}
}