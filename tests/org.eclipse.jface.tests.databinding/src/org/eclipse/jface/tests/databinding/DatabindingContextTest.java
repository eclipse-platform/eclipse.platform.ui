/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159539
 *     Brad Reynolds - bug 140644
 *     Brad Reynolds - bug 159940
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import junit.framework.TestCase;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.DefaultBindSpec;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.ListBinding;
import org.eclipse.core.internal.databinding.ValueBinding;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

public class DatabindingContextTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();

		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
	}

	public void testDisposeBindings() throws Exception {
		DataBindingContext dbc = new DataBindingContext();

		Binding binding = new BindingStub(dbc);
		dbc.addBinding(binding);

		assertFalse(binding.isDisposed());
		dbc.dispose();
		assertTrue("binding should be diposed when dbc is disposed", binding
				.isDisposed());
	}

	public void testBindValue() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		IObservableValue target = new WritableValue(String.class);
		IObservableValue model = new WritableValue(String.class);

		Binding binding = dbc.bindValue(target, model, null);
		assertTrue("binding is of the incorrect type",
				binding instanceof ValueBinding);
	}

	public void testBindList() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		IObservableList target = new WritableList();
		IObservableList model = new WritableList();

		Binding binding = dbc.bindList(target, model, null);
		assertTrue("binding is of the incorrect type",
				binding instanceof ListBinding);
	}

	/**
	 * Asserts that IStatus is populated and change events are fired when a
	 * Binding that is associated with a context is in error.
	 * 
	 * @throws Exception
	 */
	public void testValidationError() throws Exception {
		WritableValue targetObservable = new WritableValue(String.class);
		WritableValue modelObservable = new WritableValue(String.class);

		final String errorMessage = "error";
		DataBindingContext dbc = new DataBindingContext();
		ValueChangeCounter errorCounter = new ValueChangeCounter();
		ChangeCounter errorsCounter = new ChangeCounter();

		IObservableValue error = dbc.getValidationStatus();
		error.addValueChangeListener(errorCounter);
		assertTrue(((IStatus) error.getValue()).isOK());

		IObservableMap errors = dbc.getValidationStatusMap();
		errors.addChangeListener(errorsCounter);
		assertEquals(0, errors.size());

		IValidator validator = new IValidator() {
			public IStatus validate(Object value) {
				return ValidationStatus.error(errorMessage);
			}
		};

		dbc.bindValue(targetObservable, modelObservable, new DefaultBindSpec()
				.setTargetValidator(validator));

		targetObservable.setValue("");
		assertFalse(((IStatus) error.getValue()).isOK());
		assertEquals(errorMessage, ((IStatus) error.getValue()).getMessage());
		assertEquals(1, errors.size());
		assertEquals(1, errorsCounter.count);
		assertEquals(1, errorCounter.count);
	}

	/**
	 * Asserts that then
	 * {@link DataBindingContext#bindValue(IObservableValue, IObservableValue, org.eclipse.jface.databinding.DefaultBindSpec)}
	 * if invoked the created binding is added to the internal list of bindings.
	 * 
	 * @throws Exception
	 */
	public void testBindValueAddBinding() throws Exception {
		WritableValue targetValue = new WritableValue(String.class);
		WritableValue modelValue = new WritableValue(String.class);

		DataBindingContext dbc = new DataBindingContext();
		assertNotNull(dbc.getBindings());
		assertEquals(0, dbc.getBindings().size());

		Binding binding = dbc.bindValue(targetValue, modelValue, null);
		assertNotNull(binding);
		assertNotNull(dbc.getBindings());
		assertEquals(1, dbc.getBindings().size());
		assertEquals(binding, dbc.getBindings().get(0));
	}

	/**
	 * Asserts that when
	 * {@link DataBindingContext#bindList(IObservableList, IObservableList, org.eclipse.jface.databinding.DefaultBindSpec)}
	 * is invoked the created binding is added to the intenal list of bindings.
	 * 
	 * @throws Exception
	 */
	public void testBindListAddBinding() throws Exception {
		WritableList targetList = new WritableList(Object.class);
		WritableList modelList = new WritableList(Object.class);

		DataBindingContext dbc = new DataBindingContext();
		assertNotNull(dbc.getBindings());
		assertEquals(0, dbc.getBindings().size());

		Binding binding = dbc.bindList(targetList, modelList, null);
		assertNotNull(binding);
		assertNotNull(dbc.getBindings());
		assertEquals(1, dbc.getBindings().size());
		assertEquals(binding, dbc.getBindings().get(0));
	}

	public void testGetBindingsImmutability() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		BindingStub binding = new BindingStub(null);
		dbc.addBinding(binding);

		try {
			dbc.getBindings().remove(0);
			fail("exception should have been thrown");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void testRemoveBinding() throws Exception {
		BindingStub binding = new BindingStub(null);
		DataBindingContext dbc = new DataBindingContext();
		dbc.addBinding(binding);

		assertTrue("context should contain the binding", dbc.getBindings()
				.contains(binding));
		assertTrue("removing the factory should return true", dbc
				.removeBinding(binding));
		assertFalse("binding should have been removed", dbc.getBindings()
				.contains(binding));
		assertFalse("when not found false should be returned", dbc
				.removeBinding(binding));
	}

	/**
	 * {@link IValueChangeListener} implementation that counts the times
	 * handleValueChange(...) is invoked.
	 * 
	 * @since 3.2
	 */
	private static class ValueChangeCounter implements IValueChangeListener {
		int count;

		public void handleValueChange(ValueChangeEvent event) {
			count++;
		}
	}

	/**
	 * {@link IChangeListener} implementation that counts the times
	 * handleChange(...) is invoked.
	 * 
	 */
	private static class ChangeCounter implements IChangeListener {
		int count;

		public void handleChange(ChangeEvent event) {
			count++;
		}
	}

	private static class BindingStub extends Binding {
		DataBindingContext context;

		public BindingStub(DataBindingContext context) {
			super(context);
		}

		public IObservableValue getPartialValidationStatus() {
			return null;
		}

		public IObservableValue getValidationStatus() {
			return null;
		}

		public void updateModelFromTarget() {
		}

		public void updateTargetFromModel() {
		}
	}
}
