package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import java.util.Iterator;

public class RemoveFromInspectorAction extends InspectorAction {

	private static final String PREFIX= "remove_from_inspector_action.";

	public RemoveFromInspectorAction(ISelectionProvider provider) {
		super(provider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
		setEnabled(!getStructuredSelection().isEmpty());
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