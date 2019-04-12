/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.services;

import org.eclipse.ui.ISourceProvider;

/**
 * <p>
 * A service that responds to changes in one or more sources. These sources can
 * be plugged into the service. Sources represent a common event framework for
 * services.
 * </p>
 * <p>
 * Clients must not extend or implement.
 * </p>
 *
 * @since 3.2
 */
public interface IServiceWithSources extends IDisposable {

	/**
	 * Adds a source provider to this service. A source provider will notify the
	 * service when the source it provides changes. An example of a source might be
	 * an active editor or the current selection. This amounts to a pluggable state
	 * tracker for the service.
	 *
	 * @param provider The provider to add; must not be <code>null</code>.
	 */
	void addSourceProvider(ISourceProvider provider);

	/**
	 * Removes a source provider from this service. Most of the time, this method
	 * call is not required as source providers typically share the same life span
	 * as the workbench itself.
	 *
	 * @param provider The provider to remove; must not be <code>null</code>.
	 */
	void removeSourceProvider(ISourceProvider provider);
}
