package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2003 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*********************************************************************/

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

public class ShowDetailPaneAction extends Action {

	private VariablesView fVariablesView;

	public ShowDetailPaneAction(VariablesView view) {
		super(ActionMessages.getString("ShowDetailPaneAction.Show_Variable_Detail_Pane_1"), Action.AS_CHECK_BOX); //$NON-NLS-1$
		setVariablesView(view);
		setToolTipText(ActionMessages.getString("ShowDetailPaneAction.Show_detail_pane_2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DETAIL_PANE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DETAIL_PANE));
		setId(DebugUIPlugin.getUniqueIdentifier() + ".ShowDetailPaneAction"); //$NON-NLS-1$
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
	private void toggleDetailPane(boolean on) {
		getVariablesView().toggleDetailPane(on);
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

