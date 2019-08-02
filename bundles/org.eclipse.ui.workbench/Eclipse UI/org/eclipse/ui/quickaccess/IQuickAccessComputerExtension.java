/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.quickaccess;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Extension interface for {@link IQuickAccessComputer} that provides ability to
 * compute a new extra set of proposals whenever filter change.
 * <p>
 * This interfaces is intended to cover cases for which computing all possible
 * elements statically with {@link IQuickAccessComputer#computeElements()} is
 * either impossible or too expensive or not relevant; in which case it can be
 * preferred to compute those proposals according to user input.
 * </p>
 * <p>
 * This interface is not intended to deal with filtering: the returned elements
 * would still be filtered according to user input.
 * </p>
 *
 * @since 3.116
 */
public interface IQuickAccessComputerExtension extends IQuickAccessComputer {

	/**
	 * Returns elements that are relevant for the given query.
	 * <p>
	 * This method is not intended to deal with filtering: the returned elements
	 * will still be filtered according to user input.
	 * </p>
	 * <p>
	 * This method will be called for each change in the query. So, if possible,
	 * it's preferred to use {@link IQuickAccessComputer#computeElements()} which is
	 * called only once per session and whose result is reused, leading to better
	 * performance.
	 * </p>
	 *
	 * @param query   query text
	 * @param monitor support for feedback and cancellation
	 * @return the elements that can be inferred from the query.
	 */
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor);
}
