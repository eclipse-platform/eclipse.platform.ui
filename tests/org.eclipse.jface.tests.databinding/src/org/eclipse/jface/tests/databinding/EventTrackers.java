/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

/**
 * Implementations of change listeners that keep track of the number of times an
 * event fires.
 * 
 * @since 1.1
 */
public class EventTrackers {
	public static class ChangeEventTracker implements IChangeListener {
		public int count;

		public ChangeEvent event;

		/**
		 * Queue that the listener will add itself too when it is notified of an
		 * event. Used to determine order of notifications of listeners.
		 */
		public final List notificationQueue;

		public ChangeEventTracker() {
			notificationQueue = null;
		}

		public ChangeEventTracker(List notificationQueue) {
			this.notificationQueue = notificationQueue;
		}

		public void handleChange(ChangeEvent event) {
			count++;
			this.event = event;
			if (notificationQueue != null) {
				notificationQueue.add(this);
			}
		}
		
		/**
		 * Convenience method to register the listener on an observable allowing for one line setup.
		 * <pre><code>
		 * ChangeEventTracker listener = new ChangeEventTracker().register(observable);
		 * </code></pre>
		 * 
		 * @param observable
		 * @return
		 */
		public ChangeEventTracker register(IObservable observable) {
			observable.addChangeListener(this);
			return this;
		}
	}

	public static class ValueChangeEventTracker implements IValueChangeListener {
		public int count;

		public ValueChangeEvent event;

		public ValueChangeEventTracker() {
		}

		public void handleValueChange(ValueChangeEvent event) {
			count++;
			this.event = event;
		}
		
		/**
		 * Convenience method to register the listener on an observable allowing for one line setup.
		 * <pre><code>
		 * ValueChangeEventTracker listener = new ValueChangeEventTracker().register(observable);
		 * </code></pre>
		 * 
		 * @param observable
		 * @return
		 */
		public ValueChangeEventTracker register(IObservableValue observable) {
			observable.addValueChangeListener(this);
			return this;
		}
	}

	public static class MapChangeEventTracker implements IMapChangeListener {
		public int count;

		public MapChangeEvent event;

		public MapChangeEventTracker() {
		}

		public void handleMapChange(MapChangeEvent event) {
			count++;
			this.event = event;
		}
	}

	public static class ListChangeEventTracker implements IListChangeListener {
		public int count;

		public ListChangeEvent event;
		
		/**
		 * Queue that the listener will add itself too when it is notified of an
		 * event. Used to determine order of notifications of listeners.
		 */
		public final List notificationQueue;

		public ListChangeEventTracker() {
			notificationQueue = null;
		}
		
		public ListChangeEventTracker(List notificationQueue) {
			this.notificationQueue = notificationQueue;
		}

		public void handleListChange(ListChangeEvent event) {
			count++;
			this.event = event;
			if (notificationQueue != null) {
				notificationQueue.add(this);
			}
		}
	}
	
	public static class SetChangeEventTracker implements ISetChangeListener {
		public int count;

		public SetChangeEvent event;
		
		/**
		 * Queue that the listener will add itself too when it is notified of an
		 * event. Used to determine order of notifications of listeners.
		 */
		public final List notificationQueue;

		public SetChangeEventTracker() {
			notificationQueue = null;
		}
		
		public SetChangeEventTracker(List notificationQueue) {
			this.notificationQueue = notificationQueue;
		}

		public void handleSetChange(SetChangeEvent event) {
			count++;
			this.event = event;
			if (notificationQueue != null) {
				notificationQueue.add(this);
			}
		}
	}
}
