package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import java.util.Iterator;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This action applies specifically to the <code>InspectorView</code>, whereas 
 * <code>AddToInspectorAction</code> applies to a more generic variables view.  
 * The difference is that this action has to avoid re-adding items
 * to the inspector.
 */
public class InspectorViewAddToInspectorAction extends AddToInspectorAction {

	public InspectorViewAddToInspectorAction(ISelectionProvider sp) {
		super(sp);
	}

	/**
	 * @see AddToInspectorAction
	 * Top-level items in the inspector are of type <code>InspectItem</code>.
	 * Children are <code>IVariable</code>s.  So to avoid re-adding top-level items to
	 * the inspector, we simply check the type, and only add <code>IVariable</code>s.
	 */
	protected void doAction(InspectorView view) throws DebugException {
		IStructuredSelection s = getStructuredSelection();
		Iterator vars = s.iterator();
		while (vars.hasNext()) {
			Object item= vars.next();
			if (item instanceof IVariable) {			
				IVariable var = (IVariable)item;
				DebugUITools.inspect(var.getName(), var.getValue());
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
			if (item instanceof IVariable) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}
}