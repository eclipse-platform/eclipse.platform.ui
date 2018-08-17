/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 254524)
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

public class BindingTest extends AbstractDefaultRealmTestCase {
	private IObservableValue<String> target;
	private IObservableValue<String> model;

	private DataBindingContext dbc;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		target = WritableValue.withValueType(String.class);
		model = WritableValue.withValueType(String.class);
		dbc = new DataBindingContext();
	}

	@Test
	public void testDisposeTargetDisposesBinding() {
		Binding binding = createBinding();
		assertFalse(binding.isDisposed());
		target.dispose();
		assertTrue(binding.isDisposed());
	}

	@Test
	public void testDisposeModelDisposesBinding() {
		Binding binding = createBinding();
		assertFalse(binding.isDisposed());
		model.dispose();
		assertTrue(binding.isDisposed());
	}

	@Test
	public void testPreDisposedTarget_FiresIllegalArgumentException() {
		try {
			target.dispose();
			createBinding();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testPreDisposedModel_FiresIllegalArgumentException() {
		try {
			model.dispose();
			createBinding();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testDisposeModelThenBinding() {
		Binding binding = createBinding();
		model.dispose();
		binding.dispose();
	}

	@Test
	public void testDisposeTargetThenBinding() {
		Binding binding = createBinding();
		target.dispose();
		binding.dispose();
	}

	@Test
	public void testDisposeObservablesThenBinding() {
		Binding binding = createBinding();
		model.dispose();
		target.dispose();
		binding.dispose();
	}

	@Test
	public void testDisposeBindingThenModel() {
		Binding binding = createBinding();
		binding.dispose();
		model.dispose();
	}

	@Test
	public void testDisposeBindingThenTarget() {
		Binding binding = createBinding();
		binding.dispose();
		target.dispose();
	}

	@Test
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
		BindingStub(IObservableValue<String> target, IObservableValue<String> model) {
			super(target, model);
		}

		@Override
		public IObservableValue<IStatus> getValidationStatus() {
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
