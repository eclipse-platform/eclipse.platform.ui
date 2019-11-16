/*******************************************************************************
 * Copyright (c) 2015, 2018 Conrad Groth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Conrad Groth - Testing my fix, that validation status is set in the correct realm
 ******************************************************************************/
package org.eclipse.core.tests.internal.databinding;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.ComputedSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.tests.databinding.observable.ThreadRealm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DifferentRealmsBindingTest {

	ThreadRealm validationRealm = new ThreadRealm();
	ThreadRealm targetRealm = new ThreadRealm();
	ThreadRealm modelRealm = new ThreadRealm();
	ThreadRealm mainThread = new ThreadRealm();

	List<IStatus> errorStatusses = new ArrayList<>();

	DataBindingContext dbc;
	ILogger logger = status -> {
		if (!status.isOK()) {
			errorStatusses.add(status);
		}
	};

	@Before
	public void setUp() throws Exception {
		errorStatusses.clear();
		initRealm(targetRealm, true);
		initRealm(modelRealm, true);
		initRealm(validationRealm, false);
		mainThread.init(Thread.currentThread());

		dbc = new DataBindingContext(validationRealm);
		Policy.setLog(logger);
	}

	private void initRealm(final ThreadRealm realm, final boolean block) {
		new Thread() {
			@Override
			public void run() {
				realm.init(Thread.currentThread());
				if (block) {
					realm.block();
				}
			}
		}.start();
		if (block) {
			realm.waitUntilBlocking();
		}
	}

	@After
	public void tearDown() throws Exception {
		validationRealm.exec(() -> dbc.dispose());
	}

	@Test
	public void testListBindingValidationRealm() throws Throwable {
		final IObservableList<?> model = new WritableList<>(targetRealm);
		final IObservableList<?> target = new WritableList<>(targetRealm);

		dbc.bindList(target, model);
		targetRealm.waitUntilBlocking();
		targetRealm.processQueue();
		targetRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testSetBindingValidationRealm() throws Throwable {
		final IObservableSet<?> model = new WritableSet<>(targetRealm);
		final IObservableSet<?> target = new WritableSet<>(targetRealm);

		dbc.bindSet(target, model);
		targetRealm.waitUntilBlocking();
		targetRealm.processQueue();
		targetRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testBindingCanBeCreatedOutsideOfValidationRealm() throws Exception {
		final ObservableSet<String> model = new WritableSet<>(targetRealm);
		final ObservableSet<String> target = new WritableSet<>(targetRealm);

		targetRealm.unblock();

		AtomicReference<Exception> exceptionOccured = new AtomicReference<>();
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					dbc.bindSet(target, model);
				} catch (Exception e) {
					exceptionOccured.set(e);
				}
			}
		};
		t.start();
		t.join(1000);

		assertNull(exceptionOccured.get());
	}

	@Test
	public void testBindComputedListToWritableListInDifferentRealm() {
		final IObservableValue<String> modelValue = new WritableValue<>(mainThread);
		final IObservableList<String> model = new ComputedList<String>(mainThread) {
			@Override
			protected List<String> calculate() {
				return Collections.singletonList(modelValue.getValue());
			}
		};
		final IObservableList<String> target = new WritableList<>(targetRealm);

		dbc.bindList(target, model);
		modelValue.setValue("Test");
		targetRealm.waitUntilBlocking();
		targetRealm.processQueue();
		targetRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testBindComputedSetToWritableSetInDifferentRealm() {
		final IObservableValue<String> modelValue = new WritableValue<>(mainThread);
		final IObservableSet<String> model = new ComputedSet<String>(mainThread) {
			@Override
			protected Set<String> calculate() {
				return Collections.singleton(modelValue.getValue());
			}
		};
		final IObservableSet<String> target = new WritableSet<>(targetRealm);

		dbc.bindSet(target, model);
		modelValue.setValue("Test");
		targetRealm.waitUntilBlocking();
		targetRealm.processQueue();
		targetRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testBindComputedValueToWritableValueInDifferentRealm() {
		final IObservableValue<String> modelValue = new WritableValue<>(mainThread);
		final IObservableValue<String> model = new ComputedValue<String>(mainThread) {
			@Override
			protected String calculate() {
				return modelValue.getValue();
			}
		};
		final IObservableValue<String> target = new WritableValue<>(targetRealm);

		dbc.bindValue(target, model);
		modelValue.setValue("Test");
		targetRealm.waitUntilBlocking();
		targetRealm.processQueue();
		targetRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testSetBindingUpdatesDontInterferWithObservableDisposing() throws Exception {
		final IObservableSet<String> model = new WritableSet<>(modelRealm);
		final IObservableSet<String> target = new WritableSet<>(targetRealm);
		dbc.bindSet(target, model);

		waitForBindingInitiated();

		modelRealm.exec(() -> model.add("one"));

		testDisposeRaceCondition(target);
	}

	@Test
	public void testListBindingUpdatesDontInterferWithObservableDisposing() throws Exception {
		final IObservableList<String> model = new WritableList<>(modelRealm);
		final IObservableList<String> target = new WritableList<>(targetRealm);
		dbc.bindList(target, model);

		waitForBindingInitiated();

		modelRealm.exec(() -> model.add("one"));

		testDisposeRaceCondition(target);
	}

	@Test
	public void testValueBindingUpdatesDontInterferWithObservableDisposing() throws Exception {
		final IObservableValue<String> model = new WritableValue<>(modelRealm);
		final IObservableValue<String> target = new WritableValue<>(targetRealm);
		dbc.bindValue(target, model);

		waitForBindingInitiated();

		modelRealm.exec(() -> model.setValue("one"));

		testDisposeRaceCondition(target);
	}

	private void testDisposeRaceCondition(final IObservable target) {
		modelRealm.processQueue();
		targetRealm.waitUntilBlocking();

		target.dispose();

		modelRealm.unblock();
		targetRealm.processQueue();
		targetRealm.unblock();
		validationRealm.unblock();

		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	private void waitForBindingInitiated() {
		modelRealm.waitUntilBlocking();
		modelRealm.processQueue();

		targetRealm.waitUntilBlocking();
		targetRealm.processQueue();
	}
}
