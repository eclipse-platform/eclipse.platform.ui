/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;

/**
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.2
 */
public interface INavigatorSaveablesService {

	/**
	 * Initializes this service. Must be called on the UI thread, and may only
	 * be called once. The given viewer's control must not be disposed.
	 * <p>
	 * The given listener will be notified of changes to the result of
	 * {@link #getSaveables()} and to the dirty state of those saveables. The
	 * given source will be used as the event source for these event
	 * notifications.
	 * </p>
	 * <p>
	 * The given viewer's selection will be used by
	 * {@link #getActiveSaveables()} to determine the active saveables. The
	 * active saveables are determined by iterating over the current selection,
	 * and for each element, walking up its parent chain until an element
	 * representing a saveable is found, or a root element is reached.
	 * </p>
	 *
	 * @param source the source of possible saveables
	 * @param viewer associated viewer
	 * @param listener listener to inform about events on saveables of the source
	 */
	public void init(ISaveablesSource source, StructuredViewer viewer,
			ISaveablesLifecycleListener listener);

	/**
	 * Returns the Saveable objects for which elements are contained in the
	 * tree.
	 *
	 * @return the saveables
	 */
	public Saveable[] getSaveables();

	/**
	 * Returns the active saveables based on the current selection. This method
	 * must be called on the UI thread.
	 *
	 * @return the active saveables based on the current selection
	 */
	public Saveable[] getActiveSaveables();

	/**
	 * Check if any SaveablesProviders are contributed.
	 *
	 * @return True if and only if any SaveablesProviders are contributed for this
	 *         service.
	 *
	 * @since 3.9
	 */
	boolean hasSaveablesProvider();

}
