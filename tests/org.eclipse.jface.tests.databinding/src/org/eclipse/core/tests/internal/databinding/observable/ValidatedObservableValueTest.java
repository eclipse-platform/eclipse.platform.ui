/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.validation.ValidatedObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class ValidatedObservableValueTest extends AbstractDefaultRealmTestCase {
	private ValidatedObservableValue validated;
	private ObservableValueStub target;
	private IObservableValue validationStatus;

	private Object oldValue;
	private Object newValue;

	protected void setUp() throws Exception {
		super.setUp();
		oldValue = new Object();
		newValue = new Object();
		target = new ObservableValueStub(Realm.getDefault());
		target.setValue(oldValue);
		validationStatus = new WritableValue(ValidationStatus.ok(),
				IStatus.class);
		validated = new ValidatedObservableValue(target, validationStatus);
	}

	public void testConstructor_RequireObservablesOnSameRealm() {
		CurrentRealm realm1 = new CurrentRealm(true);
		CurrentRealm realm2 = new CurrentRealm(true);
		target = new ObservableValueStub(realm1);
		validationStatus = new WritableValue(realm2);
		try {
			new ValidatedObservableValue(target, validationStatus);
			fail("Expected exception--target and validation status should have the same realm");
		} catch (RuntimeException expected) {
		}
	}

	public void testIsStale_WhenTargetIsStale() {
		assertFalse(target.isStale());
		assertFalse(validated.isStale());

		target.fireStale();

		assertTrue(target.isStale());
		assertTrue(validated.isStale());
	}

	public void testIsStale_WhileChangesPending() {
		assertFalse(target.isStale());
		assertFalse(validated.isStale());

		validationStatus.setValue(ValidationStatus.error("error"));

		// The validated observable goes stale only when the target changes
		// value but the validation status is not OK.
		assertFalse(target.isStale());
		assertFalse(validated.isStale());

		target.setValue(newValue);

		assertFalse(target.isStale());
		assertTrue(validated.isStale());

		validationStatus.setValue(ValidationStatus.ok());

		assertFalse(validated.isStale());
	}

	public void testGetValueType_SameAsTarget() {
		assertEquals(target.getValueType(), validated.getValueType());
	}

	public void testGetValue_InitialValue() {
		assertEquals(oldValue, target.getValue());
		assertEquals(oldValue, validated.getValue());
	}

	public void testGetValue_WhileChangesPending() {
		assertEquals(oldValue, target.getValue());
		assertEquals(oldValue, validated.getValue());

		validationStatus.setValue(ValidationStatus.error("error"));

		assertEquals(oldValue, target.getValue());
		assertEquals(oldValue, validated.getValue());

		target.setValue(newValue);

		assertEquals(newValue, target.getValue());
		assertEquals(oldValue, validated.getValue());

		validationStatus.setValue(ValidationStatus.ok());

		assertEquals(newValue, validated.getValue());
	}

	public void testSetValue_PropagatesToTarget() {
		validated.setValue(newValue);

		assertEquals(newValue, validated.getValue());
		assertEquals(newValue, target.getValue());
	}

	public void testSetValue_PropagatesToTargetWhileStatusNotOK() {
		validationStatus.setValue(ValidationStatus.error("error"));

		validated.setValue(newValue);

		assertEquals(newValue, validated.getValue());
		assertEquals(newValue, target.getValue());
		assertFalse(validated.isStale());
	}

	public void testSetValue_CachesGetValueFromTarget() {
		Object overrideValue = target.overrideValue = new Object();

		assertEquals(oldValue, validated.getValue());
		assertEquals(oldValue, target.getValue());

		validationStatus.setValue(ValidationStatus.error("error"));

		validated.setValue(newValue);

		assertEquals(overrideValue, target.getValue());
		assertEquals(overrideValue, validated.getValue());
	}

	public void testSetValue_SingleValueChangeEvent() {
		ValueChangeEventTracker tracker = ValueChangeEventTracker
				.observe(validated);

		validated.setValue(newValue);
		assertEquals(1, tracker.count);
		assertEquals(oldValue, tracker.event.diff.getOldValue());
		assertEquals(newValue, tracker.event.diff.getNewValue());
	}

	public void testSetValue_SingleValueChangeEventWhileInvalid() {
		ValueChangeEventTracker tracker = ValueChangeEventTracker
				.observe(validated);

		validationStatus.setValue(ValidationStatus.error("error"));
		validated.setValue(newValue);
		assertEquals(1, tracker.count);
		assertEquals(oldValue, tracker.event.diff.getOldValue());
		assertEquals(newValue, tracker.event.diff.getNewValue());
	}

	public void testSetValue_FiresSingleValueChangeEventWithTargetOverride() {
		ValueChangeEventTracker tracker = ValueChangeEventTracker
				.observe(validated);

		Object overrideValue = new Object();
		target.overrideValue = overrideValue;
		validated.setValue(newValue);

		assertEquals(1, tracker.count);
		assertEquals(oldValue, tracker.event.diff.getOldValue());
		assertEquals(overrideValue, tracker.event.diff.getNewValue());
	}

	public void testSetValue_FiresValueChangeEvent() {
		ValueChangeEventTracker targetTracker = ValueChangeEventTracker
				.observe(target);
		ValueChangeEventTracker validatedTracker = ValueChangeEventTracker
				.observe(validated);

		validated.setValue(newValue);

		assertEquals(1, targetTracker.count);
		assertEquals(oldValue, targetTracker.event.diff.getOldValue());
		assertEquals(newValue, targetTracker.event.diff.getNewValue());

		assertEquals(1, validatedTracker.count);
		assertEquals(oldValue, validatedTracker.event.diff.getOldValue());
		assertEquals(newValue, validatedTracker.event.diff.getNewValue());
	}

	public void testIsStale_MatchTargetStaleness() {
		target.forceStale = true;
		target.fireStale();

		assertTrue(target.isStale());
		assertTrue(validated.isStale());

		target.setValue(newValue);

		assertTrue(target.isStale());
		assertTrue(validated.isStale());
	}

	static class ObservableValueStub extends AbstractObservableValue {
		private Object value;
		private boolean stale;
		private boolean forceStale;

		Object overrideValue;

		public ObservableValueStub(Realm realm) {
			super(realm);
		}

		protected Object doGetValue() {
			return value;
		}

		protected void doSetValue(Object value) {
			Object oldValue = this.value;
			if (overrideValue != null)
				value = overrideValue;
			this.value = value;
			stale = forceStale;
			fireValueChange(Diffs.createValueDiff(oldValue, value));
		}

		public Object getValueType() {
			return Object.class;
		}

		protected void fireStale() {
			stale = true;
			super.fireStale();
		}

		public boolean isStale() {
			return stale;
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ValidatedObservableValueTest.class.getName());
		suite.addTestSuite(ValidatedObservableValueTest.class);
		suite.addTest(MutableObservableValueContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		private Object valueType = new Object();

		public IObservableValue createObservableValue(Realm realm) {
			return new ValidatedObservableValueStub(realm, valueType);
		}

		public Object createValue(IObservableValue observable) {
			return new Object();
		}

		public Object getValueType(IObservableValue observable) {
			return valueType;
		}

		public void change(IObservable observable) {
			ValidatedObservableValueStub validated = (ValidatedObservableValueStub) observable;
			IObservableValue target = validated.target;
			target.setValue(createValue(validated));
		}
	}

	static class ValidatedObservableValueStub extends ValidatedObservableValue {
		final IObservableValue target;
		final IObservableValue validationStatus;

		ValidatedObservableValueStub(Realm realm, Object valueType) {
			this(new WritableValue(realm, null, valueType), new WritableValue(
					realm, ValidationStatus.ok(), IStatus.class));
		}

		private ValidatedObservableValueStub(IObservableValue target,
				IObservableValue validationStatus) {
			super(target, validationStatus);
			this.target = target;
			this.validationStatus = validationStatus;
		}
	}

}