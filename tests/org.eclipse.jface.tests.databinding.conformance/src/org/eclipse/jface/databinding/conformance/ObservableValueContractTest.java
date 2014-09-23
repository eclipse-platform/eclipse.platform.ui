/*******************************************************************************
 * Copyright (c) 2007, 2014 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 213145
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.jface.databinding.conformance.delegate.IObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;

/**
 * @since 3.2
 */
public class ObservableValueContractTest extends ObservableContractTest {
	private IObservableValueContractDelegate delegate;
	private IObservableValue observable;

	public ObservableValueContractTest(IObservableValueContractDelegate delegate) {
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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		observable = (IObservableValue) getObservable();
	}

	public void testChange_ValueChangeEvent() throws Exception {
		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);

		delegate.change(observable);
		assertEquals(
				formatFail("On change value change listeners should be notified."),
				1, listener.count);
	}

	public void testGetValueType_ExpectedType() throws Exception {
		assertEquals(
				formatFail("Type of the value should be returned from getType()."),
				delegate.getValueType(observable), observable.getValueType());
	}

	public void testChange_OrderOfNotifications() throws Exception {
		final List<IObservablesListener> listeners = new ArrayList<IObservablesListener>();
		IChangeListener changeListener = new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				listeners.add(this);
			}
		};

		IValueChangeListener valueChangeListener = new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				listeners.add(this);
			}
		};

		observable.addChangeListener(changeListener);
		observable.addValueChangeListener(valueChangeListener);

		delegate.change(observable);

		assertTrue(formatFail("Change Listeners were not notified on change."),
				listeners.size() > 0);

		// not asserting the fact that both are notified as this is asserted in
		// other tests
		assertEquals(
				formatFail("Change listeners should be notified before value change listeners."),
				changeListener, listeners.get(0));
		assertEquals(
				formatFail("Value change listeners should be notified after change listeners."),
				valueChangeListener, listeners.get(1));
	}

	public void testChange_ValueChangeEventDiff() throws Exception {
		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);
		Object oldValue = observable.getValue();

		delegate.change(observable);

		ValueChangeEvent event = listener.event;

		assertTrue(formatFail("Change Listeners were not notified on change."),
				listener.count > 0);

		assertEquals(
				formatFail("When a value change event is fired the old value should be the previous value of the observable value."),
				oldValue, event.diff.getOldValue());
		assertEquals(
				formatFail("When a value change event is fired the new value should be the same as the current value of the observable value."),
				observable.getValue(), event.diff.getNewValue());
	}

	public void testChange_ValueChangeEventFiredAfterValueIsSet()
			throws Exception {
		class ValueChangeListener implements IValueChangeListener {
			Object value;

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				this.value = event.getObservableValue().getValue();
			}
		}

		ValueChangeListener listener = new ValueChangeListener();
		observable.addValueChangeListener(listener);
		delegate.change(observable);
		assertEquals(
				formatFail("When a value change event is fired the new value should be applied before firing the change event."),
				listener.value, observable.getValue());
	}

	public void testRemoveValueChangeListener_RemovesListener()
			throws Exception {
		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);
		delegate.change(observable);

		// precondition
		assertEquals(
				formatFail("Value change listeners should be notified on change."),
				1, listener.count);

		observable.removeValueChangeListener(listener);
		delegate.change(observable);

		assertEquals(
				formatFail("Value change listeners should not be notified after they've been removed from the observable."),
				1, listener.count);
	}

	public void testGetValue_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			@Override
			public void run() {
				observable.getValue();
			}
		}, formatFail("IObservableValue.getValue()"), observable);
	}

	public void testGetValue_RealmCheck() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				observable.getValue();
			}
		}, (CurrentRealm) observable.getRealm());
	}

	public static Test suite(IObservableValueContractDelegate delegate) {
		return new SuiteBuilder().addObservableContractTest(
				ObservableValueContractTest.class, delegate).build();
	}
}
