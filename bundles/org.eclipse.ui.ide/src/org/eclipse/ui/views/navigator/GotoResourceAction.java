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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * Implements the go to resource action. Opens a dialog and set the navigator
 * selection with the resource selected by the user.
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
public class GotoResourceAction extends ResourceNavigatorAction {
	/**
	 * Creates a new instance of the class.
	 *
	 * @param navigator the navigator
	 * @param label     the label
	 * @since 2.0
	 */
	public GotoResourceAction(IResourceNavigator navigator, String label) {
		super(navigator, label);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, INavigatorHelpContextIds.GOTO_RESOURCE_ACTION);
	}

	/**
	 * Collect all resources in the workbench open a dialog asking the user to
	 * select a resource and change the selection in the navigator.
	 */
	@Override
	public void run() {
		IContainer container = (IContainer) getViewer().getInput();
		GotoResourceDialog dialog = new GotoResourceDialog(getShell(), container,
				IResource.FILE | IResource.FOLDER | IResource.PROJECT);
		dialog.open();
		Object[] result = dialog.getResult();
		if (result == null || result.length == 0 || result[0] instanceof IResource == false) {
			return;
		}

		IResource selection = (IResource) result[0];
		getViewer().setSelection(new StructuredSelection(selection), true);
	}
}
