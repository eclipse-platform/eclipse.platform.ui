/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 213145
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.IObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.junit.Before;
import org.junit.Test;

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
public class MutableObservableValueContractTest extends ObservableValueContractTest {
	private final IObservableValueContractDelegate delegate;

	private IObservableValue observable;

	public MutableObservableValueContractTest(IObservableValueContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.observable = (IObservableValue) getObservable();
	}

	@Test
	public void testSetValue_SetsValue() throws Exception {
		Object value = delegate.createValue(observable);

		observable.setValue(value);
		assertEquals(
				formatFail("IObservableValue.setValue(Object) should set the value of the observable."),
				value, observable.getValue());
	}

	@Test
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

	@Test
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

	@Test
	public void testSetValue_RealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> observable.setValue(delegate.createValue(observable)),
				(CurrentRealm) observable.getRealm());
	}
}
