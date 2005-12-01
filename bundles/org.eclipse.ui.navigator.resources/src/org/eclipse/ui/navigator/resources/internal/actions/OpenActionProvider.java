package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.internal.AdaptabilityUtility;
import org.eclipse.ui.navigator.internal.actions.CommonActionProvider;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;

/**
 * Provides the open and open with menus for IResources.
 * 
 * @since 3.2
 * 
 */
public class OpenActionProvider extends CommonActionProvider implements
		ICommonActionProvider {

	private OpenFileAction openFileAction;

	private ActionContext context;

	private IWorkbenchPartSite viewSite = null;

	public void init(String anExtensionId, IViewPart aViewPart,
			INavigatorContentService aContentService,
			StructuredViewer aStructuredViewer) {
		viewSite = aViewPart.getSite();
		openFileAction = new OpenFileAction(viewSite.getPage());
	}

	public void setActionContext(ActionContext aContext) {
		context = aContext;
	}

	public boolean fillContextMenu(IMenuManager aMenu) {
		openFileAction.selectionChanged((IStructuredSelection) context
				.getSelection());
		if (openFileAction.isEnabled()) {
			aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openFileAction);
			addOpenWithMenu(aMenu);
			return true;
		}
		return false;
	}

	public boolean fillActionBars(IActionBars theActionBars) {
		IStructuredSelection selection = (IStructuredSelection) context
		.getSelection();
		if(selection.size() == 1 && selection.getFirstElement() instanceof IFile) { 
			openFileAction.selectionChanged(selection);
			theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openFileAction);
			return true;
		}
		return false;
	
	}

	private void addOpenWithMenu(IMenuManager aMenu) {
		IStructuredSelection ss = (IStructuredSelection) context.getSelection();

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
