package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.VariablesView;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

public class ShowDetailPaneAction extends Action {

	private VariablesView fVariablesView;

	public ShowDetailPaneAction(VariablesView view) {
		super(ActionMessages.getString("ShowDetailPaneAction.Show_Variable_Detail_Pane_1")); //$NON-NLS-1$
		setVariablesView(view);
		setToolTipText(ActionMessages.getString("ShowDetailPaneAction.Show_detail_pane_2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE));
		setId(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier() + ".ShowDetailPaneAction"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.SHOW_DETAIL_PANE_ACTION);
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
		getVariablesView().toggleDetailPane(on);
		setToolTipText(on ? ActionMessages.getString("ShowDetailPaneAction.Hide_detail_pane_3") : ActionMessages.getString("ShowDetailPaneAction.Show_detail_pane_2")); //$NON-NLS-2$ //$NON-NLS-1$
	}

	/**
	 * @see Action#setChecked(boolean)
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		toggleDetailPane(value);
	}
	
	protected VariablesView getVariablesView() {
		return fVariablesView;
	}

	protected void setVariablesView(VariablesView variablesView) {
		fVariablesView = variablesView;
	}
}

