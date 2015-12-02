/*******************************************************************************
 * Copyright (c) 2013, 2015 Robin Stocker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robin Stocker - extracted API out of CellLabelProvider
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * Interface to provide tool tip information for a given element.
 *
 * @see org.eclipse.jface.viewers.CellLabelProvider
 *
 * @since 3.10
 */
public interface IToolTipProvider {

	/**
	 * Get the text displayed in the tool tip for object.
	 *
	 * @param element
	 *            the element for which the tool tip is shown
	 * @return the {@link String} or <code>null</code> if there is not text to
	 *         display
	 */
	public String getToolTipText(Object element);

}
