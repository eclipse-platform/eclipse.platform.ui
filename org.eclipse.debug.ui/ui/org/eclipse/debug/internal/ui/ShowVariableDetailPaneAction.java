package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.help.WorkbenchHelp;

public class ShowVariableDetailPaneAction extends Action {

	private VariablesView fVariablesView;

	public ShowVariableDetailPaneAction(VariablesView view) {
		super(DebugUIMessages.getString("ShowVariableDetailPaneAction.Show_Variable_Detail_Pane_1")); //$NON-NLS-1$
		fVariablesView = view;
		setToolTipText(DebugUIMessages.getString("ShowVariableDetailPaneAction.Show_detail_pane_2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.SHOW_DETAIL_PANE_ACTION });
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		toggleDetailPane(isChecked());
	}

	/**
	 * Ask the VariablesView to toggle its detail pane and reset this action's
	 * tooltip as appropriate.
	 */
	private void toggleDetailPane(final boolean on) {
		fVariablesView.toggleDetailPane(on);
		setToolTipText(on ? DebugUIMessages.getString("ShowVariableDetailPaneAction.Hide_detail_pane_3") : DebugUIMessages.getString("ShowVariableDetailPaneAction.Show_detail_pane_4")); //$NON-NLS-2$ //$NON-NLS-1$
	}

	/**
	 * @see Action#setChecked(boolean)
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		toggleDetailPane(value);
	}
}

