/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 265561, 262287, 268688
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.util.Objects;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @param <S>
 *            type of the source object
 * @param <T>
 *            type of the value of the property
 * @since 1.2
 */
public class SimplePropertyObservableValue<S, T> extends AbstractObservableValue<T>
		implements IPropertyObservable<SimpleValueProperty<S, T>> {
	private S source;
	private SimpleValueProperty<S, T> property;

	private boolean updating = false;
	private T cachedValue;
	private boolean stale;

	private INativePropertyListener<S> listener;

	public SimplePropertyObservableValue(Realm realm, S source,
			SimpleValueProperty<S, T> property) {
		super(realm);
		this.source = source;
		this.property = property;
	}

	@Override
	protected void firstListenerAdded() {
		if (!isDisposed() && listener == null) {
			listener = property.adaptListener(event -> {
				if (!isDisposed() && !updating) {
					getRealm().exec(() -> {
						if (event.type == SimplePropertyEvent.CHANGE) {
							notifyIfChanged(event.diff);
						} else if (event.type == SimplePropertyEvent.STALE && !stale) {
							stale = true;
							fireStale();
						}
					});
				}
			});
		}
		getRealm().exec(() -> {
			cachedValue = property.getValue(source);
			stale = false;
			if (listener != null)
				listener.addTo(source);
		});
	}

	@Override
	protected void lastListenerRemoved() {
		if (listener != null)
			listener.removeFrom(source);
		cachedValue = null;
		stale = false;
	}

	@Override
	protected T doGetValue() {
		notifyIfChanged(null);
		return property.getValue(source);
	}

	@Override
	protected void doSetValue(T value) {
		updating = true;
		try {
			property.setValue(source, value);
		} finally {
			updating = false;
		}

		notifyIfChanged(null);
	}

	private void notifyIfChanged(ValueDiff<? extends T> diff) {
		if (hasListeners()) {
			T oldValue = cachedValue;
			T newValue = cachedValue = property.getValue(source);
			if (diff == null)
				diff = Diffs.createValueDiff(oldValue, newValue);
			if (!Objects.equals(oldValue, newValue) || stale) {
				stale = false;
				fireValueChange(Diffs.unmodifiableDiff(diff));
			}
		}
	}

	@Override
	public Object getValueType() {
		return property.getValueType();
	}

	@Override
	public Object getObserved() {
		return source;
	}

	@Override
	public SimpleValueProperty<S, T> getProperty() {
		return property;
	}

	@Override
	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return stale;
	}

	@Override
	public synchronized void dispose() {
		if (!isDisposed()) {
			if (listener != null)
				listener.removeFrom(source);
			source = null;
			property = null;
			listener = null;
			stale = false;
		}
		super.dispose();
	}
}
