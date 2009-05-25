/*******************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Matthew Hall - initial API and implementation (bug 180746)
 * 		Boris Bokowski, IBM - initial API and implementation
 * 		Matthew Hall - bugs 212223, 208332, 245647
 *  	Will Horn - bug 215297
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;

/**
 * {@link IObservableValue} implementation that wraps an
 * {@link IObservableValue} and delays notification of value change events from
 * the wrapped observable value until a certain time has passed since the last
 * change event. This class helps to boost performance in bindings (both in
 * validation and in event firing) when the observed value is rapidly changing.
 * A common use of this class is to delay validation until the user stops typing
 * in an UI field. To notify about pending changes, a DelayedObservableValue
 * fires a stale event when the wrapped observable value fires a change event,
 * and remains stale as long as a value change is pending.
 * 
 * Note that this class will not forward {@link ValueChangingEvent} events from
 * a wrapped {@link IVetoableValue}.
 * 
 * @since 1.2
 */
public class DelayedObservableValue extends AbstractObservableValue implements
		IStaleListener, IValueChangeListener {
	class ValueUpdater implements Runnable {
		private final Object oldValue;

		boolean cancel = false;
		boolean running = false;

		ValueUpdater(Object oldValue) {
			this.oldValue = oldValue;
		}

		void cancel() {
			cancel = true;
		}

		public void run() {
			if (!cancel)
				try {
					running = true;
					internalFireValueChange(oldValue);
				} finally {
					running = false;
				}
		}
	}

	private final int delay;
	private IObservableValue observable;

	private boolean dirty = true;
	private Object cachedValue = null;

	private boolean updating = false;

	private ValueUpdater updater = null;

	/**
	 * Constructs a new instance bound to the given
	 * <code>ISWTObservableValue</code> and configured to fire change events
	 * once there have been no value changes in the observable for
	 * <code>delay</code> milliseconds.
	 * 
	 * @param delayMillis
	 * @param observable
	 * @throws IllegalArgumentException
	 *             if <code>updateEventType</code> is an incorrect type.
	 */
	public DelayedObservableValue(int delayMillis, IObservableValue observable) {
		super(observable.getRealm());
		this.delay = delayMillis;
		this.observable = observable;

		observable.addValueChangeListener(this);
		observable.addStaleListener(this);

		cachedValue = doGetValue();
	}

	public void handleValueChange(ValueChangeEvent event) {
		if (!updating)
			makeDirty();
	}

	public void handleStale(StaleEvent staleEvent) {
		if (!updating)
			fireStale();
	}

	protected Object doGetValue() {
		if (dirty) {
			cachedValue = observable.getValue();
			dirty = false;

			if (updater != null && !updater.running) {
				fireValueChange(Diffs.createValueDiff(updater.oldValue,
						cachedValue));
				cancelScheduledUpdate();
			}
		}
		return cachedValue;
	}

	protected void doSetValue(Object value) {
		updating = true;
		try {
			// Principle of least surprise: setValue overrides any pending
			// update from observable.
			dirty = false;
			cancelScheduledUpdate();

			Object oldValue = cachedValue;
			observable.setValue(value);
			// Bug 215297 - target observable could veto or override value
			// passed to setValue(). Make sure we cache whatever is set.
			cachedValue = observable.getValue();

			if (!Util.equals(oldValue, cachedValue))
				fireValueChange(Diffs.createValueDiff(oldValue, cachedValue));
		} finally {
			updating = false;
		}
	}

	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return (dirty && updater != null) || observable.isStale();
	}

	/**
	 * Returns the type of the value from {@link #doGetValue()}, i.e.
	 * String.class
	 * 
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
	 */
	public Object getValueType() {
		return observable.getValueType();
	}

	public synchronized void dispose() {
		cancelScheduledUpdate();
		if (observable != null) {
			observable.dispose();
			observable = null;
		}
		super.dispose();
	}

	private void makeDirty() {
		if (!dirty) {
			dirty = true;
			fireStale();
		}
		cancelScheduledUpdate(); // if any
		scheduleUpdate();
	}

	private void cancelScheduledUpdate() {
		if (updater != null) {
			updater.cancel();
			updater = null;
		}
	}

	private void scheduleUpdate() {
		updater = new ValueUpdater(cachedValue);
		getRealm().timerExec(delay, updater);
	}

	private void internalFireValueChange(final Object oldValue) {
		cancelScheduledUpdate();
		fireValueChange(new ValueDiff() {
			public Object getOldValue() {
				return oldValue;
			}

			public Object getNewValue() {
				return getValue();
			}
		});
	}
}
