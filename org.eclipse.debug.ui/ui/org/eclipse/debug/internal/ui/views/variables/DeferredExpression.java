/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.ui.progress.IElementCollector;

public class DeferredExpression extends DeferredVariable {

    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void fetchDeferredChildren(Object parent, IElementCollector collector, IProgressMonitor monitor) {
        Object[] children= null;
        try {
            if (parent instanceof IExpressionManager) {
                children = ((IExpressionManager)parent).getExpressions();
            } else if (parent instanceof IExpression) {
                if (parent instanceof IErrorReportingExpression) {
                    IErrorReportingExpression expression= (IErrorReportingExpression) parent;
                    if (expression.hasErrors()) {
                        children= expression.getErrorMessages();
                    }
                }
                if (children == null) {
                    IExpression expression = (IExpression)parent;
                    IValue value = expression.getValue();
                    children = getValueChildren(expression, value);
                }
            } else if (parent instanceof IVariable) {
                super.fetchDeferredChildren(parent, collector, monitor);
                return;
            }
        } catch (DebugException de) {
            DebugUIPlugin.log(de);
        }
        
        if (children == null) {
            children = new Object[0];
        }
        collector.add(children, monitor);
        collector.done();
    }


}
