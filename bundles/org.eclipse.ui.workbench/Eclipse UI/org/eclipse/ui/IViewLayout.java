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
package org.eclipse.ui;

/**
 * Represents the layout info for a view or placeholder in an
 * {@link IPageLayout}.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewLayout {

	/**
	 * Returns whether the view is closeable. The default is <code>true</code>.
	 *
	 * @return <code>true</code> if the view is closeable, <code>false</code> if not
	 */
	boolean isCloseable();

	/**
	 * Sets whether the view is closeable.
	 *
	 * @param closeable <code>true</code> if the view is closeable,
	 *                  <code>false</code> if not
	 */
	void setCloseable(boolean closeable);

	/**
	 * Returns whether the view is moveable. The default is <code>true</code>.
	 *
	 * @return <code>true</code> if the view is moveable, <code>false</code> if not
	 */
	boolean isMoveable();

	/**
	 * Sets whether the view is moveable.
	 *
	 * @param moveable <code>true</code> if the view is moveable, <code>false</code>
	 *                 if not
	 */
	void setMoveable(boolean moveable);

	/**
	 * Returns whether the view is a standalone view.
	 *
	 * @see IPageLayout#addStandaloneView
	 */
	boolean isStandalone();

	/**
	 * Returns whether the view shows its title. This is only applicable to
	 * standalone views.
	 *
	 * @see IPageLayout#addStandaloneView
	 */
	boolean getShowTitle();
}
