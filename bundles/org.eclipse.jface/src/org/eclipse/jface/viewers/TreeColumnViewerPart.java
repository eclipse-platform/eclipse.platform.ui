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

package org.eclipse.jface.viewers;

import org.eclipse.swt.widgets.TreeColumn;

/**
 * Wrapper class for {@link TreeColumn}
 * @since 3.3
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as
 * part of a work in progress. This API may change at any given time. Please 
 * do not use this API without consulting with the Platform/UI team.

 * 
 */
public class TreeColumnViewerPart extends ColumnViewerPart {
	private TreeColumn column;

	/**
	 * Create new TableColumnViewerPart. This simply wrapps up a new
	 * {@link TreeColumn#TableColumn(org.eclipse.swt.widgets.Table, int)}.
	 * Before calling {@link TreeViewer#setInput(Object)}} you need to set the
	 * label-provider using
	 * {@link ColumnViewerPart#setLabelProvider(ViewerLabelProvider)}
	 * 
	 * @param parent
	 *            the table-viewer
	 * @param style
	 *            style bits used to create TableColumn {@link TreeColumn}
	 */
	public TreeColumnViewerPart(TreeViewer parent, int style) {
		this(new TreeColumn(parent.getTree(), style),null);
	}

	/**
	 * Create new TableColumnViewerPart. This simply wrapps up a new
	 * {@link TableColumn#TableColumn(org.eclipse.swt.widgets.Table, int, int)}
	 * 
	 * @param parent
	 *            the table-viewer
	 * @param style
	 *            style style bits used to create TableColumn
	 *            {@link TableColumn}
	 * @param index
	 *            the index of the column
	 */
	public TreeColumnViewerPart(TreeViewer parent, int style, int index) {
		this(new TreeColumn(parent.getTree(), style, index),null);
	}

	/**
	 * Wrap an existing TableColumn
	 * 
	 * @param column
	 *            the existing table-column
	 * @param labelProvider
	 *            the label provider
	 */
	public TreeColumnViewerPart(TreeColumn column,
			ViewerLabelProvider labelProvider) {
		super(column,labelProvider);
		this.column = column;
	}

	/**
	 * @return access the underlying tree-column
	 */
	public TreeColumn getColumn() {
		return column;
	}
}
