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
import org.eclipse.ui.help.*;
import org.eclipse.jface.action.*;

/**
 * Try to quit the application.
 */
public class QuitAction extends Action {
	private IWorkbench workbench;
/**
 * Creates a new <code>QuitAction</code>. The action is
 * initialized from the <code>JFaceResources</code> bundle.
 */
public QuitAction(IWorkbench workbench) {
	setText(WorkbenchMessages.getString("Exit.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("Exit.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.QUIT);
	WorkbenchHelp.setHelp(this, IHelpContextIds.QUIT_ACTION);
	this.workbench = workbench;
}
/**
 * Perform the action: quit the application.
 */
public void run() {
	workbench.close();
}
}
