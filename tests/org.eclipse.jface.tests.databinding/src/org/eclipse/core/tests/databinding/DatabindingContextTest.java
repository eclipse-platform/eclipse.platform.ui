/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *     Brad Reynolds - bugs 159539, 140644, 159940, 116920, 159768
 *     Matthew Hall - bugs 213145, 260329
 *******************************************************************************/
package org.eclipse.core.tests.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabindingContextTest extends AbstractDefaultRealmTestCase {
	private DataBindingContext dbc;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		dbc = new DataBindingContext();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if (dbc != null) {
			dbc.dispose();
		}
		super.tearDown();
	}

	@Test
	public void testDisposeBindings() throws Exception {
		Binding binding = new BindingStub();
		binding.init(dbc);

		assertFalse(binding.isDisposed());
		dbc.dispose();
		assertTrue("binding should be diposed when dbc is disposed", binding
				.isDisposed());
	}

	@Test
	public void testBindValue() throws Exception {
		IObservableValue<String> target = WritableValue.withValueType(String.class);
		IObservableValue<String> model = WritableValue.withValueType(String.class);

		Binding binding = dbc.bindValue(target, model);
		assertTrue("binding is of the incorrect type", binding.getClass()
				.getName().endsWith("ValueBinding"));
	}

	@Test
	public void testBindList() throws Exception {
		IObservableList<Object> target = WritableList.withElementType(Object.class);
		IObservableList<Object> model = WritableList.withElementType(Object.class);

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
	@Test
	public void testValidationError() throws Exception {
		WritableValue<String> targetObservable = WritableValue
				.withValueType(String.class);
		WritableValue<String> modelObservable = WritableValue
				.withValueType(String.class);

		final String errorMessage = "error";
		ValueChangeEventTracker<IStatus> errorCounter = new ValueChangeEventTracker<>();
		ChangeEventTracker errorsCounter = new ChangeEventTracker();

		IObservableValue<IStatus> error = new AggregateValidationStatus(dbc
				.getBindings(), AggregateValidationStatus.MAX_SEVERITY);
		error.addValueChangeListener(errorCounter);
		assertTrue(error.getValue().isOK());

		IObservableMap<Binding, IStatus> errors = dbc.getValidationStatusMap();
		errors.addChangeListener(errorsCounter);
		assertEquals(0, errors.size());

		IValidator<String> validator = value -> ValidationStatus.error(errorMessage);

		dbc.bindValue(targetObservable, modelObservable,
				new UpdateValueStrategy<String, String>().setAfterGetValidator(validator), null);

		targetObservable.setValue("");
		assertFalse(error.getValue().isOK());
		assertEquals(errorMessage, error.getValue().getMessage());
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
	@Test
	public void testBindValueAddBinding() throws Exception {
		WritableValue<String> targetValue = WritableValue.withValueType(String.class);
		WritableValue<String> modelValue = WritableValue.withValueType(String.class);

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
	@Test
	public void testBindListAddBinding() throws Exception {
		WritableList<Object> targetList = new WritableList<>(new ArrayList<>(), Object.class);
		WritableList<Object> modelList = new WritableList<>(new ArrayList<>(), Object.class);

		assertNotNull(dbc.getBindings());
		assertEquals(0, dbc.getBindings().size());

		Binding binding = dbc.bindList(targetList, modelList);
		assertNotNull(binding);
		assertNotNull(dbc.getBindings());
		assertEquals(1, dbc.getBindings().size());
		assertEquals(binding, dbc.getBindings().get(0));
	}

	@Test
	public void testGetBindingsImmutability() throws Exception {
		BindingStub binding = new BindingStub();
		binding.init(dbc);

		try {
			dbc.getBindings().remove(0);
			fail("exception should have been thrown");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
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
	@Test
	public void testValidateTargetAfterValueBindingCreation() throws Exception {
		WritableValue<String> target = new WritableValue<>("", String.class);
		WritableValue<String> model = new WritableValue<>("2", String.class);

		// Test contra-variant validator type on setAfterConvertValidator by
		// using Object as type argument
		class Validator implements IValidator<Object> {
			@Override
			public IStatus validate(Object value) {
				return ValidationStatus.error("error");
			}
		}

		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy<String, String>().setAfterConvertValidator(new Validator()), null);

		assertEquals(IStatus.ERROR, binding.getValidationStatus()
				.getValue().getSeverity());
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
			super(new WritableValue<>(), new WritableValue<>());
		}

		@Override
		public IObservableValue<IStatus> getValidationStatus() {
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
