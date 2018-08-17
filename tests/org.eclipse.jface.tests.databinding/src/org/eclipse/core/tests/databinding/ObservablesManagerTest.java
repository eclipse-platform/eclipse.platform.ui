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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
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

	@Before
	public void setUp() throws Exception {
		super.setUp();

		dbc = new DataBindingContext();
	}

	@After
	public void tearDown() throws Exception {
		if (dbc != null) {
			dbc.dispose();
		}
		super.tearDown();
	}

	@Test
	public void testOnlyModelIsDisposed() throws Exception {
		IObservableValue targetOv = new WritableValue();
		IObservableValue modelOv = new WritableValue();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, false, true);
		observablesManager.dispose();

		assertFalse(targetOv.isDisposed());
		assertTrue(modelOv.isDisposed());
	}

	@Test
	public void testOnlyTargetIsDisposed() throws Exception {
		IObservableValue targetOv = new WritableValue();
		IObservableValue modelOv = new WritableValue();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, true, false);
		observablesManager.dispose();

		assertTrue(targetOv.isDisposed());
		assertFalse(modelOv.isDisposed());
	}

	@Test
	public void testTargetAndModelIsDisposed() throws Exception {
		IObservableValue targetOv = new WritableValue();
		IObservableValue modelOv = new WritableValue();
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
		IObservable observable = new WritableList();
		manager.addObservable(observable);
		observable.dispose();

		manager.dispose();
	}
}
