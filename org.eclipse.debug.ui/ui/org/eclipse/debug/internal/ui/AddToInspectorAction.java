package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;import org.eclipse.debug.core.DebugException;import org.eclipse.debug.core.model.IVariable;import org.eclipse.debug.ui.DebugUITools;import org.eclipse.jface.viewers.ISelectionProvider;import org.eclipse.jface.viewers.IStructuredSelection;import org.eclipse.ui.help.WorkbenchHelp;

public class AddToInspectorAction extends InspectorAction {

	private static final String PREFIX= "add_to_inspector_action.";
	
	public AddToInspectorAction(ISelectionProvider sp) {
		super(sp, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
		setEnabled(!getStructuredSelection().isEmpty());
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