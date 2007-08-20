/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

/**
 * Memento provider for expression manager.
 * 
 * @since 3.4
 */
public class ExpressionManagerMementoProvider extends DebugElementMementoProvider {

	private static final String EXP_MGR = "EXP_MGR"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider#getElementName(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected String getElementName(Object element, IPresentationContext context) throws CoreException {
		if (element instanceof IExpressionManager) {
			return EXP_MGR;
		}
		return null;
	}

}
