/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Closes the active editor.
 */
public class CloseEditorAction extends ActiveEditorAction {
/**
 *	Create an instance of this class
 */
public CloseEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("CloseEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("CloseEditorAction.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.CLOSE);
	WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_PART_ACTION);
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	IEditorPart part = getActiveEditor();
	if (part != null)
		getActivePage().closeEditor(part, true);
}
}
