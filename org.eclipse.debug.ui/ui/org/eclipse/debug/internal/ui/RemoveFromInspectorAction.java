package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.help.WorkbenchHelp;

public class RemoveFromInspectorAction extends InspectorAction {

	public RemoveFromInspectorAction(ISelectionProvider provider) {
		super(provider, DebugUIMessages.getString("RemoveFromInspectorAction.&Remove_1")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RemoveFromInspectorAction.Remove_Selected_Variables_from_Inspector_2")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE));
		setEnabled(!getStructuredSelection().isEmpty());
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ACTION });
	}
	
	/**
	 * @see InspectorAction
	 */
	protected void doAction(InspectorView view) {
		IStructuredSelection selection= getStructuredSelection();
		Iterator vars= selection.iterator();
		while (vars.hasNext()) {
			Object item= vars.next();
			if (item instanceof InspectItem) {
				view.removeFromInspector(item);
			}
		}
	}

	/**
	 * @see InspectorAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		if (sel == null) {
			setEnabled(false);
			return;
		}
		Iterator iterator= sel.iterator();
		while (iterator.hasNext()) {
			Object item= iterator.next();
			if (item instanceof InspectItem) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);		
	}
}