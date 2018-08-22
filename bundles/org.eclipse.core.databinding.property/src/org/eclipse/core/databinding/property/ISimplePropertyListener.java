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
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 194734, 262287
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import org.eclipse.core.databinding.observable.IDiff;

/**
 * Listener for changes to properties on a particular source object
 *
 * @param <D>
 *            type of the diff handled by this listener
 * @param <S>
 *            type of the source object
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.2
 */
@FunctionalInterface
public interface ISimplePropertyListener<S, D extends IDiff> {
	/**
	 * Handle the described property event.
	 *
	 * @param event
	 *            the event which occurred
	 */
	public void handleEvent(SimplePropertyEvent<S, D> event);
}
