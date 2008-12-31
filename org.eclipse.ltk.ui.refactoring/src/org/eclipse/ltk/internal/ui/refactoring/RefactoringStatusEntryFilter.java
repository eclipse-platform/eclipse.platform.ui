/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

/**
 * Implementation for a refactoring status entry filter.
 *
 * @since 3.2
 */
public class RefactoringStatusEntryFilter {

	/**
	 * Is the specified status entry accepted by the filter?
	 *
	 * @param entry
	 *            the status entry to test
	 * @return <code>true</code> if it is accepted for preview,
	 *         <code>false</code> otherwise
	 */
	public boolean select(final RefactoringStatusEntry entry) {
		return true;
	}
}
