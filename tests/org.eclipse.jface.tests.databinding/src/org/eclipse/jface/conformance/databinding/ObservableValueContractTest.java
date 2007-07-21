/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.conformance.databinding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

/**
 * @since 3.2
 */
public class ObservableValueContractTest extends ObservableContractTest {

	private IObservableValueContractDelegate delegate;

	public ObservableValueContractTest(
			IObservableValueContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	/**
	 * @param testName
	 * @param delegate
	 */
	public ObservableValueContractTest(String testName,
			IObservableValueContractDelegate delegate) {
		super(testName, delegate);

		this.delegate = delegate;
	}

	public void testChangeNotifiesValueChangeListeners() throws Exception {
		IObservableValue observable = delegate.createObservableValue();
		ValueChangeListener listener = new ValueChangeListener(observable)
				.init();

		delegate.change(observable);
		assertEquals("On change value change listeners should be notified.", 1,
				listener.count);
	}

	public void testSettingSameValueDoesNotNotifiyValueChangeListeners()
			throws Exception {
		IObservableValue observable = delegate.createObservableValue();
		delegate.change(observable);

		ValueChangeListener listener = new ValueChangeListener(observable)
				.init();
		Object value = observable.getValue();
		observable.setValue(value);

		assertEquals(
				"When the current value is set on the observable a change does not occur thus value change listeners should not be notified.",
				0, listener.count);
	}

	public void testSettingSameValueDoesNotNotifyChangeListeners()
			throws Exception {
		IObservableValue observable = delegate.createObservableValue();
		delegate.change(observable);

		ChangeListener listener = new ChangeListener(observable).init();
		Object value = observable.getValue();
		observable.setValue(value);

		assertEquals(
				"When the current value is set on the observable a change does not occur thus change listeners should not be notified.",
				0, listener.count);
	}

	public void testObservableTypeIsTheExpectedType() throws Exception {
		IObservableValue observable = delegate.createObservableValue();
		assertEquals("Type of the value should be returned from getType().",
				delegate.getValueType(observable), observable.getValueType());
	}

	public void testChangeListenersAreNotifiedBeforeValueChangeListeners()
			throws Exception {
		final List listeners = new ArrayList();
		IChangeListener changeListener = new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				listeners.add(this);
			}
		};

		IValueChangeListener valueChangeListener = new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				listeners.add(this);
			}
		};

		IObservableValue observable = delegate.createObservableValue();
		observable.addChangeListener(changeListener);
		observable.addValueChangeListener(valueChangeListener);

		delegate.change(observable);
		// not asserting the fact that both are notified as this is asserted in
		// other tests
		assertEquals(
				"Change listeners should be notified before value change listeners.",
				changeListener, listeners.get(0));
		assertEquals(
				"Value change listeners should be notified after change listeners.",
				valueChangeListener, listeners.get(1));
	}

	public void testValueChangeEventOldValueIsPreviousValue() throws Exception {
		IObservableValue observable = delegate.createObservableValue();
		ValueChangeListener listener = new ValueChangeListener(observable)
				.init();
		Object oldValue = observable.getValue();

		delegate.change(observable);

		ValueChangeEvent event = listener.event;
		assertEquals(
				"When a value change event is fired the old value should be the previous value of the observable value.",
				oldValue, event.diff.getOldValue());
	}

	public void testValueChangeEventObservableValue() throws Exception {
		IObservableValue observable = delegate.createObservableValue();
		ValueChangeListener listener = new ValueChangeListener(observable)
				.init();
		delegate.change(observable);

		ValueChangeEvent event = listener.event;
		assertEquals(
				"When a value change event is fired the new value should be the same as the current value of the observable value.",
				observable.getValue(), event.diff.getNewValue());
	}

	public void testRemoveValueChangeListenerRemovesListener() throws Exception {
		IObservableValue observable = delegate.createObservableValue();
		ValueChangeListener listener = new ValueChangeListener(observable)
				.init();
		delegate.change(observable);

		// precondition
		assertEquals("Value change listeners should be notified on change.", 1,
				listener.count);

		observable.removeValueChangeListener(listener);
		delegate.change(observable);

		assertEquals(
				"Value change listeners should not be notified after they've been removed from the observable.",
				1, listener.count);
	}
	
	public void testGetValue_GetterCalled() throws Exception {
		final IObservableValue observable = delegate.createObservableValue();
		assertGetterCalled(new Runnable() {
			public void run() {
				observable.getValue();
			}
		}, "IObservableValue.getValue()", observable);
	}

	/* package */static class ChangeListener implements IChangeListener {
		int count;

		IObservable observable;

		ChangeListener(IObservable observable) {
			this.observable = observable;
		}

		ChangeListener init() {
			observable.addChangeListener(this);
			return this;
		}

		public void handleChange(ChangeEvent event) {
			count++;
		}
	}

	/* package */static class ValueChangeListener implements
			IValueChangeListener {
		int count;

		ValueChangeEvent event;

		private IObservableValue observable;

		ValueChangeListener(IObservableValue observable) {
			this.observable = observable;
		}

		ValueChangeListener init() {
			observable.addValueChangeListener(this);
			return this;
		}

		public void handleValueChange(ValueChangeEvent event) {
			count++;
			this.event = event;
		}
	}
}
