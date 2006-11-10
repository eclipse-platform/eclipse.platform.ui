/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;

/**
 * Label adapter for variables.
 * 
 * @since 3.2
 */
public class VariableLabelAdapter extends AsynchronousDebugLabelAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.AsynchronousDebugLabelAdapter#getLabels(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected String[] getLabels(Object element, IPresentationContext context) throws CoreException {
		String viewId = context.getId();
		String[] ids = context.getColumns();
		if (ids != null) {
			if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(viewId) || IDebugUIConstants.ID_REGISTER_VIEW.equals(viewId)) {
				IVariable variable = (IVariable) element;
				IValue value = variable.getValue();
				String[] columns = new String[ids.length];
				for (int i = 0; i < ids.length; i++) {
					columns[i] = getColumnText(variable, value, ids[i], context);
				}
				return columns;
			}
		}
		String[] labels = super.getLabels(element, context);
		String[] escaped = new String[labels.length];
		for (int i = 0; i < escaped.length; i++) {
			escaped[i] = escapeSpecialChars(labels[i]);
		}
		return escaped;
	}
	
	/**
	 * Returns the text for the given variable and value for the specified column.
	 * 
	 * @param variable
	 * @param value
	 * @param columnId
	 * @return text
	 * @throws CoreException
	 */
	protected String getColumnText(IVariable variable, IValue value, String columnId, IPresentationContext context) throws CoreException {
		if (VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(columnId)) {
			return getVariableName(variable, context);
		} else if (VariableColumnPresentation.COLUMN_VARIABLE_TYPE.equals(columnId)) {
			return getVariableTypeName(variable, context);
		} else if (VariableColumnPresentation.COLUMN_VARIABLE_VALUE.equals(columnId)) {
			return getValueText(variable, value, context);
		} else if (VariableColumnPresentation.COLUMN_VALUE_TYPE.equals(columnId)) {
			return getValueTypeName(variable, value, context);
		}		
		return null;
	}
	
	/**
	 * Returns the name of the given variable to display in <code>COLUMN_VARIABLE_NAME</code>.
	 * 
	 * @param variable
	 * @return variable name
	 * @throws CoreException
	 */
	protected String getVariableName(IVariable variable, IPresentationContext context) throws CoreException {
		return variable.getName();
	}
	
	/**
	 * Returns the type name of the given variable to display in <code>COLUMN_VARIABLE_TYPE</code>.
	 * 
	 * @param variable
	 * @return variable type name
	 * @throws CoreException
	 */
	protected String getVariableTypeName(IVariable variable, IPresentationContext context) throws CoreException {
		return variable.getReferenceTypeName();
	}
	
	/**
	 * Returns the label for the given value's type to display in <code>COLUMN_VARIABLE_VALUE</code>
	 * 
	 * @param variable
	 * @param value
	 * @return value label
	 * @throws CoreException
	 */
	protected String getValueTypeName(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		return value.getReferenceTypeName();
	}
	
	/**
	 * Returns the label for the given value to display in <code>COLUMN_VALUE_TYPE</code>
	 * 
	 * @param variable
	 * @param value
	 * @return value label
	 * @throws CoreException
	 */
	protected String getValueText(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		return escapeSpecialChars(value.getValueString());
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#getForeground(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	protected RGB[] getForegrounds(Object element, IPresentationContext context) throws CoreException {
		int numElements = getNumElements(context);
		if (numElements == 1) {
	        if (element instanceof IVariable) {
	        	IVariable variable = (IVariable) element;
	        	try {
					if (variable.hasValueChanged()) {
						RGB[] rgbs =  new RGB[numElements];
						Arrays.fill(rgbs, DebugUIPlugin.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR).getRGB());
						return rgbs;
					}
				} catch (DebugException e) {
				}
	        }
		}
        return super.getForegrounds(element, context);
	}

	protected RGB[] getBackgrounds(Object element, IPresentationContext context) throws CoreException {
		int numElements = getNumElements(context);
		if (numElements > 1) {
	        if (element instanceof IVariable) {
	        	IVariable variable = (IVariable) element;
	        	try {
					if (variable.hasValueChanged()) {
						RGB[] rgbs =  new RGB[numElements];
						Arrays.fill(rgbs, DebugUIPlugin.getPreferenceColor(IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB());
						return rgbs;
					}
				} catch (DebugException e) {
				}
	        }
		}
        return super.getBackgrounds(element, context);
	}
	
	protected String escapeSpecialChars(String label) {
		return DefaultLabelProvider.escapeSpecialChars(label);
	}

	
}
