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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.registry.IViewDescriptor;

/**
 * Show a View.
 */
public class ShowViewAction extends Action {
	private IWorkbenchWindow window;
	private IViewDescriptor desc;
/**
 * ShowViewAction constructor comment.
 */
protected ShowViewAction(IWorkbenchWindow window, IViewDescriptor desc) {
	super(""); //$NON-NLS-1$
	String accel = desc.getAccelerator();
	String label = desc.getLabel();
	setText(accel == null ? label : label + "@" + accel); //$NON-NLS-1$
	setImageDescriptor(desc.getImageDescriptor());
	setToolTipText(label);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SHOW_VIEW_ACTION);
	this.window = window;
	this.desc = desc;
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {
	IWorkbenchPage page = window.getActivePage();
	if (page != null) {
		try {
			page.showView(desc.getID());
		} catch (PartInitException e) {
			ErrorDialog.openError(window.getShell(), WorkbenchMessages.getString("ShowView.errorTitle"), //$NON-NLS-1$
				e.getMessage(),e.getStatus());
		}
	}
}
}
