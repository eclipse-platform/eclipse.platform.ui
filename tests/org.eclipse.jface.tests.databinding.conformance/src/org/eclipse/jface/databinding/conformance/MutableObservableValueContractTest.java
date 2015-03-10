/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 213145
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.IObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;

/**
 * Mutability tests for IObservableValue.
 *
 * <p>
 * This class is experimental and can change at any time. It is recommended to
 * not subclass or assume the test names will not change. The only API that is
 * guaranteed to not change are the constructors. The tests will remain public
 * and not final in order to allow for consumers to turn off a test if needed by
 * subclassing.
 * </p>
 *
 * @since 3.2
 */
public class MutableObservableValueContractTest extends ObservableDelegateTest {
	private IObservableValueContractDelegate delegate;

	private IObservableValue observable;

	/**
	 * @param delegate
	 */
	public MutableObservableValueContractTest(
			IObservableValueContractDelegate delegate) {
		this(null, delegate);
	}

	public MutableObservableValueContractTest(String testName,
			IObservableValueContractDelegate delegate) {
		super(testName, delegate);
		this.delegate = delegate;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.observable = (IObservableValue) getObservable();
	}

	public void testSetValue_SetsValue() throws Exception {
		Object value = delegate.createValue(observable);

		observable.setValue(value);
		assertEquals(
				formatFail("IObservableValue.setValue(Object) should set the value of the observable."),
				value, observable.getValue());
	}

	public void testSetValue_ChangeEvent() throws Exception {
		ChangeEventTracker listener = ChangeEventTracker.observe(observable);

		observable.setValue(delegate.createValue(observable));

		assertEquals(formatFail("Change event listeners were not notified"), 1,
				listener.count);
		assertEquals(
				formatFail("IObservableValue.setValue(Object) should fire one ChangeEvent."),
				1, listener.count);
		assertEquals(
				formatFail("IObservableValue.setValue(Object)'s change event observable should be the created observable."),
				observable, listener.event.getObservable());
	}

	public void testSetValue_SameValue() throws Exception {
		// invoke change to ensure observable has a value
		delegate.change(observable);

		ValueChangeEventTracker valueChangeListener = ValueChangeEventTracker
				.observe(observable);
		ChangeEventTracker changeListener = ChangeEventTracker
				.observe(observable);
		Object value = observable.getValue();
		observable.setValue(value);

		assertEquals(
				formatFail("IObservableValue.setValue() should not fire a value change event when the value has not change."),
				0, valueChangeListener.count);
		assertEquals(
				formatFail("IObservableValue.setValue() should not fire a change event when the value has not change."),
				0, changeListener.count);
	}

	public void testSetValue_RealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				observable.setValue(delegate.createValue(observable));
			}
		}, (CurrentRealm) observable.getRealm());
	}

	public static Test suite(IObservableValueContractDelegate delegate) {
		return new SuiteBuilder()
				.addObservableContractTest(
						MutableObservableValueContractTest.class, delegate)
				.addObservableContractTest(ObservableValueContractTest.class,
						delegate).build();
	}
}
