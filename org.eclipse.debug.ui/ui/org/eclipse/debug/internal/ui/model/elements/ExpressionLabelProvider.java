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
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.swt.graphics.RGB;

/**
 * @since 3.3
 */
public class ExpressionLabelProvider extends VariableLabelProvider {

	protected RGB getForeground(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
        if (element instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression = (IErrorReportingExpression) element;
            if (expression.hasErrors()) {
                return new RGB(255, 0, 0);
            }
        }		
		return super.getForeground(element, presentationContext, columnId);
	}
	
}
