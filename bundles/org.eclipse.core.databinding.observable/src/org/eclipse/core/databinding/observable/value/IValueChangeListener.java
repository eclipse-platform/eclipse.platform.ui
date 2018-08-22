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
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.IObservablesListener;

/**
 * Listener for changes to observable values.
 *
 * @param <T>
 *            the type of value being observed
 *
 * @since 1.0
 *
 */
@FunctionalInterface
public interface IValueChangeListener<T> extends IObservablesListener {

	/**
	 * Handles a change to an observable value. The given event object must only
	 * be used locally in this method because it may be reused for other change
	 * notifications. The diff object referenced by the event is immutable and
	 * may be used non-locally.
	 *
	 * @param event
	 *            the event
	 */
	void handleValueChange(ValueChangeEvent<? extends T> event);
}
