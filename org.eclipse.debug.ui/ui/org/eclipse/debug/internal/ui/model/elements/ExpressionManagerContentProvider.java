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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Default content provider for the expression manager.
 */
public class ExpressionManagerContentProvider extends ElementContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		return ((IExpressionManager) element).getExpressions().length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		return getElements(((IExpressionManager) parent).getExpressions(), index, length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#supportsContextId(java.lang.String)
	 */
	protected boolean supportsContextId(String id) {
		return id.equals(IDebugUIConstants.ID_EXPRESSION_VIEW);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		return ((IExpressionManager)element).hasExpressions();
	}
	
}
