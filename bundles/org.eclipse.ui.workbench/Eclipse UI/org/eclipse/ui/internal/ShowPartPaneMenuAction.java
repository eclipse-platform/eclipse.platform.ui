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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Show the menu on top of the icon in the
 * view or editor label.
 */
public class ShowPartPaneMenuAction extends PartEventAction {

	private int accelerator;

/**
 * Constructor for ShowPartPaneMenuAction.
 * @param text
 */
public ShowPartPaneMenuAction(WorkbenchWindow window) {
	super(""); //$NON-NLS-1$
	initText();
	window.getPartService().addPartListener(this);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SHOW_PART_PANE_MENU_ACTION);
}
/**
 * Initialize the menu text and tooltip.
 */
protected void initText() {
	setText(WorkbenchMessages.getString("ShowPartPaneMenuAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ShowPartPaneMenuAction.toolTip")); //$NON-NLS-1$
}
/**
 * Show the pane title menu.
 */
protected void showMenu(PartPane pane) {
	pane.showPaneMenu();
}
/**
 * Updates the enabled state.
 */
protected void updateState() {
	setEnabled(getActivePart() != null);
}
/**
 * See Action
 */
public void runWithEvent(Event e) {
	accelerator = e.detail;
	IWorkbenchPart part = getActivePart();
	if(part != null)
		showMenu(((PartSite)part.getSite()).getPane());
}
/**
 * See IPartListener
 */
public void partOpened(IWorkbenchPart part) {
	super.partOpened(part);
	updateState();
}
/**
 * See IPartListener
 */
public void partClosed(IWorkbenchPart part) {
	super.partClosed(part);
	updateState();
}
/**
 * See IPartListener
 */
public void partActivated(IWorkbenchPart part) {
	super.partActivated(part);
	updateState();
}
/**
 * See IPartListener
 */
public void partDeactivated(IWorkbenchPart part) {
	super.partDeactivated(part);
	updateState();
}

public int getAccelerator() {
	int accelerator = this.accelerator;
	accelerator = accelerator & ~ SWT.CTRL;
	accelerator = accelerator & ~ SWT.SHIFT;
	accelerator = accelerator & ~ SWT.ALT;
	return accelerator;
}
}

