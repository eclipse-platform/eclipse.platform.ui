package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.help.WorkbenchHelp;

public class AddToInspectorAction extends InspectorAction {
	
	public AddToInspectorAction(ISelectionProvider sp) {
		super(sp, DebugUIMessages.getString("AddToInspectorAction.title")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("AddToInspectorAction.toolTipText")); //$NON-NLS-1$
		setEnabled(!getStructuredSelection().isEmpty());
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_INSPECT));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_INSPECT));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_INSPECT));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.INSPECT_ACTION });
	}

	/**
	 * @see InspectorAction
	 */
	protected void doAction(InspectorView view) throws DebugException {
		IStructuredSelection s = getStructuredSelection();
		Iterator vars = s.iterator();
		while (vars.hasNext()) {
			IVariable var = (IVariable)vars.next();
			DebugUITools.inspect(var.getName(), var.getValue());
		}
	} 
}