package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.views.ConsoleViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Clears the output of the selected launches
 */
public class ClearOutputAction extends Action {

	private ConsoleViewer fConsoleViewer;

	public ClearOutputAction(ConsoleViewer viewer) {
		super(DebugUIMessages.getString("ClearOutputAction.title")); //$NON-NLS-1$
		fConsoleViewer= viewer;
		setToolTipText(DebugUIMessages.getString("ClearOutputAction.toolTipText")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_CLEAR));		
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_CLEAR));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_CLEAR));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.CLEAR_CONSOLE_ACTION });
	}

	/**
	 * @see Action
	 */
	public void run() {
		fConsoleViewer.clearDocument();
	}
}

