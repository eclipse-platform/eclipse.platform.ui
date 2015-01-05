/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 159539, 140644, 159940, 116920, 159768
 *     Matthew Hall - bugs 213145, 260329
 *******************************************************************************/
package org.eclipse.core.tests.databinding;

import java.util.ArrayList;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class DatabindingContextTest extends AbstractDefaultRealmTestCase {
	private DataBindingContext dbc;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		dbc = new DataBindingContext();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#tearDown
	 * ()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (dbc != null) {
			dbc.dispose();
		}
		super.tearDown();
	}

	public void testDisposeBindings() throws Exception {
		Binding binding = new BindingStub();
		binding.init(dbc);

		assertFalse(binding.isDisposed());
		dbc.dispose();
		assertTrue("binding should be diposed when dbc is disposed", binding
				.isDisposed());
	}

	public void testBindValue() throws Exception {
		IObservableValue target = WritableValue.withValueType(String.class);
		IObservableValue model = WritableValue.withValueType(String.class);

		Binding binding = dbc.bindValue(target, model);
		assertTrue("binding is of the incorrect type", binding.getClass()
				.getName().endsWith("ValueBinding"));
	}

	public void testBindList() throws Exception {
		IObservableList target = WritableList.withElementType(Object.class);
		IObservableList model = WritableList.withElementType(Object.class);

		Binding binding = dbc.bindList(target, model);
		assertTrue("binding is of the incorrect type", binding.getClass()
				.getName().endsWith("ListBinding"));
	}

	/**
	 * Asserts that IStatus is populated and change events are fired when a
	 * Binding that is associated with a context is in error.
	 *
	 * @throws Exception
	 */
	public void testValidationError() throws Exception {
		WritableValue targetObservable = WritableValue
				.withValueType(String.class);
		WritableValue modelObservable = WritableValue
				.withValueType(String.class);

		final String errorMessage = "error";
		ValueChangeEventTracker errorCounter = new ValueChangeEventTracker();
		ChangeEventTracker errorsCounter = new ChangeEventTracker();

		IObservableValue error = new AggregateValidationStatus(dbc
				.getBindings(), AggregateValidationStatus.MAX_SEVERITY);
		error.addValueChangeListener(errorCounter);
		assertTrue(((IStatus) error.getValue()).isOK());

		IObservableMap errors = dbc.getValidationStatusMap();
		errors.addChangeListener(errorsCounter);
		assertEquals(0, errors.size());

		IValidator validator = new IValidator() {
			@Override
			public IStatus validate(Object value) {
				return ValidationStatus.error(errorMessage);
			}
		};

		dbc
				.bindValue(targetObservable, modelObservable,
						new UpdateValueStrategy()
								.setAfterGetValidator(validator), null);

		targetObservable.setValue("");
		assertFalse(((IStatus) error.getValue()).isOK());
		assertEquals(errorMessage, ((IStatus) error.getValue()).getMessage());
		assertEquals(1, errors.size());
		assertEquals(1, errorsCounter.count);
		assertEquals(1, errorCounter.count);
		error.dispose();
	}

	/**
	 * Asserts that then
	 * {@link DataBindingContext#bindValue(IObservableValue, IObservableValue, UpdateValueStrategy, UpdateValueStrategy)}
	 * if invoked the created binding is added to the internal list of bindings.
	 *
	 * @throws Exception
	 */
	public void testBindValueAddBinding() throws Exception {
		WritableValue targetValue = WritableValue.withValueType(String.class);
		WritableValue modelValue = WritableValue.withValueType(String.class);

		assertNotNull(dbc.getBindings());
		assertEquals(0, dbc.getBindings().size());

		Binding binding = dbc.bindValue(targetValue, modelValue);
		assertNotNull(binding);
		assertNotNull(dbc.getBindings());
		assertEquals(1, dbc.getBindings().size());
		assertEquals(binding, dbc.getBindings().get(0));
	}

	/**
	 * Asserts that when
	 * {@link DataBindingContext#bindList(IObservableList, IObservableList, UpdateListStrategy, UpdateListStrategy)}
	 * is invoked the created binding is added to the intenal list of bindings.
	 *
	 * @throws Exception
	 */
	public void testBindListAddBinding() throws Exception {
		WritableList targetList = new WritableList(new ArrayList(),
				Object.class);
		WritableList modelList = new WritableList(new ArrayList(), Object.class);

		assertNotNull(dbc.getBindings());
		assertEquals(0, dbc.getBindings().size());

		Binding binding = dbc.bindList(targetList, modelList);
		assertNotNull(binding);
		assertNotNull(dbc.getBindings());
		assertEquals(1, dbc.getBindings().size());
		assertEquals(binding, dbc.getBindings().get(0));
	}

	public void testGetBindingsImmutability() throws Exception {
		BindingStub binding = new BindingStub();
		binding.init(dbc);

		try {
			dbc.getBindings().remove(0);
			fail("exception should have been thrown");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testRemoveBinding() throws Exception {
		BindingStub binding = new BindingStub();
		binding.init(dbc);

		assertTrue("context should contain the binding", dbc.getBindings()
				.contains(binding));
		binding.dispose();
		assertFalse("binding should have been removed", dbc.getBindings()
				.contains(binding));
	}

	/**
	 * Asserts that when a ValueBinding is created validation is ran to ensure
	 * that the validation status of the Binding reflects the validity of the
	 * value in the target.
	 *
	 * @throws Exception
	 */
	public void testValidateTargetAfterValueBindingCreation() throws Exception {
		WritableValue target = new WritableValue("", String.class);
		WritableValue model = new WritableValue("2", String.class);
		class Validator implements IValidator {
			@Override
			public IStatus validate(Object value) {
				return ValidationStatus.error("error");
			}
		}

		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy()
						.setAfterConvertValidator(new Validator()), null);

		assertEquals(IStatus.ERROR, ((IStatus) binding.getValidationStatus()
				.getValue()).getSeverity());
	}

	protected void assertNoErrorsFound() {
		IStatus status = AggregateValidationStatus.getStatusMaxSeverity(dbc
				.getBindings());
		assertTrue("No errors should be found, but found " + status, status
				.isOK());
	}

	protected void assertErrorsFound() {
		IStatus status = AggregateValidationStatus.getStatusMaxSeverity(dbc
				.getBindings());
		assertFalse("Errors should be found, but found none.", status.isOK());
	}

	private static class BindingStub extends Binding {

		public BindingStub() {
			super(new WritableValue(), new WritableValue());
		}

		@Override
		public IObservableValue getValidationStatus() {
			return null;
		}

		@Override
		public void updateTargetToModel() {
		}

		@Override
		public void updateModelToTarget() {
		}

		@Override
		protected void postInit() {
		}

		@Override
		protected void preInit() {
		}

		@Override
		public void validateModelToTarget() {
		}

		@Override
		public void validateTargetToModel() {
		}
	}
}
