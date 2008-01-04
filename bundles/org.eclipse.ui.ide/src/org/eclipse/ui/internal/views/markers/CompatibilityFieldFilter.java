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
public abstract class CompatibilityFieldFilter extends MarkerFieldFilter {

	/**
	 * Load the settings in the legacy format for the receiver.
	 * @param memento
	 */
	public abstract void loadLegacySettings(IMemento memento);

	/**
	 * Initialize from a legacy problem filter
	 * @param problemFilter
	 */
	public abstract void initialize(ProblemFilter problemFilter);
}
