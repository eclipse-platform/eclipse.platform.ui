/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 175735)
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import java.util.Collection;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * @since 1.2
 */
public abstract class DuplexingObservableValue extends AbstractObservableValue {
	private IObservableList target;
	private final Object valueType;

	private boolean dirty = true;
	private boolean updating = false;
	private Object cachedValue = null; // applicable only while hasListener()

	private PrivateInterface privateInterface;

	/**
	 * @param target
	 */
	public DuplexingObservableValue(IObservableList target) {
		this(target, target.getElementType());
	}

	/**
	 * @param target
	 * @param valueType
	 */
	public DuplexingObservableValue(IObservableList target, Object valueType) {
		super(target.getRealm());
		this.target = target;
		this.valueType = valueType;
	}

	private class PrivateInterface implements IChangeListener, IStaleListener {
		public void handleChange(ChangeEvent event) {
			if (!updating)
				makeDirty();
		}

		public void handleStale(StaleEvent staleEvent) {
			if (!dirty) {
				fireStale();
			}
		}
	}

	protected void firstListenerAdded() {
		if (privateInterface == null)
			privateInterface = new PrivateInterface();
		target.addChangeListener(privateInterface);
		target.addStaleListener(privateInterface);
	}

	protected void lastListenerRemoved() {
		target.removeChangeListener(privateInterface);
		target.removeStaleListener(privateInterface);
	}

	protected final void makeDirty() {
		if (hasListeners() && !dirty) {
			dirty = true;

			// copy the old value
			final Object oldValue = cachedValue;
			// Fire the "dirty" event. This implementation recomputes the new
			// value lazily.
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

	public boolean isStale() {
		getValue();
		return target.isStale();
	}

	protected Object doGetValue() {
		if (!hasListeners())
			return coalesceElements(target);

		if (dirty) {
			cachedValue = coalesceElements(target);
			dirty = false;
			if (target.isStale())
				fireStale();
		}

		return cachedValue;
	}

	protected abstract Object coalesceElements(Collection elements);

	protected void doSetValue(Object value) {
		final Object oldValue = cachedValue;

		boolean wasUpdating = updating;
		try {
			updating = true;
			for (int i = 0; i < target.size(); i++)
				target.set(i, value);
		} finally {
			updating = wasUpdating;
		}

		// Fire the "dirty" event. This implementation recomputes the new
		// value lazily.
		if (hasListeners()) {
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

	public Object getValueType() {
		return valueType;
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeValueForListeners();
	}

	public synchronized void addValueChangeListener(
			IValueChangeListener listener) {
		super.addValueChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeValueForListeners();
	}

	/**
	 * Some clients just add a listener and expect to get notified even if they
	 * never called getValue(), so we have to call getValue() ourselves here to
	 * be sure. Need to be careful about realms though, this method can be
	 * called outside of our realm. See also bug 198211. If a client calls this
	 * outside of our realm, they may receive change notifications before the
	 * runnable below has been executed. It is their job to figure out what to
	 * do with those notifications.
	 */
	private void computeValueForListeners() {
		getRealm().exec(new Runnable() {
			public void run() {
				// We are not currently listening.
				if (hasListeners()) {
					// But someone is listening for changes. Call getValue()
					// to make sure we start listening to the observables we
					// depend on.
					getValue();
				}
			}
		});
	}

	public synchronized void dispose() {
		if (privateInterface != null && target != null) {
			target.removeChangeListener(privateInterface);
			target.removeStaleListener(privateInterface);
		}
		target = null;
		privateInterface = null;
		super.dispose();
	}
}
