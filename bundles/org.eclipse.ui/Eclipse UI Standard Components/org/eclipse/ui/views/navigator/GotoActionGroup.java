package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.views.framelist.*;

/**
 * This is the action group for the goto actions.
 */
public class GotoActionGroup extends ActionGroup {

	private IResourceNavigatorPart navigator;
	private BackAction backAction;
	private ForwardAction forwardAction;
	private GoIntoAction goIntoAction;
	private UpAction upAction;
	private GotoResourceAction gotoResourceAction;

	public GotoActionGroup(IResourceNavigatorPart navigator) {
		this.navigator = navigator;
		makeActions();
	}

	private void makeActions() {
		FrameList frameList = navigator.getFrameList();
		goIntoAction = new GoIntoAction(frameList);
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);
		gotoResourceAction =
			new GotoResourceAction(
				navigator,
				ResourceNavigatorMessages.getString("ResourceNavigator.resourceText")); //$NON-NLS-1$
	}

	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1
			&& ResourceSelectionUtil.allResourcesAreOfType(
				selection,
				IResource.PROJECT | IResource.FOLDER)) {
			menu.add(goIntoAction);
		}
		MenuManager gotoMenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.goto")); //$NON-NLS-1$
		menu.add(gotoMenu);
		gotoMenu.add(backAction);
		gotoMenu.add(forwardAction);
		gotoMenu.add(upAction);
		gotoMenu.add(gotoResourceAction);
	}
	
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.GO_INTO,
			goIntoAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.BACK,
			backAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.FORWARD,
			forwardAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.UP,
			upAction);
			
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
	}
	
	public void updateActionBars() {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		goIntoAction.setEnabled(selection.size() == 1
			&& ResourceSelectionUtil.allResourcesAreOfType(
				selection,
				IResource.PROJECT | IResource.FOLDER));
		// the rest of the actions update by listening to frame list changes
	}
}