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
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.swt.graphics.RGB;

public class ExpressionLabelAdapter extends VariableLabelAdapter {

    protected RGB[] getForegrounds(Object element, IPresentationContext context) throws CoreException {
        if (element instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression = (IErrorReportingExpression) element;
            if (expression.hasErrors()) {
                return new RGB[] { new RGB(255, 0, 0) };
            }
        }
        return super.getForegrounds(element, context);
    }
}
