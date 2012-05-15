/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;

/**
 * Watch expressions modifier can change the expression name but not its value.
 * 
 * @since 3.6
 */
public class WatchExpressionCellModifier implements ICellModifier {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
        if (VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(property)) {
            return element instanceof IWatchExpression;
        }  
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {
        if (VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(property)) {
            return DefaultLabelProvider.escapeSpecialChars( ((IWatchExpression)element).getExpressionText() );
        }  
        return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {
		Object oldValue = getValue(element, property);
        if (!value.equals(oldValue)) {
        	if (VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(property)) {
				if (element instanceof IWatchExpression) {
					if (value instanceof String) {
						// The value column displays special characters 
					    // escaped, so encode the string with any special 
					    // characters escaped properly
						String expressionText = DefaultLabelProvider.encodeEsacpedChars((String)value);
						IWatchExpression expression = (IWatchExpression) element;
						// Bug 345974 see ExpressionManagerContentProvider.AddNewExpressionElement.modify does not allow an empty string
						if (expressionText.trim().length() > 0) {
							expression.setExpressionText(expressionText);
						} else {
							DebugPlugin.getDefault().getExpressionManager().removeExpression(expression);
						}
					}
				}
	        }
		}
	}

}
