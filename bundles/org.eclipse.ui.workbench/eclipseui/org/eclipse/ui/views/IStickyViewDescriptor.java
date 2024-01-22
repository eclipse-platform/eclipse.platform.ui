/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.views;

/**
 * Supplemental view interface that describes various sticky characteristics
 * that a view may possess.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.views.IViewRegistry
 * @see org.eclipse.ui.views.IViewDescriptor
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IStickyViewDescriptor {
	/**
	 * Return the id of the view to be made sticky.
	 *
	 * @return the id of the view to be made sticky
	 */
	String getId();

	/**
	 * Return the location of this sticky view. Must be one of
	 * <code>IPageLayout.LEFT</code>, <code>IPageLayout.RIGHT</code>,
	 * <code>IPageLayout.TOP</code>, or <code>IPageLayout.BOTTOM</code>.
	 *
	 * @return the location constant
	 */
	int getLocation();

	/**
	 * Return whether this view should be closeable.
	 *
	 * @return whether this view should be closeeable
	 */
	boolean isCloseable();

	/**
	 * Return whether this view should be moveable.
	 *
	 * @return whether this view should be moveable
	 */
	boolean isMoveable();
}
