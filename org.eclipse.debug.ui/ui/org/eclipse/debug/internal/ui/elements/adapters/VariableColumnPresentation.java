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

import org.eclipse.debug.internal.ui.viewers.provisional.AbstractColumnPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Columns for Java variables.
 * 
 * @since 3.2
 */
public class VariableColumnPresentation extends AbstractColumnPresentation {
	
	/**
	 * Constant identifier for the default variable column presentation.
	 */
	public final static String DEFAULT_VARIABLE_COLUMN_PRESENTATION = IDebugUIConstants.PLUGIN_ID + ".VARIALBE_COLUMN_PRESENTATION";  //$NON-NLS-1$
	
	/**
	 * Default column identifiers
	 */
	public final static String COLUMN_VARIABLE_NAME = DEFAULT_VARIABLE_COLUMN_PRESENTATION + ".COL_VAR_NAME"; //$NON-NLS-1$
	public final static String COLUMN_VARIABLE_TYPE = DEFAULT_VARIABLE_COLUMN_PRESENTATION + ".COL_VAR_TYPE"; //$NON-NLS-1$
	public final static String COLUMN_VARIABLE_VALUE = DEFAULT_VARIABLE_COLUMN_PRESENTATION + ".COL_VAR_VALUE"; //$NON-NLS-1$
	public final static String COLUMN_VALUE_TYPE = DEFAULT_VARIABLE_COLUMN_PRESENTATION + ".COL_VALUE_TYPE"; //$NON-NLS-1$
	
	private static final String[] ALL_COLUMNS = new String[]{COLUMN_VARIABLE_NAME, COLUMN_VARIABLE_TYPE, COLUMN_VARIABLE_VALUE, COLUMN_VALUE_TYPE};
	private static final String[] INITIAL_COLUMNS = new String[]{COLUMN_VARIABLE_NAME, COLUMN_VARIABLE_VALUE}; 
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getColumns()
	 */
	public String[] getAvailableColumns() {
		return ALL_COLUMNS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getHeader(java.lang.String)
	 */
	public String getHeader(String id) {
		if (COLUMN_VARIABLE_TYPE.equals(id)) {
			return Messages.VariableColumnPresentation_0;
		}
		if (COLUMN_VARIABLE_NAME.equals(id)) {
			return Messages.VariableColumnPresentation_1;
		}
		if (COLUMN_VARIABLE_VALUE.equals(id)) {
			return Messages.VariableColumnPresentation_2;
		}
		if (COLUMN_VALUE_TYPE.equals(id)) {
			return Messages.VariableColumnPresentation_3;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getId()
	 */
	public String getId() {
		return DEFAULT_VARIABLE_COLUMN_PRESENTATION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getInitialColumns()
	 */
	public String[] getInitialColumns() {
		return INITIAL_COLUMNS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#isOptional()
	 */
	public boolean isOptional() {
		return true;
	}

}
