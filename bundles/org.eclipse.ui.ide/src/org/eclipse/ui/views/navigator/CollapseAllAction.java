/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation
 *   Sebastian Davids <sdavids@gmx.de> - Collapse all action (25826)
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.CollapseAllHandler;

/**
 * Collapse all project nodes.
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
public class CollapseAllAction extends ResourceNavigatorAction {

	/**
	 * Creates the action.
	 *
	 * @param navigator the resource navigator
	 * @param label     the label for the action
	 */
	public CollapseAllAction(IResourceNavigator navigator, String label) {
		super(navigator, label);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, INavigatorHelpContextIds.COLLAPSE_ALL_ACTION);
		setEnabled(true);
		setActionDefinitionId(CollapseAllHandler.COMMAND_ID);
	}

	/*
	 * Implementation of method defined on <code>IAction</code>.
	 */
	@Override
	public void run() {
		getNavigator().getViewer().collapseAll();
	}
}
