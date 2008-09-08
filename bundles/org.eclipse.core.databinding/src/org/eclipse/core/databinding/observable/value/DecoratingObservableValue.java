/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Matthew Hall - but 246626
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.DecoratingObservable;

/**
 * An observable value which decorates another observable value.
 * 
 * @since 1.2
 */
public class DecoratingObservableValue extends DecoratingObservable implements
		IObservableValue {
	private IObservableValue decorated;

	private IValueChangeListener valueChangeListener;

	/**
	 * Constructs a DecoratingObservableValue which decorates the given
	 * observable.
	 * 
	 * @param decorated
	 *            the observable value being decorated
	 * @param disposeDecoratedOnDispose 
	 */
	public DecoratingObservableValue(IObservableValue decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
		this.decorated = decorated;
	}

	public synchronized void addValueChangeListener(
			IValueChangeListener listener) {
		addListener(ValueChangeEvent.TYPE, listener);
	}

	public synchronized void removeValueChangeListener(
			IValueChangeListener listener) {
		removeListener(ValueChangeEvent.TYPE, listener);
	}

	protected void fireValueChange(ValueDiff diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new ValueChangeEvent(this, diff));
	}

	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireValueChange() instead"); //$NON-NLS-1$
	}

	protected void firstListenerAdded() {
		if (valueChangeListener == null) {
			valueChangeListener = new IValueChangeListener() {
				public void handleValueChange(ValueChangeEvent event) {
					DecoratingObservableValue.this.handleValueChange(event);
				}
			};
		}
		decorated.addValueChangeListener(valueChangeListener);
		super.firstListenerAdded();
	}

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
	protected void handleValueChange(final ValueChangeEvent event) {
		fireValueChange(event.diff);
	}

	public Object getValue() {
		getterCalled();
		return decorated.getValue();
	}

	public void setValue(Object value) {
		checkRealm();
		decorated.setValue(value);
	}

	public Object getValueType() {
		return decorated.getValueType();
	}

	public synchronized void dispose() {
		if (decorated != null && valueChangeListener != null) {
			decorated.removeValueChangeListener(valueChangeListener);
		}
		decorated = null;
		valueChangeListener = null;
		super.dispose();
	}
}
