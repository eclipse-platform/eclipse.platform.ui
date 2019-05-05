/*******************************************************************************
 * Copyright (c) 2007, 2009 Bob Smith and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bob Smith - initial API and implementation (bug 198880)
 *     Matthew Hall - bugs 146397, 260329
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class ObservablesManagerTest extends AbstractDefaultRealmTestCase {
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
	public void testOnlyModelIsDisposed() throws Exception {
		IObservableValue<?> targetOv = new WritableValue<>();
		IObservableValue<?> modelOv = new WritableValue<>();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, false, true);
		observablesManager.dispose();

		assertFalse(targetOv.isDisposed());
		assertTrue(modelOv.isDisposed());
	}

	@Test
	public void testOnlyTargetIsDisposed() throws Exception {
		IObservableValue<?> targetOv = new WritableValue<>();
		IObservableValue<?> modelOv = new WritableValue<>();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, true, false);
		observablesManager.dispose();

		assertTrue(targetOv.isDisposed());
		assertFalse(modelOv.isDisposed());
	}

	@Test
	public void testTargetAndModelIsDisposed() throws Exception {
		IObservableValue<?> targetOv = new WritableValue<>();
		IObservableValue<?> modelOv = new WritableValue<>();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, true, true);
		observablesManager.dispose();

		assertTrue(targetOv.isDisposed());
		assertTrue(modelOv.isDisposed());
	}

	@Test
	public void testDispose_Bug277966_NPEWhenManagedObservableAlreadyDisposed() {
		ObservablesManager manager = new ObservablesManager();

		// NPE only occurs when explicitly managing (i.e. not through a
		// DataBindingContext) observables where hashCode() is a tracked getter
		IObservable observable = new WritableList<>();
		manager.addObservable(observable);
		observable.dispose();

		manager.dispose();
	}

	@Test
	public void testDisposeMultipleTargets_Bug546983() {
		DataBindingContext context = new DataBindingContext();

		IObservableList<IObservable> targets = new WritableList<>(
				Arrays.asList(new WritableValue<>(), new WritableValue<>()), WritableValue.class);
		IObservableList<IObservable> models = new WritableList<>(
				Arrays.asList(new WritableValue<>(), new WritableValue<>()), WritableValue.class);

		context.addBinding(createDummyBinding(targets, models));
		ObservablesManager manager = new ObservablesManager();
		manager.addObservablesFromContext(context, true, true);
		manager.dispose();

		assertTrue(targets.get(0).isDisposed());
		assertTrue(targets.get(1).isDisposed());
		assertTrue(models.get(0).isDisposed());
		assertTrue(models.get(1).isDisposed());
	}

	private Binding createDummyBinding(IObservableList<IObservable> targets, IObservableList<IObservable> models) {
		return new Binding(new WritableValue<>(), new WritableValue<>()) {
			@Override
			public IObservableList<IObservable> getTargets() {
				return targets;
			}
			@Override
			public IObservableList<IObservable> getModels() {
				return models;
			}

			@Override
			public IObservableValue<IStatus> getValidationStatus() {
				return Observables.constantObservableValue(Status.OK_STATUS);
			}
			@Override
			public void validateTargetToModel() {
			}
			@Override
			public void validateModelToTarget() {
			}
			@Override
			public void updateTargetToModel() {
			}
			@Override
			public void updateModelToTarget() {
			}
			@Override
			protected void preInit() {
			}
			@Override
			protected void postInit() {
			}
		};
	}
}
