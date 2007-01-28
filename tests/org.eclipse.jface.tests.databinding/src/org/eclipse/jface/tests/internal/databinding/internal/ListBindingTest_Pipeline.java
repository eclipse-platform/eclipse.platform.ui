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

import junit.framework.TestCase;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.IBindingListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.ListBinding;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackLastListener;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackPositionListener;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackedValidator;
import org.eclipse.swt.widgets.Display;

/**
 * Asserts pipeline behavior of ListBinding.
 * 
 * @since 3.2
 */
public class ListBindingTest_Pipeline extends TestCase {
	private DataBindingContext dbc;
	private IObservableList target;
	private IObservableList model;

	protected void setUp() throws Exception {
		super.setUp();

		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
		dbc = new DataBindingContext();
		target = new WritableList(String.class);
		model = new WritableList(String.class);
	}

	protected void tearDown() throws Exception {
		Realm.setDefault(null);

		super.tearDown();
	}

	public void testTargetToModelPipelinePhaseOrder() throws Exception {
		int[] positions = new int[] { BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.PIPELINE_AFTER_CHANGE };

		int[] copyTypes = new int[positions.length];
		Arrays.fill(copyTypes, BindingEvent.EVENT_COPY_TO_MODEL);

		TrackPositionListener listener = new TrackPositionListener(
				positions.length);

		ListBinding binding = new ListBinding(target, model,
				new BindSpec());
		binding.init(dbc);
		binding.addBindingEventListener(listener);

		target.add("1");
		assertTrue("positions", Arrays.equals(positions, listener.positions));
		assertTrue("copy types", Arrays.equals(copyTypes, listener.copyTypes));
	}

	public void testModelToTargetPipelinePhaseOrder() throws Exception {
		int[] positions = new int[] { BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.PIPELINE_AFTER_CHANGE };
		int[] types = new int[positions.length];
		Arrays.fill(types, BindingEvent.EVENT_COPY_TO_TARGET);

		TrackPositionListener listener = new TrackPositionListener(
				positions.length);
		ListBinding binding = new ListBinding(target, model,
				new BindSpec());
		binding.init(dbc);

		binding.addBindingEventListener(listener);

		model.add("1");
		assertTrue("positions", Arrays.equals(positions, listener.positions));
		assertTrue("copy types", Arrays.equals(types, listener.copyTypes));
	}

	public void testTargetToModelValidationAfterGet() throws Exception {
		assertValidation(BindingEvent.PIPELINE_AFTER_GET, -1,
				BindingEvent.EVENT_COPY_TO_MODEL);
	}

	public void testTargetToModelValidationBeforeChange() throws Exception {
		assertValidation(BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.EVENT_COPY_TO_MODEL);
	}

	public void testModelToTargetValidationAfterGet() throws Exception {
		assertValidation(BindingEvent.PIPELINE_AFTER_GET, -1,
				BindingEvent.EVENT_COPY_TO_TARGET);
	}

	public void testModelToTargetValidationBeforeChange() throws Exception {
		assertValidation(BindingEvent.PIPELINE_BEFORE_CHANGE,
				BindingEvent.PIPELINE_AFTER_GET,
				BindingEvent.EVENT_COPY_TO_TARGET);
	}

	private void assertValidation(int position, int previousPosition,
			int copyType) throws Exception {
		TrackLastListener listener = new TrackLastListener();
		listener.active = false;
		TrackedValidator validator = new TrackedValidator(listener);

		IObservableList value = null;
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

		ListBinding binding = new ListBinding(target, model, bindSpec);
		binding.init(dbc);

		binding.addBindingEventListener(listener);
		listener.active = true;
		value.add("value");

		assertEquals("validator", validator, listener.lastValidator);
		assertEquals("validator invocation count", 1, validator.count);
		assertEquals("last binding event position", previousPosition,
				listener.lastPosition);
	}

	public void testUpdateModelFromTargetAfterGet() throws Exception {
		assertLastPhase(BindingEvent.PIPELINE_AFTER_GET, true);
	}

	public void testUpdateModelFromTargetBeforeChange() throws Exception {
		assertLastPhase(BindingEvent.PIPELINE_BEFORE_CHANGE, true);
	}

	public void testUpdateModelFromTargetAfterChange() throws Exception {
		assertLastPhase(BindingEvent.PIPELINE_AFTER_CHANGE, true);
	}

	public void testUpdateTargetFromModelAfterGet() throws Exception {
		assertLastPhase(BindingEvent.PIPELINE_AFTER_GET, false);
	}

	public void testUpdateTargetFromModelBeforeChange() throws Exception {
		assertLastPhase(BindingEvent.PIPELINE_BEFORE_CHANGE, false);
	}

	public void testUpdateTargetFromModelAfterChange() throws Exception {
		assertLastPhase(BindingEvent.PIPELINE_AFTER_CHANGE, false);
	}

	private void assertLastPhase(int phase, boolean performTarget) {
		TrackLastListener listener = new TrackLastListener();
		ListBinding binding = new ListBinding(target, model,
				new BindSpec());
		binding.init(dbc);

		binding.addBindingEventListener(listener);

		if (performTarget) {
			binding.updateModelFromTarget(phase);
		} else {
			binding.updateTargetFromModel(phase);
		}

		if (performTarget) {
			assertEquals(BindingEvent.EVENT_COPY_TO_MODEL,
					listener.lastCopyType);
		} else {
			assertEquals(BindingEvent.EVENT_COPY_TO_TARGET,
					listener.lastCopyType);
		}
		assertEquals(phase, listener.lastPosition);
	}

	public void testValidationErrorStatusValidatorFailure() throws Exception {
		class Validator implements IValidator {
			public IStatus validate(Object value) {
				return Status.CANCEL_STATUS;
			}
		}

		Validator validator = new Validator();
		ListBinding binding = new ListBinding(target, model,
				new BindSpec().addTargetValidator(
						BindingEvent.PIPELINE_AFTER_GET, validator));
		binding.init(dbc);

		assertTrue(((IStatus) binding.getValidationStatus().getValue()).isOK());
		target.add("value");
		assertFalse("status should be in error", ((IStatus) binding
				.getValidationStatus().getValue()).isOK());
	}

	public void testValidationErrorStatusListenerFailure() throws Exception {
		class Listener implements IBindingListener {
			public IStatus handleBindingEvent(BindingEvent e) {
				return Status.CANCEL_STATUS;
			}
		}

		ListBinding binding = new ListBinding(target, model,
				new BindSpec());
		binding.init(dbc);

		binding.addBindingEventListener(new Listener());

		assertTrue(((IStatus) binding.getValidationStatus().getValue()).isOK());
		target.add("value");
		assertFalse("status should be in error", ((IStatus) binding
				.getValidationStatus().getValue()).isOK());
	}
}
