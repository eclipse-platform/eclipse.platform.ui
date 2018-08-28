/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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

package org.eclipse.ui.internal.views.markers;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

/**
 * CompatibilityFieldFilter is the abstract superclass of the internal filters that support
 * the compatibility filter.
 * @since 3.4
 *
 */
abstract class CompatibilityFieldFilter extends MarkerFieldFilter {

	/**
	 * Load the settings in the legacy format for the receiver.
	 * @param memento
	 * @param generator the generator we are using now
	 */
	abstract void loadLegacySettings(IMemento memento, MarkerContentGenerator generator);

	/**
	 * Initialize from a legacy problem filter
	 * @param problemFilter
	 */
	abstract void initialize(ProblemFilter problemFilter);
}
