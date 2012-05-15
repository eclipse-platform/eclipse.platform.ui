/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind Rvier Systems - added support for columns (bug 235646)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Factory for default variable column presentation.
 * 
 * @since 3.2
 */
public class VariableColumnFactoryAdapter implements IColumnPresentationFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresenetationFactoryAdapter#createColumnPresentation(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
		String id = context.getId();
        if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id) || 
            IDebugUIConstants.ID_REGISTER_VIEW.equals(id) || 
            IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) 
        {
			if (element instanceof IStackFrame || element instanceof IExpressionManager) {
				return new VariableColumnPresentation();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresenetationFactoryAdapter#getColumnPresentationId(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public String getColumnPresentationId(IPresentationContext context, Object element) {
		String id = context.getId();
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id) || 
		    IDebugUIConstants.ID_REGISTER_VIEW.equals(id) || 
		    IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) 
		{
			if (element instanceof IStackFrame || element instanceof IExpressionManager) {
				return IDebugUIConstants.COLUMN_PRESENTATION_ID_VARIABLE;
			}
		}
		return null;
	}

}
