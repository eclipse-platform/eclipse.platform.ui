package org.eclipse.ui.externaltools.internal.ant.view.actions;

/**********************************************************************
Copyright (c) 2003 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*********************************************************************/

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

public class ShowTargetViewerAction extends Action {

	private AntView antView;

	public ShowTargetViewerAction(AntView view) {
		super("Show Activated Target View");
		setAntView(view);
		setToolTipText("Show Activated Target View"); 
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DETAIL_PANE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DETAIL_PANE));
		setId(IExternalToolConstants.PLUGIN_ID + ".ShowTargetViewerAction"); 
		//WorkbenchHelp.setHelp(this, IDebugHelpContextIds.SHOW_DETAIL_PANE_ACTION);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		//getAntView().toggleTargetViewer(isChecked());
	}

	/**
	 * Ask the VariablesView to toggle its detail pane and reset this action's
	 * tooltip as appropriate.
	 */
	private void toggleDetailPane(boolean on) {
		getAntView().toggleTargetViewer(on);
	}

	/**
	 * @see Action#setChecked(boolean)
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		toggleDetailPane(value);
	}
	
	protected AntView getAntView() {
		return antView;
	}

	protected void setAntView(AntView antView) {
		this.antView = antView;
	}
}