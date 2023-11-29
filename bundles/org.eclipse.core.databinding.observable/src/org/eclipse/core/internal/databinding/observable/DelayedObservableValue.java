/*******************************************************************************
 * Copyright (c) 2007, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Matthew Hall - initial API and implementation (bug 180746)
 * 		Boris Bokowski, IBM - initial API and implementation
 * 		Matthew Hall - bugs 212223, 208332, 245647
 *  	Will Horn - bug 215297
 *  	Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Objects;

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
 * @param <T>
 *            the type of the object being observed
 *
 * @since 1.2
 */
public class DelayedObservableValue<T> extends AbstractObservableValue<T>
		implements IStaleListener, IValueChangeListener<T> {
	class ValueUpdater implements Runnable {
		private final T oldValue;

		boolean cancel = false;
		boolean running = false;

		ValueUpdater(T oldValue) {
			this.oldValue = oldValue;
		}

		void cancel() {
			cancel = true;
		}

		@Override
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
	private IObservableValue<T> observable;

	private boolean dirty = true;
	private T cachedValue = null;

	private boolean updating = false;

	private ValueUpdater updater = null;

	/**
	 * Constructs a new instance bound to the given
	 * <code>ISWTObservableValue</code> and configured to fire change events
	 * once there have been no value changes in the observable for
	 * <code>delay</code> milliseconds.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>updateEventType</code> is an incorrect type.
	 */
	public DelayedObservableValue(int delayMillis, IObservableValue<T> observable) {
		super(observable.getRealm());
		this.delay = delayMillis;
		this.observable = observable;

		observable.addValueChangeListener(this);
		observable.addStaleListener(this);

		cachedValue = doGetValue();
	}

	@Override
	public void handleValueChange(ValueChangeEvent<? extends T> event) {
		if (!updating)
			makeDirty();
	}

	@Override
	public void handleStale(StaleEvent staleEvent) {
		if (!updating)
			fireStale();
	}

	private T internalGetValue() {
		ObservableTracker.setIgnore(true);
		try {
			return observable.getValue();
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	protected T doGetValue() {
		if (dirty) {
			cachedValue = internalGetValue();
			dirty = false;

			if (updater != null && !updater.running) {
				fireValueChange(Diffs.createValueDiff(updater.oldValue,
						cachedValue));
				cancelScheduledUpdate();
			}
		}
		return cachedValue;
	}

	@Override
	protected void doSetValue(T value) {
		updating = true;
		try {
			// Principle of least surprise: setValue overrides any pending
			// update from observable.
			dirty = false;
			cancelScheduledUpdate();

			T oldValue = cachedValue;
			observable.setValue(value);
			// Bug 215297 - target observable could veto or override value
			// passed to setValue(). Make sure we cache whatever is set.
			cachedValue = internalGetValue();

			if (!Objects.equals(oldValue, cachedValue))
				fireValueChange(Diffs.createValueDiff(oldValue, cachedValue));
		} finally {
			updating = false;
		}
	}

	@Override
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
	@Override
	public Object getValueType() {
		return observable.getValueType();
	}

	@Override
	public synchronized void dispose() {
		cancelScheduledUpdate();
		if (observable != null) {
			observable.dispose();
			observable = null;
		}
		super.dispose();
	}

	private void makeDirty() {
		// Schedule updater before firing event
		cancelScheduledUpdate(); // if any
		scheduleUpdate();

		if (!dirty) {
			dirty = true;
			fireStale();
		}
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

	private void internalFireValueChange(final T oldValue) {
		cancelScheduledUpdate();
		fireValueChange(new ValueDiff<T>() {
			@Override
			public T getOldValue() {
				return oldValue;
			}

			@Override
			public T getNewValue() {
				return getValue();
			}
		});
	}
}
