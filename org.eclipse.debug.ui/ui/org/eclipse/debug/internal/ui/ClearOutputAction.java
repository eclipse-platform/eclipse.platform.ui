package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.jface.action.Action;

/**
 * Clears the output of the selected launches
 */
public class ClearOutputAction extends Action {

	private final static String PREFIX= "clear_output_action.";
	private ConsoleViewer fConsoleViewer;

	public ClearOutputAction(ConsoleViewer viewer) {
		super(DebugUIUtils.getResourceString(PREFIX + TEXT));
		fConsoleViewer= viewer;
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
	}

	/**
	 * @see Action
	 */
	public void run() {
		fConsoleViewer.clearDocument();
	}
}

