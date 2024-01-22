/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 500661, 492180
 *******************************************************************************/
package org.eclipse.ui.quickaccess;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A QuickAccessElement describes one of the possible actions to show in Quick
 * Access.
 *
 * @since 3.115
 */
public abstract class QuickAccessElement {

	protected static final String separator = " - "; //$NON-NLS-1$

	/**
	 * Returns the label to be displayed to the user.
	 *
	 * @return the label
	 */
	public abstract String getLabel();

	/**
	 * Returns the image descriptor for this element.
	 *
	 * @return an image descriptor, or null if no image is available
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Returns the id for this element. The id has to be unique within the
	 * QuickAccessProvider that provided this element.
	 *
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * Executes the associated action for this element.
	 */
	public abstract void execute();

	/**
	 * Return the label to be used for sorting elements.
	 *
	 * @return the sort label
	 */
	public String getSortLabel() {
		return getLabel();
	}

	/**
	 * Return the label to be used for matching elements. The match string can
	 * contain additional text that should result in a match, but isn't shown in the
	 * quick access UI.
	 * <p>
	 * The match label should always be either identical to or a superset of the
	 * actual {@link #getLabel() label}.
	 *
	 * @return the match label
	 */
	public String getMatchLabel() {
		return getLabel();
	}
}
