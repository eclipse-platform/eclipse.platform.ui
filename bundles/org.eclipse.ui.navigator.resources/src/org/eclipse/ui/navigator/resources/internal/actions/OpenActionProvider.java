package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonActionProviderConfig;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.internal.AdaptabilityUtility;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;

/**
 * Provides the open and open with menus for IResources.
 * 
 * @since 3.2
 * 
 */
public class OpenActionProvider extends CommonActionProvider   {

	private OpenFileAction openFileAction;
 
	private IWorkbenchPartSite viewSite = null;

	public void init(CommonActionProviderConfig aConfig) {
		viewSite = aConfig.getViewSite();
		openFileAction = new OpenFileAction(viewSite.getPage());
	}
 

	public void fillContextMenu(IMenuManager aMenu) {
		openFileAction.selectionChanged((IStructuredSelection) getContext()
				.getSelection());
		if (openFileAction.isEnabled()) {
			aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openFileAction);
			addOpenWithMenu(aMenu);
		}
	}

	public void fillActionBars(IActionBars theActionBars) {
		IStructuredSelection selection = (IStructuredSelection) getContext()
		.getSelection();
		if(selection.size() == 1 && selection.getFirstElement() instanceof IFile) { 
			openFileAction.selectionChanged(selection);
			theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openFileAction);
		} 
	
	}

	private void addOpenWithMenu(IMenuManager aMenu) {
		IStructuredSelection ss = (IStructuredSelection) getContext().getSelection();

		if (ss == null || ss.size() != 1)
			return;

		Object o = ss.getFirstElement();

		IResource resource = (IResource) AdaptabilityUtility.getAdapter(o,
				IResource.class);

		// Create a menu flyout.
		IMenuManager submenu = new MenuManager(
				WorkbenchNavigatorMessages.OpenWithMenu_label,
				ICommonMenuConstants.GROUP_OPEN_WITH);
		submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
		if (resource != null && !(resource instanceof IProject)
				&& !(resource instanceof IFolder))
			submenu.add(new OpenWithMenu(viewSite.getPage(), resource));

		submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_ADDITIONS));

		// Add the submenu.
		if (submenu.getItems().length > 2 && submenu.isEnabled())
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
	}

}
