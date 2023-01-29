/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 *     Brad Reynolds - bug 116920, 159768
 *     Matthew Hall - bug 260329
 *******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.eclipse.core.databinding.UpdateListStrategy.POLICY_NEVER;
import static org.eclipse.core.databinding.UpdateListStrategy.POLICY_UPDATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ListBinding;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class ListBindingTest extends AbstractDefaultRealmTestCase {
	private IObservableList<String> target;
	private IObservableList<String> model;
	private DataBindingContext dbc;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		target = new WritableList<>(new ArrayList<>(), String.class);
		model = new WritableList<>(new ArrayList<>(), String.class);
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
		Binding binding = dbc.bindList(target, model,
				new UpdateListStrategy<>(UpdateListStrategy.POLICY_ON_REQUEST),
				new UpdateListStrategy<>(UpdateListStrategy.POLICY_ON_REQUEST));

		target.add("1");
		List<String> targetCopy = new ArrayList<>(target.size());
		targetCopy.addAll(target);

		model.add("2");

		assertFalse("target should not equal model", target.equals(model));
		binding.updateTargetToModel();
		assertEquals("target should not have changed", targetCopy, target);
		assertEquals("target != model", target, model);
	}

	@Test
	public void testUpdateTargetFromModel() throws Exception {
		Binding binding = dbc.bindList(target, model,
				new UpdateListStrategy<>(UpdateListStrategy.POLICY_ON_REQUEST),
				new UpdateListStrategy<>(UpdateListStrategy.POLICY_ON_REQUEST));

		target.add("1");
		model.add("2");

		List<String> modelCopy = new ArrayList<>(model.size());
		modelCopy.addAll(model);

		assertFalse("model should not equal target", model.equals(target));
		binding.updateModelToTarget();

		assertEquals("model should not have changed", modelCopy, model);
		assertEquals("model != target", model, target);
	}

	@Test
	public void testGetTarget() throws Exception {
		Binding binding = dbc.bindList(target, model);
		IObservable targetList = binding.getTarget();
		assertEquals(target, targetList);
	}

	@Test
	public void testGetModel() throws Exception {
		Binding binding = dbc.bindList(target, model);
		IObservable modelList = binding.getModel();
		assertEquals(model, modelList);
	}

	@Test
	public void testStatusIsInstanceOfBindingStatus() throws Exception {
		Binding binding = dbc.bindList(target, model);
		assertTrue(binding.getValidationStatus().getValue() instanceof BindingStatus);
	}

	@Test
	public void testAddValidationStatusContainsMultipleStatuses() throws Exception {
		UpdateListStrategy<String, String> strategy = new UpdateListStrategy<String, String>() {
			@Override
			protected IStatus doAdd(IObservableList<? super String> observableList, String element, int index) {
				super.doAdd(observableList, element, index);

				switch (index) {
				case 0:
					return ValidationStatus.error("");
				case 1:
					return ValidationStatus.info("");
				}

				return null;
			}
		};

		Binding binding = dbc.bindList(target, model, strategy, null);
		target.addAll(Arrays.asList(new String[] {"1", "2"}));

		IStatus status = binding.getValidationStatus().getValue();
		assertEquals("maximum status", IStatus.ERROR, status.getSeverity());
		assertTrue("multi status", status.isMultiStatus());

		IStatus[] children = status.getChildren();
		assertEquals("multi status children", 2, children.length);
		assertEquals("first status severity", IStatus.ERROR, children[0].getSeverity());
		assertEquals("second status severity", IStatus.INFO, children[1].getSeverity());
	}

	@Test
	public void testRemoveValidationStatusContainsMultipleStatuses() throws Exception {
		List<String> items = Arrays.asList(new String[] { "1", "2" });
		model.addAll(items);

		UpdateListStrategy<String, String> strategy = new UpdateListStrategy<String, String>() {
			int count;
			@Override
			protected IStatus doRemove(IObservableList<? super String> observableList, int index) {
				super.doRemove(observableList, index);

				switch (count++) {
				case 0:
					return ValidationStatus.error("");
				case 1:
					return ValidationStatus.info("");
				}

				return null;
			}
		};

		Binding binding = dbc.bindList(target, model, strategy, null);
		target.removeAll(items);

		IStatus status = binding.getValidationStatus().getValue();
		assertEquals("maximum status", IStatus.ERROR, status.getSeverity());
		assertTrue("multi status", status.isMultiStatus());

		IStatus[] children = status.getChildren();
		assertEquals("multi status children", 2, children.length);
		assertEquals("first status severity", IStatus.ERROR, children[0].getSeverity());
		assertEquals("second status severity", IStatus.INFO, children[1].getSeverity());
	}

	@Test
	public void testAddOKValidationStatus() throws Exception {
		Binding binding = dbc.bindList(target, model);
		target.add("1");

		IStatus status = binding.getValidationStatus().getValue();
		assertTrue(status.isOK());
		assertEquals(0, status.getChildren().length);
	}

	@Test
	public void testRemoveOKValidationStatus() throws Exception {
		model.add("1");
		Binding binding = dbc.bindList(target, model);

		target.remove("1");
		IStatus status = binding.getValidationStatus().getValue();
		assertTrue(status.isOK());
		assertEquals(0, status.getChildren().length);
	}

	/**
	 * We test common functionality from UpdateStrategy here, because that base
	 * class would need much more stubbing and mocking to test it.
	 */
	@Test
	public void testErrorDuringConversion() {
		UpdateListStrategy<String, String> modelToTarget = new UpdateListStrategy<>();
		modelToTarget.setConverter(IConverter.create(String.class, String.class, fromObject -> {
			throw new IllegalArgumentException();
		}));

		Binding binding = dbc.bindList(target, model, new UpdateListStrategy<>(), modelToTarget);
		CountDownLatch latch = new CountDownLatch(1);

		Policy.setLog(status -> {
			latch.countDown();
			assertEquals(IStatus.ERROR, status.getSeverity());
			assertTrue(status.getException() instanceof IllegalArgumentException);
		});

		model.add("first");

		assertTrue("Target not changed on conversion error", target.isEmpty());
		assertEquals(0, latch.getCount());
		assertEquals(IStatus.ERROR, binding.getValidationStatus().getValue().getCode());

		Policy.setLog(null);
	}

	/**
	 * We test common functionality from UpdateStrategy here, because that base
	 * class would need much more stubbing and mocking to test it.
	 */
	@Test
	public void testErrorDuringRemove() {
		IObservableList<String> target = new WritableList<String>() {
			@Override
			public String remove(int index) {
				throw new IllegalArgumentException();
			}
		};

		Binding binding = dbc.bindList(target, model, new UpdateListStrategy<>(), new UpdateListStrategy<>());
		CountDownLatch latch = new CountDownLatch(1);

		Policy.setLog(status -> {
			latch.countDown();
			assertEquals(IStatus.ERROR, status.getSeverity());
			assertTrue(status.getException() instanceof IllegalArgumentException);
		});

		model.add("first");
		model.remove("first");

		assertEquals("Target not changed on conversion error", Arrays.asList("first"), target);
		assertEquals(0, latch.getCount());
		assertEquals(IStatus.ERROR, binding.getValidationStatus().getValue().getSeverity());
	}

	/**
	 * We test common functionality from UpdateStrategy here, because that base
	 * class would need much more stubbing and mocking to test it.
	 */
	@Test
	public void testErrorDuringMove() {
		IObservableList<String> target = new WritableList<String>() {
			@Override
			public String move(int index, int index2) {
				throw new IllegalArgumentException();
			}
		};
		Binding binding = dbc.bindList(target, model, new UpdateListStrategy<>(), new UpdateListStrategy<>());
		CountDownLatch latch = new CountDownLatch(1);

		Policy.setLog(status -> {
			latch.countDown();
			assertEquals(IStatus.ERROR, status.getSeverity());
			assertTrue(status.getException() instanceof IllegalArgumentException);
		});

		model.add("first");
		model.add("second");
		model.move(0, 1);

		assertEquals("Target not changed on conversion error", Arrays.asList("first", "second"), target);
		assertEquals(0, latch.getCount());
		assertEquals(IStatus.ERROR, binding.getValidationStatus().getValue().getSeverity());
	}

	/**
	 * We test common functionality from UpdateStrategy here, because that base
	 * class would need much more stubbing and mocking to test it.
	 */
	@Test
	public void testErrorDuringReplace() {
		IObservableList<String> target = new WritableList<String>() {
			@Override
			public String set(int index, String element) {
				throw new IllegalArgumentException();
			}
		};

		Binding binding = dbc.bindList(target, model, new UpdateListStrategy<>(), new UpdateListStrategy<>());
		CountDownLatch latch = new CountDownLatch(1);

		Policy.setLog(status -> {
			latch.countDown();
			assertEquals(IStatus.ERROR, status.getSeverity());
			assertTrue(status.getException() instanceof IllegalArgumentException);
		});

		model.add("first");
		model.set(0, "second");

		assertEquals("Element not changed on conversion error", "first", target.get(0));
		assertEquals(0, latch.getCount());
		assertEquals(IStatus.ERROR, binding.getValidationStatus().getValue().getSeverity());
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
		new ListBinding<>(target, model, new UpdateListStrategy<>(), new UpdateListStrategy<>());
		model.remove(0);
	}

	/**
	 * Test for bug 491678.
	 */
	@Test
	public void testTargetValueIsSyncedToModelIfModelWasNotSyncedToTarget() {
		target.add("first");
		dbc.bindList(target, model, new UpdateListStrategy<>(POLICY_UPDATE), new UpdateListStrategy<>(POLICY_NEVER));
		assertEquals(model.size(), target.size());
	}

	/**
	 * Test for bug 326507.
	 */
	@Test
	public void testConversion() {
		dbc.bindList(target, model, new UpdateListStrategy<>(), UpdateListStrategy
				.create(IConverter.create(String.class, String.class, fromObject -> fromObject + "converted")));

		model.add("1");
		assertEquals("1converted", target.get(0));

		model.set(0, "2");
		assertEquals("2converted", target.get(0));
	}
}
