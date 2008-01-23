/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Matthew Hall - initial API and implementation (bug 180746)
 * 		Boris Bokowski, IBM - initial API and implementation
 * 		Matthew Hall - bug 212223
 *      Will Horn - bug 215297
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link IObservableValue} implementation that wraps any
 * {@link ISWTObservableValue} and delays notification of value change events
 * from the wrapped observable value until a certain time has passed since the
 * last change event, or until a FocusOut event is received from the underlying
 * widget (whichever happens earlier). This class helps to delay validation
 * until the user stops typing. To notify about pending changes, a delayed
 * observable value will fire a stale event when the wrapped observable value
 * fires a change event, but this change is being delayed.
 * 
 * Note that this class will not forward {@link ValueChangingEvent} events from
 * a wrapped {@link IVetoableValue}.
 * 
 * @since 1.2
 */
public class DelayedObservableValue extends AbstractSWTObservableValue {
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

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			if (!updating)
				fireStale();
		}
	};

	private IValueChangeListener valueChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			if (!updating)
				makeDirty();
		}
	};

	private Listener focusOutListener = new Listener() {
		public void handleEvent(Event event) {
			// Force update on focus out
			if (dirty)
				internalFireValueChange(cachedValue);
		}
	};

	private final int delay;
	private ISWTObservableValue observable;
	private Control control;

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
	public DelayedObservableValue(int delayMillis,
			ISWTObservableValue observable) {
		super(observable.getRealm(), observable.getWidget());
		this.delay = delayMillis;
		this.observable = observable;

		observable.addValueChangeListener(valueChangeListener);
		observable.addStaleListener(staleListener);
		Widget widget = observable.getWidget();
		if (widget instanceof Control) {
			control = (Control) widget;
			control.addListener(SWT.FocusOut, focusOutListener);
		}

		cachedValue = doGetValue();
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

	public void dispose() {
		cancelScheduledUpdate();
		if (observable != null) {
			observable.dispose();
			observable.removeValueChangeListener(valueChangeListener);
			observable.removeStaleListener(staleListener);
			observable = null;
		}
		if (control != null) {
			control.removeListener(SWT.FocusOut, focusOutListener);
			control = null;
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
		observable.getWidget().getDisplay().timerExec(delay, updater);
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
