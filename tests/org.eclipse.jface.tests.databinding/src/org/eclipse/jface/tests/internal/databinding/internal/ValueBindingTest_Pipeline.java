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

package org.eclipse.jface.tests.internal.databinding.internal;

import java.util.Arrays;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.IBindingListener;
import org.eclipse.core.databinding.observable.value.AbstractVetoableValue;
import org.eclipse.core.databinding.observable.value.ChangeVetoException;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.ValueBinding;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackLastListener;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackPositionListener;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackedValidator;

/**
 * Asserts the pipeline behavior of ValueBinding.
 * 
 * @since 3.2
 */
public class ValueBindingTest_Pipeline extends AbstractDefaultRealmTestCase {
	private IObservableValue target;
	private IObservableValue model;
	private DataBindingContext dbc;

	protected void setUp() throws Exception {
		super.setUp();

		target = new WritableValue(String.class, null);
		model = new WritableValue(String.class, null);
		dbc = new DataBindingContext();
	}

	public void testTargetToModelPipelinePositionOrder() throws Exception {
		int[] positions = new int[] { BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.PIPELINE_AFTER_CHANGE };
		int[] copyTypes = new int[positions.length];
		Arrays.fill(copyTypes, BindingEvent.EVENT_COPY_TO_MODEL);

		TrackPositionListener listener = new TrackPositionListener(
				positions.length);

		Binding binding = new ValueBinding(target, model, new BindSpec());
		binding.init(dbc);

		binding.addBindingEventListener(listener);

		listener.reset(); // reset, runs on bind

		target.setValue("1");
		assertEquals(positions.length, listener.count);
		assertTrue("positions", Arrays.equals(positions, listener.positions));
		assertTrue("copy types", Arrays.equals(copyTypes, listener.copyTypes));
	}

	public void testTargetToModelValidationAfterGet() throws Exception {
		assertNull(assertValidation(BindingEvent.PIPELINE_AFTER_GET, -1,
				BindingEvent.EVENT_COPY_TO_MODEL));
	}

	public void testTargetToModelValidationAfterConvert() throws Exception {
		assertNull(assertValidation(BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.EVENT_COPY_TO_MODEL));
	}

	public void testTargetToModelValidationBeforeGet() throws Exception {
		assertNull(assertValidation(BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.EVENT_COPY_TO_MODEL));
	}

	private Exception assertValidation(int position, int previousPosition,
			int copyType) throws Exception {
		TrackLastListener listener = new TrackLastListener();
		listener.active = false;
		TrackedValidator validator = new TrackedValidator(listener);

		IObservableValue value = null;
		BindSpec bindSpec = new BindSpec();
		switch (copyType) {
		case BindingEvent.EVENT_COPY_TO_TARGET:
			bindSpec.addModelValidator(position, validator);
			value = model;
			break;
		case BindingEvent.EVENT_COPY_TO_MODEL:
			bindSpec.addTargetValidator(position, validator);
			value = target;
			break;
		}

		ValueBinding binding = new ValueBinding(target, model, bindSpec);
		binding.init(dbc);

		binding.addBindingEventListener(listener);
		listener.active = true;

		Exception exception = null;
		try {
			value.setValue("1");
		} catch (Exception e) {
			exception = e;
		}

		assertEquals("validator", validator, listener.lastValidator);
		assertEquals("validator invocation count", 1, validator.count);
		assertEquals("last binding event position", previousPosition,
				listener.lastPosition);
		return exception;
	}

	public void testModelToTargetPipelinePositionOrder() throws Exception {
		int[] positions = new int[] { BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.PIPELINE_BEFORE_CHANGE };
		int[] copyTypes = new int[positions.length];
		Arrays.fill(copyTypes, BindingEvent.EVENT_COPY_TO_TARGET);

		TrackPositionListener listener = new TrackPositionListener(
				positions.length);

		ValueBinding binding = new ValueBinding(target, model,
				new BindSpec());
		binding.init(dbc);

		binding.addBindingEventListener(listener);

		model.setValue("1");
		assertEquals(3, listener.count);
		assertTrue("positions", Arrays.equals(positions, listener.positions));
		assertTrue("copy types", Arrays.equals(copyTypes, listener.copyTypes));
	}

	public void testModelToTargetValidationAfterGet() throws Exception {
		assertNull(assertValidation(BindingEvent.PIPELINE_AFTER_GET, -1,
				BindingEvent.EVENT_COPY_TO_TARGET));
	}

	public void testModelToTargetValidationAfterConvert() throws Exception {
		assertNull(assertValidation(BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.EVENT_COPY_TO_TARGET));
	}

	public void testModelToTargetValidationBeforeChange() throws Exception {
		assertNull(assertValidation(BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.EVENT_COPY_TO_TARGET));
	}

	public void testUpdateModelFromTargetAfterGet() throws Exception {
		assertLastPosition(BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.EVENT_COPY_TO_MODEL);
	}

	public void testUpdateModelFromTargetAfterConvert() throws Exception {
		assertLastPosition(BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.EVENT_COPY_TO_MODEL);
	}

	public void testUpdateModelFromTargetBeforeChange() throws Exception {
		assertLastPosition(BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.EVENT_COPY_TO_MODEL);
	}

	public void testUpdateModelFromTargetAfterChange() throws Exception {
		assertLastPosition(BindingEvent.PIPELINE_AFTER_CHANGE,
				BindingEvent.EVENT_COPY_TO_MODEL);
	}

	public void testUpdateTargetFromModelAfterGet() throws Exception {
		assertLastPosition(BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.EVENT_COPY_TO_TARGET);
	}

	public void testUpdateTargetFromModelAfterConvert() throws Exception {
		assertLastPosition(BindingEvent.PIPELINE_AFTER_CONVERT,
				BindingEvent.EVENT_COPY_TO_TARGET);
	}

	public void testUpdateTargetFromModelAfterChange() throws Exception {
		assertLastPosition(BindingEvent.PIPELINE_AFTER_CHANGE,
				BindingEvent.EVENT_COPY_TO_TARGET);
	}

	private void assertLastPosition(int position, int copyType) {
		TrackLastListener listener = new TrackLastListener();

		ValueBinding binding = new ValueBinding(target, model,
				new BindSpec());
		binding.init(dbc);

		binding.addBindingEventListener(listener);
		listener.active = true;

		switch (copyType) {
		case BindingEvent.EVENT_COPY_TO_MODEL:
			binding.updateModelFromTarget(position);
			break;
		case BindingEvent.EVENT_COPY_TO_TARGET:
			binding.updateTargetFromModel(position);
			break;
		}

		assertEquals("copyType", copyType, listener.lastCopyType);
		assertEquals("last position", position, listener.lastPosition);
	}

	public void testValidationErrorStatusValidatorFailure() throws Exception {
		class Validator implements IValidator {
			public IStatus validate(Object value) {
				return Status.CANCEL_STATUS;
			}
		}

		Validator validator = new Validator();
		ValueBinding binding = new ValueBinding(target, model,
				new BindSpec().addTargetValidator(
						BindingEvent.PIPELINE_AFTER_GET, validator));
		binding.init(dbc);

		assertTrue(((IStatus) binding.getValidationStatus().getValue()).isOK());
		target.setValue("value");
		assertFalse("status should be in error", ((IStatus) binding
				.getValidationStatus().getValue()).isOK());
	}

	public void testValidationErrorStatusListenerFailure() throws Exception {
		class Listener implements IBindingListener {
			public IStatus handleBindingEvent(BindingEvent e) {
				return Status.CANCEL_STATUS;
			}
		}

		ValueBinding binding = new ValueBinding(target, model,
				new BindSpec());
		binding.init(dbc);

		binding.addBindingEventListener(new Listener());

		assertTrue(((IStatus) binding.getValidationStatus().getValue()).isOK());
		target.setValue("value");
		assertFalse("status should be in error", ((IStatus) binding
				.getValidationStatus().getValue()).isOK());
	}

	public void testTargetToModelPartialValidation() throws Exception {
		target = new VetoableValueStub();
		Exception e = assertValidation(BindingEvent.PIPELINE_VALUE_CHANGING,
				-1, BindingEvent.EVENT_COPY_TO_MODEL);
		assertNotNull("exception should have been thrown", e);
		assertTrue("ChangeVetoException", e instanceof ChangeVetoException);
	}

	public void testPartialValidationErrorStatusValidatorFailure()
			throws Exception {
		target = new VetoableValueStub();

		class Listener implements IBindingListener {
			public IStatus handleBindingEvent(BindingEvent e) {
				return Status.CANCEL_STATUS;
			}
		}

		ValueBinding binding = new ValueBinding(target, model, new BindSpec());
		binding.init(dbc);
		binding.addBindingEventListener(new Listener());
		assertTrue(((IStatus) binding.getPartialValidationStatus().getValue())
				.isOK());
		
		try {
			target.setValue("1");
		} catch (ChangeVetoException e) {
		}
		
		assertFalse("status should be in error", ((IStatus) binding
				.getPartialValidationStatus().getValue()).isOK());
	}

	static class VetoableValueStub extends AbstractVetoableValue {
		Object value;

		protected void doSetApprovedValue(Object value) {
			this.value = value;
		}

		protected Object doGetValue() {
			return value;
		}

		public Object getValueType() {
			return null;
		}
	}
}
