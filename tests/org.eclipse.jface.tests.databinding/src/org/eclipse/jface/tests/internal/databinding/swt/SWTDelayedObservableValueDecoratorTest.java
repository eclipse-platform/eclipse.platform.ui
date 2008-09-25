/*******************************************************************************
 * Copyright (c) 2007-2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 212223)
 *     Matthew Hall - bug 213145, 245647
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

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
	private SWTObservableValueStub target;
	private ISWTObservableValue delayed;

	protected void setUp() throws Exception {
		super.setUp();
		display = Display.getCurrent();
		shell = new Shell(display);
		target = new SWTObservableValueStub(SWTObservables.getRealm(display),
				shell);
		oldValue = new Object();
		newValue = new Object();
		target.setValue(oldValue);
		delayed = SWTObservables.observeDelayedValue(1, target);
	}

	protected void tearDown() throws Exception {
		delayed.dispose();
		target.dispose();
		target = null;
		shell.dispose();
		shell = null;
		display = null;
		super.tearDown();
	}

	public void testFocusOut_FiresPendingValueChange() {
		assertFiresPendingValueChange(new Runnable() {
			public void run() {
				// simulate focus-out event
				shell.notifyListeners(SWT.FocusOut, new Event());
			}
		});
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

	static class SWTObservableValueStub extends AbstractSWTObservableValue {
		private Object value;
		private boolean stale;

		Object overrideValue;

		public SWTObservableValueStub(Realm realm, Widget widget) {
			super(realm, widget);
		}

		protected Object doGetValue() {
			return value;
		}

		protected void doSetValue(Object value) {
			Object oldValue = this.value;
			if (overrideValue != null)
				value = overrideValue;
			this.value = value;
			stale = false;
			fireValueChange(Diffs.createValueDiff(oldValue, value));
		}

		public Object getValueType() {
			return Object.class;
		}

		protected void fireStale() {
			stale = true;
			super.fireStale();
		}

		public boolean isStale() {
			return stale;
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(
				SWTDelayedObservableValueDecoratorTest.class.getName());
		suite.addTestSuite(SWTDelayedObservableValueDecoratorTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		Shell shell;

		public void setUp() {
			super.setUp();
			shell = new Shell();
		}

		public void tearDown() {
			shell.dispose();
			shell = null;
			super.tearDown();
		}

		public IObservableValue createObservableValue(Realm realm) {
			return SWTObservables.observeDelayedValue(0,
					new SWTObservableValueStub(realm, shell));
		}

		public Object getValueType(IObservableValue observable) {
			return Object.class;
		}

		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		public Object createValue(IObservableValue observable) {
			return new Object();
		}
	}
}
