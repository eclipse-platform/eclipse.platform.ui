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

package org.eclipse.core.databinding.observable;

/**
 * Listener for staleness events. An observable object is stale if its state
 * will change eventually.
 *
 * @since 1.0
 */
@FunctionalInterface
public interface IStaleListener extends IObservablesListener {

	/**
	 * Handle the event that the given observable object is now stale. The given
	 * event object must only be used locally in this method because it may be
	 * reused for other change notifications.
	 *
	 * @param staleEvent
	 */
	public void handleStale(StaleEvent staleEvent);

}
