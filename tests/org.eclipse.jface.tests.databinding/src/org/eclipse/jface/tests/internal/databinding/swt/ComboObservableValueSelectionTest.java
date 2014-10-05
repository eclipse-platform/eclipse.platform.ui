/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 213145, 194734, 195222
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public class ComboObservableValueSelectionTest extends TestCase {
	private Delegate delegate;

	private Combo combo;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		delegate = new Delegate();
		delegate.setUp();
		combo = delegate.combo;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		delegate.tearDown();
	}

	public void testSelection_NotifiesObservable() throws Exception {
		IObservableValue observable = (IObservableValue) delegate
				.createObservable(SWTObservables.getRealm(Display.getDefault()));

		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);
		combo.select(0);
		combo.notifyListeners(SWT.Selection, null);

		assertEquals("Observable was not notified.", 1, listener.count);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ComboObservableValueSelectionTest.class
				.toString());
		suite.addTestSuite(ComboObservableValueSelectionTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		/* package */Combo combo;

		@Override
		public void setUp() {
			shell = new Shell();
			combo = new Combo(shell, SWT.NONE);
			combo.add("a");
			combo.add("b");
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.selection().observe(realm, combo);
		}

		@Override
		public void change(IObservable observable) {
			int index = combo
					.indexOf((String) createValue((IObservableValue) observable));

			((IObservableValue) observable).setValue(combo.getItem(index));
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return String.class;
		}

		@Override
		public Object createValue(IObservableValue observable) {
			Combo combo = ((Combo) ((ISWTObservable) observable).getWidget());
			switch (combo.getSelectionIndex()) {
			case -1:
				// fall thru
			case 1:
				return combo.getItem(0);
			case 0:
				return combo.getItem(1);
			default:
				throw new RuntimeException("Unexpected selection.");
			}
		}
	}
}
