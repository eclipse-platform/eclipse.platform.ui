package org.eclipse.jface.layout;

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - API refactoring and general maintenance
 *******************************************************************************/

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * The TreeColumnAdapter is the ControlAdapter used to maintain Table sizes
 * {@link Tree}.
 * 
 * @since 3.3
 * @see AbstractColumnAdapter
 */
public class TreeColumnAdapter extends AbstractColumnAdapter {
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param tree
	 *            the tree the layout is applied to
	 */
	public TreeColumnAdapter(Tree tree) {
		super(tree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.layout.AbstractColumnAdapter#getColumnCount()
	 */
	int getColumnCount() {
		return ((Tree) getControl()).getColumnCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.layout.AbstractColumnAdapter#setColumnWidths(int[])
	 */
	void setColumnWidths(int[] widths) {
		TreeColumn[] columns = ((Tree) getControl()).getColumns();
		for (int i = 0; i < widths.length; i++) {
			columns[i].setWidth(widths[i]);
		}
	}
}