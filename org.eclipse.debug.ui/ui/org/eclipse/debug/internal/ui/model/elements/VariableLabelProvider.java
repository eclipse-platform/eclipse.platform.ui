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
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;

/**
 * @since 3.3
 */
public class VariableLabelProvider extends DebugElementLabelProvider {

	protected RGB getBackground(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (columnId != null) {
	        if (element instanceof IVariable) {
	        	IVariable variable = (IVariable) element;
				if (variable.hasValueChanged()) {
					return DebugUIPlugin.getPreferenceColor(IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB();
				}
	        }
		}
		return super.getBackground(element, presentationContext, columnId);
	}

	protected RGB getForeground(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (columnId == null) {
	        if (element instanceof IVariable) {
	        	IVariable variable = (IVariable) element;
				if (variable.hasValueChanged()) {
					return DebugUIPlugin.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR).getRGB();
				}
	        }
		}
	    return super.getForeground(element, presentationContext, columnId);
	}

	protected ImageDescriptor getImageDescriptor(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (columnId == null || VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(columnId)) {
			return super.getImageDescriptor(element, presentationContext, columnId);
		}
		return null;
	}

	protected String getLabel(Object element, IPresentationContext context, String columnId) throws CoreException {
		if (columnId == null) {
			return escapeSpecialChars(super.getLabel(element, context, columnId));
		} else {
			IVariable variable = (IVariable) element;
			IValue value = variable.getValue();		
			return getColumnText(variable, value, context, columnId);
		}
	}
	
	/**
	 * Returns text for a specific columns for the variable/value.
	 * 
	 * @param variable
	 * @param value
	 * @param context
	 * @param columnId
	 * @return
	 * @throws CoreException
	 */
	protected String getColumnText(IVariable variable, IValue value, IPresentationContext context, String columnId) throws CoreException {
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
	
	protected String escapeSpecialChars(String label) {
		return DefaultLabelProvider.escapeSpecialChars(label);
	}	
}
