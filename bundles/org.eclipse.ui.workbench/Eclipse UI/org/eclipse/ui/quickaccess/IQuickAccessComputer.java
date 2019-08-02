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

/**
 * Implementations of this interface are used to compute some extra content for
 * the Quick Access features, using extension point
 * <code>org.eclipse.ui.quickaccess</code>.
 * <p>
 * Use extension class {@link IQuickAccessComputerExtension} to run a new query
 * and return new results whenever filter text change.
 * </p>
 *
 * @since 3.115
 */
public interface IQuickAccessComputer {

	/**
	 * Returns the elements to add to the Quick Access proposals, for any request or
	 * filter.
	 * <p>
	 * The returned elements are then filtered according to user input so only
	 * relevant ones are shown in the list of proposals.
	 * </p>
	 *
	 * @return the elements to add to the Quick Access proposals
	 */
	QuickAccessElement[] computeElements();

	/**
	 * If necessary, reset the state of this computer.
	 */
	void resetState();

	/**
	 * @return whether the current computer needs a refresh (ie last result of
	 *         {@link #computeElements()} is outdated).
	 */
	boolean needsRefresh();

}
