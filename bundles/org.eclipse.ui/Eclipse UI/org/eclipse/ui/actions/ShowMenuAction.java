package org.eclipse.ui.actions;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Show the menu on top of the icon in the
 * view or editor label.
 */
public class ShowMenuAction extends PartEventAction {

/**
 * Constructor for ShowMenuAction.
 * @param text
 */
public ShowMenuAction(WorkbenchWindow window) {
	super("");
	setText(WorkbenchMessages.getString("ShowMenuAction.text"));
	setToolTipText(WorkbenchMessages.getString("ShowMenuAction.toolTip"));
	window.getPartService().addPartListener(this);
}
/**
 * See Action
 */
public void run() {
	WorkbenchPart part = (WorkbenchPart)getActivePart();
	if(part != null)
		((PartSite)part.getSite()).getPane().showPaneMenu();
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
/**
 * Updates the enabled state.
 */
private void updateState() {
	WorkbenchPart part = (WorkbenchPart)getActivePart();
	setEnabled(part != null);
}
}

