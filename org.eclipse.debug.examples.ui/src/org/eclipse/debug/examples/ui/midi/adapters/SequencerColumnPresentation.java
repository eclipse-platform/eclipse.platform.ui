/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.ui.midi.adapters;

import org.eclipse.debug.internal.ui.viewers.provisional.AbstractColumnPresentation;

/**
 * Columns for sequencer in the variables view.
 * 
 * @since 1.0
 */
public class SequencerColumnPresentation extends AbstractColumnPresentation {

	/**
	 * Column identifiers
	 */
	public static final String COL_NAME = "NAME"; //$NON-NLS-1$
	public static final String COL_VALUE = "VALUE"; //$NON-NLS-1$

	/**
	 * All columns
	 */
	public static final String[] COLUMN_IDS = new String[]{COL_NAME, COL_VALUE};

	/**
	 * Column presentation ID.
	 */
	public static final String ID = "org.eclipse.debug.examples.ui.midi.columnPresentation"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getAvailableColumns()
	 */
	@Override
	public String[] getAvailableColumns() {
		return COLUMN_IDS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getHeader(java.lang.String)
	 */
	@Override
	public String getHeader(String id) {
		if (COL_NAME.equals(id)) {
			return "Control"; //$NON-NLS-1$
		}
		if (COL_VALUE.equals(id)) {
			return "Value"; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getId()
	 */
	@Override
	public String getId() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getInitialColumns()
	 */
	@Override
	public String[] getInitialColumns() {
		return COLUMN_IDS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#isOptional()
	 */
	@Override
	public boolean isOptional() {
		return false;
	}

}
