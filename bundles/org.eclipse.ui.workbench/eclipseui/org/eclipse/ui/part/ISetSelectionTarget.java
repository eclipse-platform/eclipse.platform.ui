/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.part;

import org.eclipse.jface.viewers.ISelection;

/**
 * Interface for views which support reveal and select.
 * <p>
 * This interface may be implemented by subclasses of <code>ViewPart</code>.
 * This interface is commonly used by a New wizard to reveal and select a
 * resource in a workbench part which it has just created.
 * </p>
 *
 * @see org.eclipse.ui.IViewPart
 * @see org.eclipse.ui.part.ViewPart
 */
public interface ISetSelectionTarget {
	/**
	 * Reveals and selects the given element within this target view.
	 *
	 * @param selection the new element to select
	 */
	void selectReveal(ISelection selection);
}
