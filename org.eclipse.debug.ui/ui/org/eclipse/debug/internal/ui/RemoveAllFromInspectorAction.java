package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.help.WorkbenchHelp;

public class RemoveAllFromInspectorAction extends InspectorAction {
	
	private static final String PREFIX= "remove_all_from_inspector_action.";

	public RemoveAllFromInspectorAction(ISelectionProvider provider) {
		super(provider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_ALL));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ALL_ACTION });
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