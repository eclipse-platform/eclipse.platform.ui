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
package org.eclipse.debug.internal.ui.viewers.provisional;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


/**
 * Common function for a column editor.
 * <p>
 * Clients implementing <code>IColumnEditor</code> must subclass this class.
 * </p>
 * @since 3.2
 */
public abstract class AbstractColumnEditor implements IColumnEditor {
	
	private IPresentationContext fContext;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#init(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
		fContext = context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#dispose()
	 */
	public void dispose() {
		fContext = null;
	}
	
	/**
	 * Returns the context this column presentation is installed in.
	 * 
	 * @return presentation context
	 */
	protected IPresentationContext getPresentationContext() {
		return fContext;
	}
}
