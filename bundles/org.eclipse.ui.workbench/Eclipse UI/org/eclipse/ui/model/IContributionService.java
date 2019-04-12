/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.model;

/**
 * Instances of this service are capable of providing standard mechanisms that
 * clients may use to order, display, and generally work with contributions to
 * the Workbench.
 *
 * @since 3.4
 *
 */
public interface IContributionService {

	/**
	 * contributionType value for the PropertyDialog
	 */
	String TYPE_PROPERTY = "property"; //$NON-NLS-1$

	/**
	 * contributionType value for Preferences
	 */
	String TYPE_PREFERENCE = "preference"; //$NON-NLS-1$

	/**
	 * Return a comparator for ordering contributions within the user interface.
	 *
	 * @param contributionType the type of contribution, must not be
	 *                         <code>null</code>.
	 * @return the comparator
	 * @see #TYPE_PREFERENCE
	 * @see #TYPE_PROPERTY
	 */
	ContributionComparator getComparatorFor(String contributionType);
}
