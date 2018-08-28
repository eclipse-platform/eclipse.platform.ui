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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Font;

/**
 * The ITableFontProvider is a font provider that provides fonts to
 * individual cells within tables.
 * @since 3.1
 */
public interface ITableFontProvider {

	/**
	 * Provides a font for the given element at index
	 * columnIndex.
	 * @param element The element being displayed
	 * @param columnIndex The index of the column being displayed
	 * @return Font
	 */
	public Font getFont(Object element, int columnIndex);

}
