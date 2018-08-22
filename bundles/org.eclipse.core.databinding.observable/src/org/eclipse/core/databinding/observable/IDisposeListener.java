/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 146397)
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 * Listener for dispose events. An observable object is disposed if its
 * {@link IObservable#dispose()} method has been called.
 *
 * @since 1.2
 */
@FunctionalInterface
public interface IDisposeListener extends IObservablesListener {
	/**
	 * Handle the event that the given observable object has been disposed. The
	 * given event object must only be used locally in this method because it
	 * may be reused for other dispose notifications.
	 *
	 * @param event
	 */
	public void handleDispose(DisposeEvent event);
}
