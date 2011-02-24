/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.provisional;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Common function for a column presentation.
 * <p>
 * Clients implementing <code>IColumnPresentation</code> must subclass this class.
 * </p>
 * @since 3.2
 */
public abstract class AbstractColumnPresentation implements IColumnPresentation2 {
	
	private IPresentationContext fContext;
	
	/**
	 * Empty array of strings
	 */
	protected static final String[] EMPTY = new String[0];

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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getImageDescriptor(java.lang.String)
	 */
	public ImageDescriptor getImageDescriptor(String id) {
		return null;
	}

	/**
	 * Returns the context this column presentation is installed in.
	 * 
	 * @return presentation context
	 */
	protected IPresentationContext getPresentationContext() {
		return fContext;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation2#getInitialColumnWidth(java.lang.String, int, java.lang.String[])
	 */
	public int getInitialColumnWidth(String id, int treeWidgetWidth, String[] visibleColumnIds) {
		return -1;
	}
}
