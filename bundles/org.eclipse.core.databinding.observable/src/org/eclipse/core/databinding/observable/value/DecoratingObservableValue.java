/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Matthew Hall - but 246626
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.DecoratingObservable;
import org.eclipse.core.databinding.observable.Diffs;

/**
 * An observable value which decorates another observable value.
 *
 * @param <T>
 *            the type of value being observed
 *
 * @since 1.2
 */
public class DecoratingObservableValue<T> extends DecoratingObservable
		implements IObservableValue<T> {
	private IObservableValue<T> decorated;

	private IValueChangeListener<T> valueChangeListener;

	/**
	 * Constructs a DecoratingObservableValue which decorates the given
	 * observable.
	 *
	 * @param decorated
	 *            the observable value being decorated
	 * @param disposeDecoratedOnDispose
	 */
	public DecoratingObservableValue(IObservableValue<T> decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
		this.decorated = decorated;
	}

	@Override
	public synchronized void addValueChangeListener(IValueChangeListener<? super T> listener) {
		addListener(ValueChangeEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeValueChangeListener(IValueChangeListener<? super T> listener) {
		removeListener(ValueChangeEvent.TYPE, listener);
	}

	protected void fireValueChange(ValueDiff<T> diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new ValueChangeEvent<>(this, diff));
	}

	@Override
	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireValueChange() instead"); //$NON-NLS-1$
	}

	@Override
	protected void firstListenerAdded() {
		if (valueChangeListener == null) {
			valueChangeListener = new IValueChangeListener<T>() {
				@Override
				public void handleValueChange(ValueChangeEvent<? extends T> event) {
					DecoratingObservableValue.this.handleValueChange(event);
				}
			};
		}
		decorated.addValueChangeListener(valueChangeListener);
		super.firstListenerAdded();
	}

	@Override
	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		if (valueChangeListener != null) {
			decorated.removeValueChangeListener(valueChangeListener);
			valueChangeListener = null;
		}
	}

	/**
	 * Called whenever a ValueChangeEvent is received from the decorated
	 * observable. By default, this method fires the value change event again,
	 * with the decorating observable as the event source. Subclasses may
	 * override to provide different behavior.
	 *
	 * @param event
	 *            the change event received from the decorated observable
	 */
	protected void handleValueChange(final ValueChangeEvent<? extends T> event) {
		fireValueChange(Diffs.unmodifiableDiff(event.diff));
	}

	@Override
	public T getValue() {
		getterCalled();
		return decorated.getValue();
	}

	@Override
	public void setValue(T value) {
		checkRealm();
		decorated.setValue(value);
	}

	@Override
	public Object getValueType() {
		return decorated.getValueType();
	}

	@Override
	public synchronized void dispose() {
		if (decorated != null && valueChangeListener != null) {
			decorated.removeValueChangeListener(valueChangeListener);
		}
		decorated = null;
		valueChangeListener = null;
		super.dispose();
	}
}
