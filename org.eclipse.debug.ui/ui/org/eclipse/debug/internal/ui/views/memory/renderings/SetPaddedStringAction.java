/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action for setting the padded string preference
 * @since 3.1
 */
public class SetPaddedStringAction implements IViewActionDelegate {

	public void init(IViewPart view) {

	}

	public void run(IAction action) {
		Shell shell = DebugUIPlugin.getShell();
		SetPaddedStringDialog dialog = new SetPaddedStringDialog(shell);
		dialog.open();		

	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
