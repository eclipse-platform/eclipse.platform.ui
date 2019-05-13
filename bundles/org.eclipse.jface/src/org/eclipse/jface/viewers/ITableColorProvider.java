/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Initial implementation - Gunnar Ahlberg (IBS AB, www.ibs.net)
 *     IBM Corporation - further revisions
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;

/**
 * Interface to provide color representation for a given cell within
 * the row for an element in a table.
 * @since 3.1
 */
public interface ITableColorProvider {

	/**
	 * Provides a foreground color for the given element.
	 *
	 * @param element the element
	 * @param columnIndex the zero-based index of the column in which
	 * 	the color appears
	 * @return the foreground color for the element, or <code>null</code> to
	 *         use the default foreground color
	 */
	Color getForeground(Object element, int columnIndex);

	/**
	 * Provides a background color for the given element at the specified index
	 *
	 * @param element the element
	 * @param columnIndex the zero-based index of the column in which the color appears
	 * @return the background color for the element, or <code>null</code> to
	 *         use the default background color
	 *
	 */
	Color getBackground(Object element, int columnIndex);
}
