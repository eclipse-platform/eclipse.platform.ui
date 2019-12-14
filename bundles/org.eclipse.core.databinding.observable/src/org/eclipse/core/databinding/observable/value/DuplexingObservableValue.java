/*******************************************************************************
 * Copyright (c) 2009, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 175735)
 *     Matthew Hall - bug 262407
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * A class to reduce an observable list to a single value in an implementation
 * specific way.
 * <p>
 * A concrete implementation must implement
 * {@link #coalesceElements(Collection)} to specify how the current state of the
 * observed list is reduced to a single value.
 * </p>
 * <p>
 * For example the default implementation in
 * {@link #withDefaults(IObservableList, Object, Object)} will return a
 * predefined <i>empty value</i> in case the observed list is empty, an
 * predefined <i>multi value</i> if the list contains multiple different values
 * and the list value if it is the only value or all list values are equal.
 * </p>
 *
 * @param <T> the type of value being observed
 * @since 1.2
 */
public abstract class DuplexingObservableValue<T> extends AbstractObservableValue<T> {
	/**
	 * Returns a DuplexingObservableValue implementation with predefined values to
	 * use if the list is empty or contains multiple different values.
	 *
	 * @param <T>        the type of value being observed
	 *
	 * @param target     the observable list
	 * @param emptyValue the value to use when the target list is empty
	 * @param multiValue the value to use when the target list contains multiple
	 *                   values that are not equivalent to each other.
	 * @return a DuplexingObservableValue implementation with predefined values to
	 *         use if the list is empty or contains multiple different values.
	 */
	public static <T> DuplexingObservableValue<T> withDefaults(
			IObservableList<T> target, final T emptyValue, final T multiValue) {
		return new DuplexingObservableValue<T>(target) {
			@Override
			protected T coalesceElements(Collection<T> elements) {
				if (elements.isEmpty())
					return emptyValue;
				Iterator<T> it = elements.iterator();
				T first = it.next();
				while (it.hasNext())
					if (!Objects.equals(first, it.next()))
						return multiValue;
				return first;
			}
		};
	}

	private IObservableList<T> target;
	private final Object valueType;

	private boolean dirty = true;
	private boolean updating = false;
	private T cachedValue = null; // applicable only while hasListener()

	private PrivateInterface privateInterface;

	/**
	 * @param target the observed target list
	 */
	public DuplexingObservableValue(IObservableList<T> target) {
		this(target, target.getElementType());
	}

	/**
	 * @param target    the observed target list
	 * @param valueType the value type or <code>null</code>
	 */
	public DuplexingObservableValue(IObservableList<T> target, Object valueType) {
		super(target.getRealm());
		this.target = target;
		this.valueType = valueType;
	}

	private class PrivateInterface implements IChangeListener, IStaleListener {
		@Override
		public void handleChange(ChangeEvent event) {
			if (!updating)
				makeDirty();
		}

		@Override
		public void handleStale(StaleEvent staleEvent) {
			if (!dirty) {
				fireStale();
			}
		}
	}

	@Override
	protected void firstListenerAdded() {
		if (privateInterface == null)
			privateInterface = new PrivateInterface();
		target.addChangeListener(privateInterface);
		target.addStaleListener(privateInterface);
	}

	@Override
	protected void lastListenerRemoved() {
		target.removeChangeListener(privateInterface);
		target.removeStaleListener(privateInterface);
	}

	protected final void makeDirty() {
		if (hasListeners() && !dirty) {
			dirty = true;

			// copy the old value
			final T oldValue = cachedValue;
			// Fire the "dirty" event. This implementation recomputes the new
			// value lazily.
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

	@Override
	public boolean isStale() {
		getValue();
		return target.isStale();
	}

	@Override
	protected T doGetValue() {
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

	/**
	 * Called when the current value of this class is requested. The implementation
	 * must calculate a single value which should represent the given elements.
	 *
	 * @param elements the collection of elements to be coalesced
	 * @return the value representing the elements
	 */
	protected abstract T coalesceElements(Collection<T> elements);

	@Override
	protected void doSetValue(T value) {
		final T oldValue = cachedValue;

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

	@Override
	public Object getValueType() {
		return valueType;
	}

	@Override
	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeValueForListeners();
	}

	@Override
	public synchronized void addValueChangeListener(IValueChangeListener<? super T> listener) {
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
		getRealm().exec(() -> {
			// We are not currently listening.
			if (hasListeners()) {
				// But someone is listening for changes. Call getValue()
				// to make sure we start listening to the observables we
				// depend on.
				getValue();
			}
		});
	}

	@Override
	public synchronized void dispose() {
		if (privateInterface != null && target != null) {
			target.removeChangeListener(privateInterface);
		}
		if (privateInterface != null && target != null) {
			target.removeStaleListener(privateInterface);
		}
		target = null;
		privateInterface = null;
		super.dispose();
	}
}
