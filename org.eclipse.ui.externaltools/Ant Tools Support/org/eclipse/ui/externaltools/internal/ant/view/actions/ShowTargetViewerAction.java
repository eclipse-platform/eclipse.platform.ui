package org.eclipse.ui.externaltools.internal.ant.view.actions;

/**********************************************************************
Copyright (c) 2003 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

public class ShowTargetViewerAction extends Action {

	private AntView antView;

	public ShowTargetViewerAction(AntView view) {
		super(AntViewActionMessages.getString("ShowTargetViewerAction.Show"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_TOGGLE)); //$NON-NLS-1$
		setAntView(view);
		setToolTipText(AntViewActionMessages.getString("ShowTargetViewerAction.Show"));  //$NON-NLS-1$
		setId(IExternalToolConstants.PLUGIN_ID + ".ShowTargetViewerAction");  //$NON-NLS-1$
		//WorkbenchHelp.setHelp(this, IDebugHelpContextIds.SHOW_DETAIL_PANE_ACTION);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		//all the work occurs on setChecked(booelean)
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