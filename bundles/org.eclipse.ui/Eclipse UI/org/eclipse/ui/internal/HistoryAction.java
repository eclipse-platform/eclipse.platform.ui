package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.dialogs.HistoryDialog;

public class HistoryAction extends Action {

	WorkbenchWindow window;
	/**
	 * Constructor for HistoryAction
	 */
	protected HistoryAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("HistoryAction.text")); //$NON-NLS-1$
		this.window = window;
		setToolTipText(WorkbenchMessages.getString("HistoryAction.toolTip")); //$NON-NLS-1$
		setId("org.eclipse.ui.internal.HistoryAction"); //$NON-NLS-1$
	}
	
	/**
	 * @see Action#run()
	 */
	public void run() {
		HistoryDialog dialog = new HistoryDialog(window);
 		dialog.open();
		WorkbenchHistory.WorkbenchHistoryItem selection = dialog.getSelection();
		if(selection == null)
			return;
		selection.open(window.getActivePage());
	}
}

