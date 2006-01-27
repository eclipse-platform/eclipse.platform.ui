package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonActionProviderConfig;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.navigator.internal.AdaptabilityUtility;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;

/**
 * Provides the open and open with menus for IResources.
 * 
 * @since 3.2
 * 
 */
public class OpenActionProvider extends CommonActionProvider {

	private OpenFileAction openFileAction;

	private ICommonViewerWorkbenchSite viewSite = null;

	private boolean contribute = false;

	public void init(CommonActionProviderConfig aConfig) {
		if (aConfig.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			viewSite = (ICommonViewerWorkbenchSite) aConfig.getViewSite();
			openFileAction = new OpenFileAction(viewSite.getPage());
			contribute = true;
		}
	}

	public void fillContextMenu(IMenuManager aMenu) {
		if (!contribute || getContext().getSelection().isEmpty())
			return;

		IStructuredSelection selection = (IStructuredSelection) getContext()
				.getSelection();

		openFileAction.selectionChanged(selection);
		if (openFileAction.isEnabled())
			aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openFileAction);
		addOpenWithMenu(aMenu);
	}

	public void fillActionBars(IActionBars theActionBars) {
		if (!contribute)
			return;
		IStructuredSelection selection = (IStructuredSelection) getContext()
				.getSelection();
		if (selection.size() == 1
				&& selection.getFirstElement() instanceof IFile) {
			openFileAction.selectionChanged(selection);
			theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
					openFileAction);
		}

	}

	private void addOpenWithMenu(IMenuManager aMenu) {
		IStructuredSelection ss = (IStructuredSelection) getContext()
				.getSelection();

		if (ss == null || ss.size() != 1)
			return;

		Object o = ss.getFirstElement();

		// first try IResource
		IAdaptable openable = (IAdaptable) AdaptabilityUtility.getAdapter(o,
				IResource.class);
		// otherwise try ResourceMapping
		if (openable == null)
			openable = (IAdaptable) AdaptabilityUtility.getAdapter(o,
					ResourceMapping.class);
		// verify the resource is an IFile
		else if (((IResource) openable).getType() != IResource.FILE)
			openable = null;

		if (openable != null) {
			// Create a menu flyout.
			IMenuManager submenu = new MenuManager(
					WorkbenchNavigatorMessages.OpenWithMenu_label,
					ICommonMenuConstants.GROUP_OPEN_WITH);
			submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
			submenu.add(new OpenWithMenu(viewSite.getPage(), openable)); 
			submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_ADDITIONS));

			// Add the submenu.
			if (submenu.getItems().length > 2 && submenu.isEnabled())
				aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH,
						submenu);
		}
	}

}
