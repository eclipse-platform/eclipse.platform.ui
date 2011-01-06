/*******************************************************************************
 * Copyright (c) 2011 Fair Isaac Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fair Isaac Corporation <Hemant.Singh@Gmail.com> - Initial API and implementation - http://bugs.eclipse.org/326695
 ******************************************************************************/

package org.eclipse.ui.model;

import org.eclipse.jface.viewers.StyledString;

/**
 * Extension interface for <code>IWorkbenchAdapter</code> that allows for
 * StyledString support.
 * 
 * @see IWorkbenchAdapter
 * @see WorkbenchLabelProvider
 * @see BaseWorkbenchContentProvider
 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider
 * 
 * @since 3.7
 */
public interface IWorkbenchAdapter3 {

	/**
	 * Returns the styled text label for the given element.
	 * 
	 * @param element
	 *            the element to evaluate the styled string for
	 * @return the styled string.
	 */
	public StyledString getStyledText(Object element);
}
