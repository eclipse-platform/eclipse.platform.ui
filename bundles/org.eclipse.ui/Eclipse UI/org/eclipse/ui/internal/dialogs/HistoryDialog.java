package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchHistory;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;


public class HistoryDialog extends SelectionDialog {

	private WorkbenchWindow window;
	private Table historyItems;
	private WorkbenchHistory.WorkbenchHistoryItem selection = null;
	
/**
 * Constructor for HistoryDialog
 */
public HistoryDialog(WorkbenchWindow window) {
	super(window.getShell());
	this.window = window;
	setTitle(WorkbenchMessages.getString("HistoryDialog.title")); //$NON-NLS-1$
	setShellStyle(getShellStyle() | SWT.RESIZE);
}

/**
 * Creates the contents of this dialog, initializes the
 * listener and the update thread.
 */
protected Control createDialogArea(Composite parent) {
	
	Composite dialogArea = (Composite)super.createDialogArea(parent);

	Label l = new Label(dialogArea,SWT.NONE);
	l.setText(WorkbenchMessages.getString("HistoryDialog.label")); //$NON-NLS-1$
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);

	historyItems = new Table(dialogArea,SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = 15 * historyItems.getItemHeight();
	historyItems.setLayoutData(data);
		
	Workbench wb = (Workbench)window.getWorkbench();
	WorkbenchHistory.WorkbenchHistoryItem items[] = wb.getWorkbenchHistory().getItems();
	for(int i=0;i<items.length;i++) {
		TableItem tableItem = new TableItem(historyItems,SWT.NULL);
		tableItem.setText(items[i].getLabel());
		tableItem.setImage(items[i].getImage());
		tableItem.setData(items[i]);
	}
			
	historyItems.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e) {
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			okPressed();
		}
	});
	return dialogArea;
}
/**
 * Notifies that the ok button of this dialog has been pressed.
 */
protected void okPressed() {
	if(historyItems != null || !(historyItems.isDisposed())) {
		TableItem items[] = historyItems.getSelection();
		if(items.length > 0)
			selection = (WorkbenchHistory.WorkbenchHistoryItem)items[0].getData();
	} 
	super.okPressed();
}

/**
 * Returns the item selected by the user or null if the
 * cancel was pressed.
 */
public WorkbenchHistory.WorkbenchHistoryItem getSelection() {
	return selection;
}
}

