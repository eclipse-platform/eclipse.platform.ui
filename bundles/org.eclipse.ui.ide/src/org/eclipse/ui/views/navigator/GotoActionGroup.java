/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.views.framelist.BackAction;
import org.eclipse.ui.views.framelist.ForwardAction;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.framelist.GoIntoAction;
import org.eclipse.ui.views.framelist.UpAction;

/**
 * This is the action group for the goto actions.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
public class GotoActionGroup extends ResourceNavigatorActionGroup {

	private BackAction backAction;

	private ForwardAction forwardAction;

	private GoIntoAction goIntoAction;

	private UpAction upAction;

	private GotoResourceAction goToResourceAction;

	public GotoActionGroup(IResourceNavigator navigator) {
		super(navigator);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1) {
			if (ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FOLDER)) {
				menu.add(goIntoAction);
			} else {
				IStructuredSelection resourceSelection = ResourceSelectionUtil.allResources(selection,
						IResource.PROJECT);
				if (resourceSelection != null && !resourceSelection.isEmpty()) {
					IProject project = (IProject) resourceSelection.getFirstElement();
					if (project.isOpen()) {
						menu.add(goIntoAction);
					}
				}
			}
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.GO_INTO, goIntoAction);
		actionBars.setGlobalActionHandler(ActionFactory.BACK.getId(), backAction);
		actionBars.setGlobalActionHandler(ActionFactory.FORWARD.getId(), forwardAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.UP, upAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.GO_TO_RESOURCE, goToResourceAction);

		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
	}

	@Override
	protected void makeActions() {
		FrameList frameList = navigator.getFrameList();
		goIntoAction = new GoIntoAction(frameList);
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);
		goToResourceAction = new GotoResourceAction(navigator, ResourceNavigatorMessages.GoToResource_label);
	}

	@Override
	public void updateActionBars() {
		ActionContext context = getContext();
		boolean enable = false;

		// Fix for bug 26126. Resource change listener could call
		// updateActionBars without a context being set.
		// This should never happen because resource navigator sets
		// context immediately after this group is created.
		if (context != null) {
			IStructuredSelection selection = (IStructuredSelection) context.getSelection();

			if (selection.size() == 1) {
				Object object = selection.getFirstElement();
				if (object instanceof IProject) {
					enable = ((IProject) object).isOpen();
				} else if (object instanceof IFolder) {
					enable = true;
				}
			}
		}
		goIntoAction.setEnabled(enable);
		// the rest of the actions update by listening to frame list changes
	}
}
