package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

public class RemoveAllFromInspectorAction extends InspectorAction {
	
	private static final String PREFIX= "remove_all_from_inspector_action.";

	public RemoveAllFromInspectorAction(ISelectionProvider provider) {
		super(provider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
	}

	/**
	 * @see InspectorAction
	 */
	protected void doAction(InspectorView view) {		
		// Ask view to remove all variables
		view.removeAllFromInspector();
	}
	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
	}
}