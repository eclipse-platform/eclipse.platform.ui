package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
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