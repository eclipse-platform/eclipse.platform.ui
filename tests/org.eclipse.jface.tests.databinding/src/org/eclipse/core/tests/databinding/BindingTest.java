/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 254524)
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class BindingTest extends AbstractDefaultRealmTestCase {
	private IObservable target;

	private IObservable model;

	private DataBindingContext dbc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		target = WritableValue.withValueType(String.class);
		model = WritableValue.withValueType(String.class);
		dbc = new DataBindingContext();
	}

	public void testDisposeTargetDisposesBinding() {
		Binding binding = createBinding();
		assertFalse(binding.isDisposed());
		target.dispose();
		assertTrue(binding.isDisposed());
	}

	public void testDisposeModelDisposesBinding() {
		Binding binding = createBinding();
		assertFalse(binding.isDisposed());
		model.dispose();
		assertTrue(binding.isDisposed());
	}

	public void testPreDisposedTarget_FiresIllegalArgumentException() {
		try {
			target.dispose();
			createBinding();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testPreDisposedModel_FiresIllegalArgumentException() {
		try {
			model.dispose();
			createBinding();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testDisposeModelThenBinding() {
		Binding binding = createBinding();
		model.dispose();
		binding.dispose();
	}

	public void testDisposeTargetThenBinding() {
		Binding binding = createBinding();
		target.dispose();
		binding.dispose();
	}

	public void testDisposeObservablesThenBinding() {
		Binding binding = createBinding();
		model.dispose();
		target.dispose();
		binding.dispose();
	}

	public void testDisposeBindingThenModel() {
		Binding binding = createBinding();
		binding.dispose();
		model.dispose();
	}

	public void testDisposeBindingThenTarget() {
		Binding binding = createBinding();
		binding.dispose();
		target.dispose();
	}

	public void testDisposeBindingThenObservables() {
		Binding binding = createBinding();
		binding.dispose();
		model.dispose();
		target.dispose();
	}

	private Binding createBinding() {
		Binding binding = new BindingStub(target, model);
		binding.init(dbc);
		return binding;
	}

	private static class BindingStub extends Binding {
		BindingStub(IObservable target, IObservable model) {
			super(target, model);
		}

		@Override
		public IObservableValue getValidationStatus() {
			return null;
		}

		@Override
		protected void postInit() {
		}

		@Override
		protected void preInit() {
		}

		@Override
		public void updateModelToTarget() {
		}

		@Override
		public void updateTargetToModel() {
		}

		@Override
		public void validateModelToTarget() {
		}

		@Override
		public void validateTargetToModel() {
		}
	}
}
