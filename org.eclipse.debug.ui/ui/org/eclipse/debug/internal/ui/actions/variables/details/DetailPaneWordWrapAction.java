/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.variables.details;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.PlatformUI;

/**
 * An check box action that allows the word wrap property to be set, determining if the detail pane
 * should wrap text.
 */
public class DetailPaneWordWrapAction extends Action {

	ITextViewer fTextViewer;
	
	public DetailPaneWordWrapAction(ITextViewer textViewer) {
		super(ActionMessages.DetailPaneWordWrapAction_0,IAction.AS_CHECK_BOX);
        
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.DETAIL_PANE_WORD_WRAP_ACTION);
		
		fTextViewer = textViewer;
		setEnabled(true);
		
		boolean prefSetting = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP);
		fTextViewer.getTextWidget().setWordWrap(prefSetting);
		setChecked(prefSetting);
		

	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fTextViewer.getTextWidget().setWordWrap(isChecked());
		DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP,isChecked());
		DebugUIPlugin.getDefault().savePluginPreferences();
	}
	
}
