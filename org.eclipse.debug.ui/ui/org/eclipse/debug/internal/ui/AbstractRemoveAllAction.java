package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Common functionality of 'remove all' actions.
 */
public abstract class AbstractRemoveAllAction extends Action {
	
	public AbstractRemoveAllAction(String label, String tooltip) {
		super(label);
		setToolTipText(tooltip);
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_ALL));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ALL_ACTION });
	}
	
}
