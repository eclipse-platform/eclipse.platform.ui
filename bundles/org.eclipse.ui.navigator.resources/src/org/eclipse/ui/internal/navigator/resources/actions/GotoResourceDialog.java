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
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.internal.navigator.INavigatorHelpContextIds;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;

/**
 * Shows a list of resources to the user with a text entry field for a string
 * pattern used to filter the list of resources.
 */
/* package */class GotoResourceDialog extends FilteredResourcesSelectionDialog {

	/**
	 * Creates a new instance of the class.
	 */
	protected GotoResourceDialog(Shell parentShell, IContainer container,
			int typesMask) {
		super(parentShell, false, container, typesMask);
		setTitle(WorkbenchNavigatorMessages.GotoResourceDialog_GoToTitle);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parentShell,
				INavigatorHelpContextIds.GOTO_RESOURCE_DIALOG);
	}
}
