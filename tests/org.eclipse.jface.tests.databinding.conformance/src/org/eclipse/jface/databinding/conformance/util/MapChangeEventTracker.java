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
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;

/**
 * Listener for tracking the firing of ChangeEvents.
 */
public class MapChangeEventTracker<K, V> implements IMapChangeListener<K, V> {
	public int count;

	public MapChangeEvent<? extends K, ? extends V> event;

	public List<IObservablesListener> queue;

	public MapChangeEventTracker() {
		this(null);
	}

	public MapChangeEventTracker(List<IObservablesListener> queue) {
		this.queue = queue;
	}

	@Override
	public void handleMapChange(MapChangeEvent<? extends K, ? extends V> event) {
		count++;
		this.event = event;

		if (queue != null) {
			queue.add(this);
		}
	}

	/**
	 * Convenience method to register a new listener.
	 *
	 * @return tracker
	 */
	public static <K, V> MapChangeEventTracker<K, V> observe(IObservableMap<K, V> observable) {
		MapChangeEventTracker<K, V> tracker = new MapChangeEventTracker<>();
		observable.addMapChangeListener(tracker);
		return tracker;
	}
}
