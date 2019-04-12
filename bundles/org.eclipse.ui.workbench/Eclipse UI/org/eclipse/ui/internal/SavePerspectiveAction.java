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
package org.eclipse.ui.internal;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 * Action to save the layout of the active perspective.
 */
public class SavePerspectiveAction extends PerspectiveAction {

	/**
	 * Creates an instance of this class.
	 *
	 * @param window the workbench window in which this action appears
	 */
	public SavePerspectiveAction(IWorkbenchWindow window) {
		super(window);
		setText(WorkbenchMessages.SavePerspective_text);
		setActionDefinitionId(IWorkbenchCommandConstants.WINDOW_SAVE_PERSPECTIVE_AS);
		// @issue missing action id
		setToolTipText(WorkbenchMessages.SavePerspective_toolTip);
		window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_ACTION);
	}

	@Override
	protected void run(IWorkbenchPage page, IPerspectiveDescriptor persp) {
		PerspectiveDescriptor desc = (PerspectiveDescriptor) persp;
		if (desc != null) {
			// saveNonSingleton(page, desc);
		}
	}

}
