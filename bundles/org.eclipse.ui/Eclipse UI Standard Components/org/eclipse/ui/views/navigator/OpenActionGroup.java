package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.*;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.*;

/**
 * This is the action group for the open actions.
 */
public class OpenActionGroup extends ActionGroup {

	private IResourceNavigatorPart navigator;
	private OpenFileAction openFileAction;

	/**
	 * The id for the Open With submenu.
	 */
	public static final String OPEN_WITH_ID = PlatformUI.PLUGIN_ID + ".OpenWithSubMenu"; //$NON-NLS-1$

	public OpenActionGroup(IResourceNavigatorPart navigator) {
		this.navigator = navigator;
		makeActions();
	}

	private void makeActions() {
		openFileAction = new OpenFileAction(navigator.getSite().getPage());
	}

	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		boolean anyResourceSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		boolean onlyFilesSelected =
			!selection.isEmpty() && ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FILE);

		if (onlyFilesSelected) {
			openFileAction.selectionChanged(selection);
			menu.add(openFileAction);
			fillOpenWithMenu(menu, selection);
		}

		if (anyResourceSelected) {
			addNewWindowAction(menu, selection);
		}
	}

	/**
	 * Adds the OpenWith submenu to the context menu.
	 * 
	 * @param menu the context menu
	 * @param selection the current selection
	 */
	private void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {

		// Only supported if exactly one file is selected.
		if (selection.size() != 1)
			return;
		Object element = selection.getFirstElement();
		if (!(element instanceof IFile))
			return;

		MenuManager submenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.openWith"), OPEN_WITH_ID); //$NON-NLS-1$
		submenu.add(new OpenWithMenu(navigator.getSite().getPage(), (IFile) element));
		menu.add(submenu);
	}

	/**
	 * Adds the Open in New Window action to the context menu.
	 * 
	 * @param menu the context menu
	 * @param selection the current selection
	 */
	private void addNewWindowAction(IMenuManager menu, IStructuredSelection selection) {

		// Only supported if exactly one container (i.e project or folder) is selected.
		if (selection.size() != 1)
			return;
		Object element = selection.getFirstElement();
		if (!(element instanceof IContainer))
			return;

		menu.add(new OpenInNewWindowAction(navigator.getSite().getWorkbenchWindow(), (IContainer) element));
	}

	/**
	 * Runs the default action (open file).
	 */
	public void runDefaultAction(IStructuredSelection selection) {
		Object element = selection.getFirstElement();
		if (element instanceof IFile) {
			openFileAction.selectionChanged(selection);
			openFileAction.run();
		}
	}
}