/*******************************************************************************
 * Copyright (c) 2007, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 212223)
 *     Matthew Hall - bug 213145, 245647, 194734
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.swt.SWTObservableValueDecorator;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * Tests for DelayedObservableValue
 *
 * @since 1.2
 */
public class SWTDelayedObservableValueDecoratorTest extends
		AbstractDefaultRealmTestCase {
	private Display display;
	private Shell shell;
	private Object oldValue;
	private Object newValue;
	private ISWTObservableValue target;
	private ISWTObservableValue delayed;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		display = Display.getCurrent();
		shell = new Shell(display);
		target = new SWTObservableValueDecorator(new WritableValue(
				DisplayRealm.getRealm(display)), shell);
		oldValue = new Object();
		newValue = new Object();
		target.setValue(oldValue);
		delayed = SWTObservables.observeDelayedValue(1, target);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		delayed.dispose();
		target.dispose();
		target = null;
		shell.dispose();
		shell = null;
		display = null;
		super.tearDown();
	}

	@Test
	public void testFocusOut_FiresPendingValueChange() {
		assertFiresPendingValueChange(() -> shell.notifyListeners(SWT.FocusOut, new Event()));
	}

	private void assertFiresPendingValueChange(Runnable runnable) {
		ValueChangeEventTracker tracker = ValueChangeEventTracker
				.observe(delayed);

		target.setValue(newValue);
		assertTrue(delayed.isStale());
		assertEquals(0, tracker.count);

		runnable.run();

		assertFalse(delayed.isStale());
		assertEquals(1, tracker.count);
		assertEquals(oldValue, tracker.event.diff.getOldValue());
		assertEquals(newValue, tracker.event.diff.getNewValue());
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(SWTMutableObservableValueContractTest.suite(new Delegate()));
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		Shell shell;

		@Override
		public void setUp() {
			super.setUp();
			shell = new Shell();
		}

		@Override
		public void tearDown() {
			shell.dispose();
			shell = null;
			super.tearDown();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return SWTObservables.observeDelayedValue(0,
					new SWTObservableValueDecorator(new WritableValue(realm,
							null, Object.class), shell));
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Object.class;
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return new Object();
		}
	}
}
