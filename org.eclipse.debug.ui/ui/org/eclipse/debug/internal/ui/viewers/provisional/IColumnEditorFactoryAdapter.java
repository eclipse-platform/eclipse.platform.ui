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
 * An adapter used to column editors. Used in conjunction with a column presentation.
 * 
 * @since 3.2
 */
public interface IColumnEditorFactoryAdapter {

	/**
	 * Constructs and returns the column editor for the given presentation
	 * context (view) and element, or <code>null</code> of none.
	 * 
	 * @param context presentation context
	 * @param element the element
	 * @return column editor or <code>null</code>
	 */
	public IColumnEditor createColumnEditor(IPresentationContext context, Object element);
	
	/**
	 * Returns the type of column editor to be used for the given context and object
	 * or <code>null</code> if none. Allows a previous column editor to be re-used if
	 * it has not changed type.
	 * 
	 * @param context presentation context
	 * @param element element
	 * @return column editor id or <code>null</code>
	 */
	public String getColumnEditorId(IPresentationContext context, Object element);
}
