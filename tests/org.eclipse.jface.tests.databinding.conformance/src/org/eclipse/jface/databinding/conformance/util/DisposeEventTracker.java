/*******************************************************************************
 * Copyright (c) 2008, 2014 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 146397)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/
package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservablesListener;

/**
 * Listener for tracking the firing of DisposeEvents.
 */
public class DisposeEventTracker implements IDisposeListener {
	public int count;
	public DisposeEvent event;

	/**
	 * Queue that the listener will add itself too when it is notified of an
	 * event. Used to determine order of notifications of listeners. Can be
	 * null.
	 */
	public final List<IObservablesListener> queue;

	public DisposeEventTracker() {
		queue = null;
	}

	public DisposeEventTracker(List<IObservablesListener> notificationQueue) {
		this.queue = notificationQueue;
	}

	@Override
	public void handleDispose(DisposeEvent event) {
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
	public static DisposeEventTracker observe(IObservable observable) {
		DisposeEventTracker tracker = new DisposeEventTracker();
		observable.addDisposeListener(tracker);
		return tracker;
	}
}
