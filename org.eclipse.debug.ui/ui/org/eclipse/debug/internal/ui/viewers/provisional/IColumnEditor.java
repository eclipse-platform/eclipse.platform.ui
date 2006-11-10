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
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Composite;

/**
 * Creates cell modifiers and editors. Used in conjunction with a column presentation.
 *  
 * @since 3.2
 */
public interface IColumnEditor {
	
	/**
	 * Initializes this column editor to be used in the
	 * given context.
	 * 
	 * @param context
	 */
	public void init(IPresentationContext context);
	
	/**
	 * Disposes this column presentation
	 */
	public void dispose();	
	
	/**
	 * Returns a cell editor to use for the specified column and object or <code>null</code>
	 * if none.
	 * 
	 * @param id column id
	 * @param element object to be edited
	 * @param parent parent control to create the cell editor in
	 * @return cell editor or <code>null</code>
	 */
	public CellEditor getCellEditor(String id, Object element, Composite parent);
	
	/**
	 * Returns the cell modifier for this set of columns.
	 * 
	 * @return cell modifier
	 */
	public ICellModifier getCellModifier();	

	/**
	 * Returns an identifier for this column editor.
	 * The identifier should be unique per kind of column editor 
	 * (for example, the column editor for Java variables
	 * in the variables view).
	 * 
	 * @return identifier
	 */
	public String getId();	
}
