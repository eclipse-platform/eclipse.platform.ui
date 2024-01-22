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
package org.eclipse.ui;

/**
 * An <code>IWorkingSetUpdater</code> can be used to dynamically update the
 * content of a working set.
 * <p>
 * A working set updater manages a set of working sets. It is contributed via
 * the attribute <code>updaterClass</code> of the <code>
 * org.eclipse.ui.workingSets</code> extension point. Extensions of this
 * extension point must therefore implement this interface.
 * </p>
 * <p>
 * API under construction and subject to change at any time.
 * </p>
 *
 * @since 3.1
 */
public interface IWorkingSetUpdater {
	/**
	 * Adds a working set to this updater.
	 *
	 * @param workingSet the working set to add to this updater
	 */
	void add(IWorkingSet workingSet);

	/**
	 * Removes a working set from this updater.
	 *
	 * @param workingSet the working set to remove
	 *
	 * @return <code>true</code> if the updater changed (e.g. the element got
	 *         removed)
	 */
	boolean remove(IWorkingSet workingSet);

	/**
	 * Returns <code>true</code> if the updater contains the given working set;
	 * otherwise <code>false</code> is returned.
	 *
	 * @param workingSet the parameter to check
	 *
	 * @return whether the updater contains the given working set
	 */
	boolean contains(IWorkingSet workingSet);

	/**
	 * Disposes this working set updater. Implementations of this method typically
	 * remove listeners from some delta providers.
	 */
	void dispose();
}
