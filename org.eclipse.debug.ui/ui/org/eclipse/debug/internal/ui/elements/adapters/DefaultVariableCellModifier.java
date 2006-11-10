/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.VariableValueEditorManager;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jface.viewers.ICellModifier;

/**
 * @since 3.2
 *
 */
public class DefaultVariableCellModifier implements ICellModifier {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		if (VariableColumnPresentation.COLUMN_VARIABLE_VALUE.equals(property)) {
			if (element instanceof IVariable) {
				return ((IVariable) element).supportsValueModification();
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {
		if (VariableColumnPresentation.COLUMN_VARIABLE_VALUE.equals(property)) {
			if (element instanceof IVariable) {
				IVariable variable = (IVariable) element;
				try {
					return DefaultLabelProvider.escapeSpecialChars(variable.getValue().getValueString());
				} catch (DebugException e) {
					DebugUIPlugin.log(e);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {
		Object oldValue = getValue(element, property);
        if (!value.equals(oldValue)) {
        	if (VariableColumnPresentation.COLUMN_VARIABLE_VALUE.equals(property)) {
				if (element instanceof IVariable) {
					IVariable variable = (IVariable) element;
					IVariableValueEditor editor = VariableValueEditorManager.getDefault().getVariableValueEditor(variable.getModelIdentifier());
					if (value instanceof String) {
						value = DefaultLabelProvider.encodeEsacpedChars((String)value);
					}
					if (editor != null) {
						if  (editor.saveVariable(variable, (String) value, DebugUIPlugin.getShell())) {
							return;
						}
					}
					try {
						variable.setValue((String) value);
					} catch (DebugException e) {
						DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), Messages.VariableColumnPresentation_4, Messages.VariableColumnPresentation_5, e.getStatus());
					}
				}
	        }
		}
	}

}
