/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
public class TrackColumnPresentation extends AbstractColumnPresentation {

	/**
	 * Column identifiers
	 */
	public static final String COL_TICK = "TICK";
	public static final String COL_BYTES = "BYTES";
	public static final String COL_COMMAND = "COMMAND";
	public static final String COL_CHANNEL = "CHANNEL";

	/**
	 * All columns
	 */
	public static final String[] COLUMN_IDS = new String[]{COL_TICK, COL_BYTES, COL_COMMAND, COL_CHANNEL};
	
	/**
	 * Initial columns
	 */
	public static final String[] COLUMN_INITIAL = new String[]{COL_TICK, COL_BYTES};

	/**
	 * Column presentation ID.
	 */
	public static final String ID = "org.eclipse.debug.examples.ui.midi.trackColumns";
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getAvailableColumns()
	 */
	public String[] getAvailableColumns() {
		return COLUMN_IDS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getHeader(java.lang.String)
	 */
	public String getHeader(String id) {
		if (COL_TICK.equals(id)) {
			return "Tick";
		}
		if (COL_BYTES.equals(id)) {
			return "Bytes";
		}
		if (COL_CHANNEL.equals(id)) {
			return "Channel";
		}
		if (COL_COMMAND.equals(id)) {
			return "Command";
		}
		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getId()
	 */
	public String getId() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getInitialColumns()
	 */
	public String[] getInitialColumns() {
		return COLUMN_INITIAL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#isOptional()
	 */
	public boolean isOptional() {
		return false;
	}

}
