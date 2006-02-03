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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;

/**
 *
 */
public class ExpressionContentAdapter extends VariableContentAdapter {

    protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
        if (parent instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression = (IErrorReportingExpression) parent;
            if (expression.hasErrors()) {
                return expression.getErrorMessages();
            }
        }

        if (parent instanceof IExpression) {
            IExpression expression = (IExpression) parent;
            IValue value = expression.getValue();
            if (value != null) {
                return getValueChildren(expression, value, context);
            }
        }
        
        return EMPTY;
    }
    
    protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
        if (element instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression = (IErrorReportingExpression) element;
            if (expression.hasErrors()) {
                return true;
            }
        }
        
        if (element instanceof IExpression) {
            IValue value = ((IExpression) element).getValue();
            if (value != null) {
                return value.hasVariables();
            }
        }
        
        return false;
    }	
    
}
