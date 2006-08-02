/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable;

import org.eclipse.swt.widgets.Control;

/**
 * An interface for objects that want to listen to and have the
 * possibility of vetoing row change events on a CompositeTable.
 * 
 * @since 3.2
 */
public class RowFocusAdapter implements IRowFocusListener {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.IRowFocusListener#arrive(org.eclipse.jface.examples.databinding.compositetable.CompositeTable, int, org.eclipse.swt.widgets.Control)
	 */
	public void arrive(CompositeTable sender, int currentObjectOffset,
			Control newRow) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.IRowFocusListener#depart(org.eclipse.jface.examples.databinding.compositetable.CompositeTable, int, org.eclipse.swt.widgets.Control)
	 */
	public void depart(CompositeTable sender, int currentObjectOffset,
			Control row) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.IRowFocusListener#requestRowChange(org.eclipse.jface.examples.databinding.compositetable.CompositeTable, int, org.eclipse.swt.widgets.Control)
	 */
	public boolean requestRowChange(CompositeTable sender,
			int currentObjectOffset, Control row) {
		return true;
	}

}
