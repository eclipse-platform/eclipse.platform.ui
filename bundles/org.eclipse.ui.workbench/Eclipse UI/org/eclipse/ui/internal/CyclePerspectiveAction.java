package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.PerspLabelProvider;
/**
 * Implements a action to enable the user switch between perspectives
 * using keyboard.
 */
public class CyclePerspectiveAction extends CyclePartAction {
	private PerspLabelProvider labelProvider = new PerspLabelProvider(false);;
/**
 * Creates a CyclePerspectiveAction.
 */
protected CyclePerspectiveAction(IWorkbenchWindow window, boolean forward) {
	super(window,forward); //$NON-NLS-1$
	window.addPerspectiveListener(new IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			updateState();
		}
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {}
	});
	updateState();
}

protected void setText() {
	// TBD: Remove text and tooltip when this becomes an invisible action.
	if (forward) {
		setText(WorkbenchMessages.getString("CyclePerspectiveAction.next.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CyclePerspectiveAction.next.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_PERSPECTIVE_FORWARD_ACTION);
	}
	else {
		setText(WorkbenchMessages.getString("CyclePerspectiveAction.prev.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CyclePerspectiveAction.prev.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_PERSPECTIVE_BACKWARD_ACTION);
	}
}
/** 
 * Dispose the resources cached by this action.
 */
protected void dispose() {
	labelProvider.dispose();
}
/**
 * Activate the selected item.
 */
public void activate(IWorkbenchPage page, Object selection) {
	if (selection != null) {
		IPerspectiveDescriptor persp = (IPerspectiveDescriptor)selection;
		page.setPerspective(persp);
	}
}
/**
 * Updates the enabled state.
 */
public void updateState() {
	WorkbenchPage page = (WorkbenchPage) getActivePage();
	if (page == null) {
		setEnabled(false);
		return;
	}
	// enable iff there is at least one other editor to switch to
	setEnabled(page.getSortedPerspectives().length >= 1);
}

/**
 * Add all views to the dialog in the activation order
 */
protected void addItems(Table table,WorkbenchPage page) {
	IPerspectiveDescriptor perspectives[] = page.getSortedPerspectives();
	for (int i = perspectives.length - 1; i >= 0 ; i--) {
		TableItem item = new TableItem(table,SWT.NONE);
		IPerspectiveDescriptor desc = perspectives[i];
		item.setText(labelProvider.getText(desc));
		item.setImage(labelProvider.getImage(desc));
		item.setData(desc);
	}
}
/**
 * Returns the string which will be shown in the table header.
 */ 
protected String getTableHeader() {
	return WorkbenchMessages.getString("CyclePerspectiveAction.header"); //$NON-NLS-1$
}
}