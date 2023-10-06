/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Change;

/**
 * Implementation for a refactoring preview change filter.
 *
 * @since 3.2
 */
public class RefactoringPreviewChangeFilter {

	/**
	 * Is the specified change accepted by the filter?
	 *
	 * @param change
	 *            the change to test
	 * @return <code>true</code> if it is accepted for preview,
	 *         <code>false</code> otherwise
	 */
	public boolean select(final Change change) {
		return true;
	}
}