/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Font;

/**
 * The ITableFontProvider is a type that provides fonts to 
 * individual cells of tables.
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
