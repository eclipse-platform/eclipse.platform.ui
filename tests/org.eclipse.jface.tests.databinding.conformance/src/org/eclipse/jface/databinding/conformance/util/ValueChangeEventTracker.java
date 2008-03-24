/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

/**
 * Listener for tracking the firing of ValueChangeEvents.
 */
public class ValueChangeEventTracker implements IValueChangeListener {
	public int count;

	public ValueChangeEvent event;

	public final List queue;

	public ValueChangeEventTracker() {
		this(null);
	}

	public ValueChangeEventTracker(List queue) {
		this.queue = queue;
	}

	public void handleValueChange(ValueChangeEvent event) {
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
	public static ValueChangeEventTracker observe(IObservableValue observable) {
		ValueChangeEventTracker tracker = new ValueChangeEventTracker();
		observable.addValueChangeListener(tracker);
		return tracker;
	}
}
