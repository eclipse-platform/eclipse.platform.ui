/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/
package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

/**
 * Listener for tracking the firing of ValueChangeEvents.
 */
public class ValueChangeEventTracker<T> implements IValueChangeListener<T> {
	public int count;

	public ValueChangeEvent<? extends T> event;

	public final List<IObservablesListener> queue;

	public ValueChangeEventTracker() {
		this(null);
	}

	public ValueChangeEventTracker(List<IObservablesListener> queue) {
		this.queue = queue;
	}

	@Override
	public void handleValueChange(ValueChangeEvent<? extends T> event) {
		count++;
		this.event = event;

		if (queue != null) {
			queue.add(this);
		}
	}

	/**
	 * Convenience method to register a new listener.
	 *
	 * @param observable
	 * @return tracker
	 */
	public static <T> ValueChangeEventTracker<T> observe(IObservableValue<T> observable) {
		ValueChangeEventTracker<T> tracker = new ValueChangeEventTracker<>();
		observable.addValueChangeListener(tracker);
		return tracker;
	}
}
