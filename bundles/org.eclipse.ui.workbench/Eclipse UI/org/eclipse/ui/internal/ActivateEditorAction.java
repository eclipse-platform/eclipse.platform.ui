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


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Activates the most recently used editor in the current window.
 */
public class ActivateEditorAction extends PageEventAction {

	private int accelerator;
	
/**
 * Creates an ActivateEditorAction.
 */
protected ActivateEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("ActivateEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ActivateEditorAction.toolTip")); //$NON-NLS-1$
	window.getPartService().addPartListener(this);
	updateState();
	WorkbenchHelp.setHelp(this, IHelpContextIds.ACTIVATE_EDITOR_ACTION);
}

public void pageActivated(IWorkbenchPage page) {
	super.pageActivated(page);
	updateState();
}

public void pageClosed(IWorkbenchPage page) {
	super.pageClosed(page);
	updateState();
}


/**
 * @see Action#run()
 */
public void runWithEvent(Event e) {
	accelerator = e.detail;
	WorkbenchPage page = (WorkbenchPage)getActivePage();
	if (page != null) {
		IEditorPart part = page.getActiveEditor(); // may not actually be active
		if (part != null) {
			page.activate(part);
		} else {
			IWorkbenchPartReference ref = page.getActivePartReference();
			if(ref instanceof IViewReference) {
				if(page.isFastView((IViewReference)ref))
					page.toggleFastView((IViewReference)ref);
			}
		}
	}
}

/**
 * Updates the enabled state.
 */
public void updateState() {
	IWorkbenchPage page = getActivePage();
	setEnabled(page != null);
}

public int getAccelerator() {
	int accelerator = this.accelerator;
	accelerator = accelerator & ~ SWT.CTRL;
	accelerator = accelerator & ~ SWT.SHIFT;
	accelerator = accelerator & ~ SWT.ALT;
	return accelerator;
}
}

