package org.eclipse.debug.internal.ui.views.variables;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.action.IAction;

/**
 * An action delegate that toggles the state of its viewer to
 * use word wrap in the details pane.
 */
public class DetailPaneWordWrapAction extends VariableViewToggleAction {
	
	/**
	 * @see VariableFilterAction#getPreferenceKey()
	 */
	protected String getPreferenceKey() {
		return IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP; 
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		super.run(action);
		((VariablesView)getView()).toggleDetailPaneWordWrap(action.isChecked());
	}	
}