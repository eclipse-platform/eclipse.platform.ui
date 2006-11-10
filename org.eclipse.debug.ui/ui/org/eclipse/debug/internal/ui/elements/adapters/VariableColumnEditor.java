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
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.debug.internal.ui.viewers.provisional.AbstractColumnEditor;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Columns for Java variables.
 * 
 * @since 3.2
 */
public class VariableColumnEditor extends AbstractColumnEditor {
	
	/**
	 * Constant identifier for the default variable column presentation.
	 */
	public final static String DEFAULT_VARIABLE_COLUMN_EDITOR = IDebugUIConstants.PLUGIN_ID + ".VARIALBE_COLUMN_EDITOR";  //$NON-NLS-1$
	
	private ICellModifier fCellModifier;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor#getCellEditor(java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor getCellEditor(String id, Object element, Composite parent) {
		return new TextCellEditor(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor#getCellModifier()
	 */
	public ICellModifier getCellModifier() {
		if (fCellModifier == null) {
			fCellModifier = new DefaultVariableCellModifier();
		}
		return fCellModifier;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor#getId()
	 */
	public String getId() {
		return DEFAULT_VARIABLE_COLUMN_EDITOR;
	}

}
