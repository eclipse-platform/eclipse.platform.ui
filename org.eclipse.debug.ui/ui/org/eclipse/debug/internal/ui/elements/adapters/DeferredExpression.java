/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;

public class DeferredExpression extends DeferredVariable {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression= (IErrorReportingExpression) parent;
            if (expression.hasErrors()) {
                return expression.getErrorMessages();
            }
        }
		if (parent instanceof IExpression) {
		    IExpression expression = (IExpression)parent;
		    IValue value = expression.getValue();
		    try {
		        return getValueChildren(expression, value);
		    } catch (DebugException e) {
		    }
		}
		return super.getChildren(parent);
	}

    protected boolean hasChildren(Object child) {
        if (child instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression = (IErrorReportingExpression) child;
            if (expression.hasErrors()) {
                return true;
            }
        }
        
        if (child instanceof IExpression) {
            IExpression expression = (IExpression) child;
            IValue value = expression.getValue();
            if (value != null) {
                try {
                    return value.hasVariables();
                } catch (DebugException e) {
                }
            }
        }
        
        return super.hasChildren(child);
    }

    

}
