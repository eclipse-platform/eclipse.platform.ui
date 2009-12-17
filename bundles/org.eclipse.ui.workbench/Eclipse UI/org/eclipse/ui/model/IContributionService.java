/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public static final String TYPE_PROPERTY = "property"; //$NON-NLS-1$

	/**
	 * contributionType value for Preferences
	 */
	public static final String TYPE_PREFERENCE = "preference"; //$NON-NLS-1$

	/**
	 * Return a comparator for ordering contributions within the user interface.
	 * 
	 * @param contributionType
	 *            the type of contribution, must not be <code>null</code>. 
	 * @return the comparator
	 * @see #TYPE_PREFERENCE
	 * @see #TYPE_PROPERTY
	 */
	public ContributionComparator getComparatorFor(String contributionType);
}
