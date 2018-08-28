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
 * The IFontDecorator is an interface for objects that return a font to
 * decorate an object.
 *
 * If an IFontDecorator decorates a font in an object that also has
 * an IFontProvider the IFontDecorator will take precedence.
 * @see IFontProvider
 *
 * @since 3.1
 */
public interface IFontDecorator {

	/**
	 * Return the font for element or <code>null</code> if there
	 * is not one.
	 *
	 * @param element
	 * @return Font or <code>null</code>
	 */
	public Font decorateFont(Object element);

}
