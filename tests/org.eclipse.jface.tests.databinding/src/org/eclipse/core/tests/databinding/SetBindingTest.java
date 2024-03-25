/*******************************************************************************
 * Copyright (c) 2016, 2018 Conrad Groth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Conrad Groth - initial implementation for bug 491678
 *******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.eclipse.core.databinding.UpdateSetStrategy.POLICY_NEVER;
import static org.eclipse.core.databinding.UpdateSetStrategy.POLICY_UPDATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.SetBinding;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SetBindingTest extends AbstractDefaultRealmTestCase {
	private IObservableSet<String> target;
	private IObservableSet<String> model;
	private DataBindingContext dbc;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		target = new WritableSet<>();
		model = new WritableSet<>();
		dbc = new DataBindingContext();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		dbc.dispose();
		model.dispose();
		target.dispose();
	}

	@Test
	public void testUpdateModelFromTarget() throws Exception {
		target.add("1");

		dbc.bindSet(target, model, new UpdateSetStrategy<>(UpdateSetStrategy.POLICY_NEVER), new UpdateSetStrategy<>());

		assertEquals("target != model", target, model);
	}

	@Test
	public void testUpdateTargetFromModel() throws Exception {
		model.add("1");

		dbc.bindSet(target, model, new UpdateSetStrategy<>(), new UpdateSetStrategy<>(UpdateSetStrategy.POLICY_NEVER));

		assertEquals("model != target" + model + target, model, target);
	}

	/**
	 * Test for bug 491678.
	 */
	@Test
	public void testAddListenerAndInitialSyncAreUninterruptable() {
		Policy.setLog(status -> {
			assertTrue("The databinding logger has the not-ok status " + status, status.isOK());
		});

		model.add("first");
		new SetBinding<>(target, model, new UpdateSetStrategy<>(), new UpdateSetStrategy<>());
		model.remove("first");
	}

	/**
	 * Test for bug 491678.
	 */
	@Test
	public void testTargetValueIsSyncedToModelIfModelWasNotSyncedToTarget() {
		target.add("first");
		dbc.bindSet(target, model, new UpdateSetStrategy<>(POLICY_UPDATE), new UpdateSetStrategy<>(POLICY_NEVER));
		assertEquals(model.size(), target.size());
	}

	@Test
	public void testErrorDuringConversion() {
		UpdateSetStrategy<String, String> modelToTarget = UpdateSetStrategy
				.create(IConverter.create(String.class, String.class, fromObject -> {
			throw new IllegalArgumentException();
		}));

		Binding binding = dbc.bindSet(target, model, new UpdateSetStrategy<>(), modelToTarget);
		CountDownLatch latch = new CountDownLatch(1);

		Policy.setLog(status -> {
			latch.countDown();
			assertEquals(IStatus.ERROR, status.getSeverity());
			assertTrue(status.getException() instanceof IllegalArgumentException);
		});

		model.add("first");

		assertTrue("Target not changed on conversion error", target.isEmpty());
		assertEquals(0, latch.getCount());
		assertEquals(IStatus.ERROR, binding.getValidationStatus().getValue().getSeverity());

		Policy.setLog(null);
	}

	/**
	 * We test common functionality from UpdateStrategy here, because that base
	 * class would need much more stubbing and mocking to test it.
	 */
	@Test
	public void testErrorDuringRemoveIsLogged() {
		IObservableSet<String> target = new WritableSet<String>() {
			@Override
			public boolean remove(Object elem) {
				throw new IllegalArgumentException();
			}
		};

		Binding binding = dbc.bindSet(target, model, new UpdateSetStrategy<>(), new UpdateSetStrategy<>());
		CountDownLatch latch = new CountDownLatch(1);

		Policy.setLog(status -> {
			latch.countDown();
			assertEquals(IStatus.ERROR, status.getSeverity());
			assertTrue(status.getException() instanceof IllegalArgumentException);
		});

		model.add("first");
		model.remove("first");

		assertEquals(0, latch.getCount());
		assertEquals(IStatus.ERROR, binding.getValidationStatus().getValue().getSeverity());
	}

}
