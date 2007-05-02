/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import java.util.LinkedHashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * @since 3.3
 */
public class ExpressionContentProvider extends VariableContentProvider {

	protected Object[] getAllChildren(Object parent, IPresentationContext context) throws CoreException {
       if (parent instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression = (IErrorReportingExpression) parent;
            if (expression.hasErrors()) {
                String[] messages = expression.getErrorMessages();
                LinkedHashSet set = new LinkedHashSet(messages.length);
                for (int i = 0; i < messages.length; i++) {
					set.add(messages[i]);
				}
                return set.toArray();
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
	
	protected boolean hasChildren(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (element instanceof IErrorReportingExpression) {
			IErrorReportingExpression expression = (IErrorReportingExpression) element;
			if (expression.hasErrors()) {
				return true;
			}
		}
		IValue value = ((IExpression)element).getValue();
		if (value == null) {
			return false;
		}
		return value.hasVariables();
	}	
}
