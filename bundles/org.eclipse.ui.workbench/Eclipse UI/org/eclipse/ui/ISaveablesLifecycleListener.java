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
 *******************************************************************************/

package org.eclipse.ui;

/**
 * Listener for events fired by implementers of {@link ISaveablesSource}.
 *
 * <p>
 * This service can be acquired from a part's service locator:
 * </p>
 *
 * <pre>
 * ISaveablesLifecycleListener listener = (ISaveablesLifecycleListener) getSite()
 * 		.getService(ISaveablesLifecycleListener.class);
 * </pre>
 *
 * or, in the case of implementers of {@link ISaveablesSource} that are not a
 * part, from the workbench:
 *
 * <pre>
 * ISaveablesLifecycleListener listener = (ISaveablesLifecycleListener) workbench
 * 		.getService(ISaveablesLifecycleListener.class);
 * </pre>
 *
 * <ul>
 * <li>This service is available globally.</li>
 * </ul>
 *
 * @since 3.2
 */
public interface ISaveablesLifecycleListener {

	/**
	 * Handle the given event. This method must be called on the UI thread.
	 *
	 * @param event
	 */
	void handleLifecycleEvent(SaveablesLifecycleEvent event);

}
