/*******************************************************************************
 * Copyright (c) 2011, 2015 Fair Isaac Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fair Isaac Corporation <Hemant.Singh@Gmail.com> - Initial API and implementation(Bug 326695)
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
	 * @param element the element to evaluate the styled string for.
	 *
	 * @return the styled string.
	 */
	StyledString getStyledText(Object element);
}
