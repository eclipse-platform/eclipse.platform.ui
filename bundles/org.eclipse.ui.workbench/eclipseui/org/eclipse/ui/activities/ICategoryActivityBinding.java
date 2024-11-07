/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.activities;

/**
 * An instance of this interface represents a binding between a category and an
 * activity.
 *
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see IActivity
 * @see ICategory
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICategoryActivityBinding extends Comparable<ICategoryActivityBinding> {

	/**
	 * Returns the identifier of the activity represented in this binding.
	 *
	 * @return the identifier of the activity represented in this binding.
	 *         Guaranteed not to be <code>null</code>.
	 */
	String getActivityId();

	/**
	 * Returns the identifier of the category represented in this binding.
	 *
	 * @return the identifier of the category represented in this binding.
	 *         Guaranteed not to be <code>null</code>.
	 */
	String getCategoryId();
}
