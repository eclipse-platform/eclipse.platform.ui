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
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
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

	ThreadRealm targetAndModelRealm = new ThreadRealm();
	ThreadRealm validationRealm = new ThreadRealm();

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
		new Thread() {
			@Override
			public void run() {
				targetAndModelRealm.init(Thread.currentThread());
				targetAndModelRealm.block();
			}
		}.start();

		validationRealm.init(Thread.currentThread());
		dbc = new DataBindingContext(validationRealm);
		Policy.setLog(logger);
	}

	@After
	public void tearDown() throws Exception {
		dbc.dispose();
	}

	@Test
	public void testListBindingValidationRealm() throws Throwable {
		final ObservableList model = new WritableList(targetAndModelRealm);
		final ObservableList target = new WritableList(targetAndModelRealm);

		dbc.bindList(target, model);
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testSetBindingValidationRealm() throws Throwable {
		final ObservableSet model = new WritableSet(targetAndModelRealm);
		final ObservableSet target = new WritableSet(targetAndModelRealm);

		dbc.bindSet(target, model);
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testBindingCanBeCreatedOutsideOfValidationRealm() throws Exception {
		final ObservableSet<String> model = new WritableSet<>(targetAndModelRealm);
		final ObservableSet<String> target = new WritableSet<>(targetAndModelRealm);

		targetAndModelRealm.unblock();

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
		// The validationRealm is the current realm.
		final IObservableValue<String> modelValue = new WritableValue<>(validationRealm);
		final IObservableList<String> model = new ComputedList<String>(validationRealm) {
			@Override
			protected List<String> calculate() {
				return Collections.singletonList(modelValue.getValue());
			}
		};
		final IObservableList<String> target = new WritableList<>(targetAndModelRealm);

		dbc.bindList(target, model);
		modelValue.setValue("Test");
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testBindComputedSetToWritableSetInDifferentRealm() {
		// The validationRealm is the current realm.
		final IObservableValue<String> modelValue = new WritableValue<>(validationRealm);
		final IObservableSet<String> model = new ComputedSet<String>(validationRealm) {
			@Override
			protected Set<String> calculate() {
				return Collections.singleton(modelValue.getValue());
			}
		};
		final IObservableSet<String> target = new WritableSet<>(targetAndModelRealm);

		dbc.bindSet(target, model);
		modelValue.setValue("Test");
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test
	public void testBindComputedValueToWritableValueInDifferentRealm() {
		// The validationRealm is the current realm.
		final IObservableValue<String> modelValue = new WritableValue<>(validationRealm);
		final IObservableValue<String> model = new ComputedValue<String>(validationRealm) {
			@Override
			protected String calculate() {
				return modelValue.getValue();
			}
		};
		final IObservableValue<String> target = new WritableValue<>(targetAndModelRealm);

		dbc.bindValue(target, model);
		modelValue.setValue("Test");
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}
}
