package org.eclipse.ui.views.navigator;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.views.framelist.*;

/**
 * This is the action group for the goto actions.
 */
public class GotoActionGroup extends ResourceNavigatorActionGroup {

	private BackAction backAction;
	private ForwardAction forwardAction;
	private GoIntoAction goIntoAction;
	private UpAction upAction;
	private GotoResourceAction gotoResourceAction;

	public GotoActionGroup(IResourceNavigator navigator) {
		super(navigator);
	}

	protected void makeActions() {
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
		if (selection.size() == 1) {
			if (ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FOLDER)) {
				menu.add(goIntoAction);
			} else {
				IStructuredSelection resourceSelection = ResourceSelectionUtil.allResources(selection, IResource.PROJECT);
				if (!resourceSelection.isEmpty()) {
					IProject project = (IProject)resourceSelection.getFirstElement();
					if (project.isOpen())
						menu.add(goIntoAction);
				}
			}
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