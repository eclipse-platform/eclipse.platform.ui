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
package org.eclipse.ui.model;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Extension interface for <code>IWorkbenchAdapter</code> that allows for color
 * and font support.
 * <p>
 * There is an associate label provider and content provider for showing
 * elements with a registered workbench adapter in JFace structured viewers.
 * </p>
 * 
 * @see IWorkbenchAdapter
 * @see WorkbenchLabelProvider
 * @see BaseWorkbenchContentProvider
 * @since 3.0
 */
public interface IWorkbenchAdapter2 {

	/**
	 * Provides a foreground color for the given element.
	 *
	 * @param element the element
	 * @return the foreground color for the element, or <code>null</code> to use the
	 *         default foreground color
	 */
	RGB getForeground(Object element);

	/**
	 * Provides a background color for the given element.
	 *
	 * @param element the element
	 * @return the background color for the element, or <code>null</code> to use the
	 *         default background color
	 */
	RGB getBackground(Object element);

	/**
	 * Provides a font the given element.
	 *
	 * @param element the element
	 * @return the font for the element, or <code>null</code> to use the default
	 *         font
	 */
	FontData getFont(Object element);

}
