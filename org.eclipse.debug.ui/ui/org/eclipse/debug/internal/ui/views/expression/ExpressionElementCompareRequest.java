/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Fix for viewer state save/restore [188704] 
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
public class ExpressionElementCompareRequest extends ExpressionElementMementoRequest implements IElementCompareRequest {

	private boolean fEqual;
	
	public ExpressionElementCompareRequest(ExpressionView view, IPresentationContext context, Object element, IMemento memento, String[] workingSets) {
		super(view, context, element, memento, workingSets);
	}
	public void setEqual(boolean equal) {
		fEqual = equal;
	}

	public boolean isEqual() {
		return fEqual;
	}

	protected void performFinished() {
		fView.compareRequestFinished(ExpressionElementCompareRequest.this);
	}
	
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("IElementCompareRequest: "); //$NON-NLS-1$
        buf.append(getElement());
        return buf.toString();
    }
}
