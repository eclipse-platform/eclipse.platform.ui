package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.*;

/**
 * This is the abstract superclass for NextEditorAction and PrevEditorAction.
 */
public class CycleEditorAction extends CyclePartAction {
	
/**
 * Creates a CycleEditorAction.
 */
protected CycleEditorAction(IWorkbenchWindow window, boolean forward) {
	super(window,forward); //$NON-NLS-1$
	window.getPartService().addPartListener(this);
	updateState();
}

protected void setText() {
	// TBD: Remove text and tooltip when this becomes an invisible action.
	if (forward) {
		setText(WorkbenchMessages.getString("CycleEditorAction.next.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CycleEditorAction.next.toolTip")); //$NON-NLS-1$
	}
	else {
		setText(WorkbenchMessages.getString("CycleEditorAction.prev.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CycleEditorAction.prev.toolTip")); //$NON-NLS-1$
	}
}

/**
 * Updates the enabled state.
 */
public void updateState() {
	IWorkbenchPage page = getActivePage();
	if (page == null) {
		setEnabled(false);
		return;
	}
	// enable iff there is at least one other editor to switch to
	setEnabled(page.getEditors().length >= 2);
}

/**
 * Add all views to the dialog in the activation order
 */
protected void addItems(Table table,WorkbenchPage page) {
	IEditorPart parts[] = page.getSortedEditors();
	for (int i = parts.length - 1; i >= 0 ; i--) {
		TableItem item  = null;
		if(parts[i] instanceof IEditorPart) {
			item = new TableItem(table,SWT.NONE);
			if(parts[i].isDirty())
				item.setText("*" + parts[i].getTitle()); //$NON-NLS-1$
			else
				item.setText(parts[i].getTitle());
			item.setImage(parts[i].getTitleImage());
			item.setData(parts[i]);
		}
	}
}
/**
 * Returns the string which will be shown in the table header.
 */ 
protected String getTableHeader() {
	return WorkbenchMessages.getString("CycleEditorAction.header"); //$NON-NLS-1$
}
}
