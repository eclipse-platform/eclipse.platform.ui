/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.adaptable;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;

public class TestNavigatorActionGroup extends ActionGroup {

	private final AdaptedResourceNavigator navigator;

	private AddBookmarkAction addBookmarkAction;

	private PropertyDialogAction propertyDialogAction;


	public TestNavigatorActionGroup(AdaptedResourceNavigator navigator) {
		this.navigator = navigator;
	}

	protected void makeActions() {
		Shell shell = navigator.getSite().getShell();
		addBookmarkAction = new AddBookmarkAction(navigator.getSite(), true);
		propertyDialogAction = new PropertyDialogAction(shell, navigator
				.getViewer());
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext()
				.getSelection();

		MenuManager newMenu = new MenuManager(ResourceNavigatorMessages.ResourceNavigator_new);
		menu.add(newMenu);
		newMenu.add(new NewWizardMenu(navigator.getSite().getWorkbenchWindow()));

		//Update the selections of those who need a refresh before filling
		addBookmarkAction.selectionChanged(selection);
		menu.add(addBookmarkAction);

		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu
				.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
						+ "-end")); //$NON-NLS-1$
		menu.add(new Separator());

		propertyDialogAction.selectionChanged(selection);
		if (propertyDialogAction.isApplicableForSelection()) {
			menu.add(propertyDialogAction);
		}
	}
}
