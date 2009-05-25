/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 118516, 213145, 194734, 195222
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.conformance.ObservableDelegateTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.2
 */
public class TableSingleSelectionObservableValueTest extends
		ObservableDelegateTest {
	private Delegate delegate;
	private IObservableValue observable;
	private Table table;

	public TableSingleSelectionObservableValueTest() {
		this(null);
	}

	public TableSingleSelectionObservableValueTest(String testName) {
		super(testName, new Delegate());
	}

	protected void setUp() throws Exception {
		super.setUp();

		observable = (IObservableValue) getObservable();
		delegate = (Delegate) getObservableContractDelegate();
		table = delegate.table;
	}

	protected IObservable doCreateObservable() {
		Delegate delegate = (Delegate) getObservableContractDelegate();
		return delegate.createObservableValue(SWTObservables.getRealm(Display
				.getDefault()));
	}

	public void testSetValue() throws Exception {
		// preconditions
		assertEquals(-1, table.getSelectionIndex());
		assertEquals(-1, ((Integer) observable.getValue()).intValue());

		Integer value = new Integer(0);
		observable.setValue(value);
		assertEquals("table selection index", value.intValue(), table
				.getSelectionIndex());
		assertEquals("observable value", value, observable.getValue());
	}

	public void testGetValue() throws Exception {
		int value = 1;
		table.setSelection(value);

		assertEquals("table selection index", value, table.getSelectionIndex());
		assertEquals("observable value", new Integer(value), observable
				.getValue());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(
				TableSingleSelectionObservableValueTest.class.toString());
		suite.addTestSuite(TableSingleSelectionObservableValueTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		Table table;

		public void setUp() {
			shell = new Shell();
			table = new Table(shell, SWT.NONE);
			new TableItem(table, SWT.NONE).setText("0");
			new TableItem(table, SWT.NONE).setText("1");
		}

		public void tearDown() {
			shell.dispose();
		}

		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.singleSelectionIndex()
					.observe(realm, table);
		}

		public Object getValueType(IObservableValue observable) {
			return Integer.TYPE;
		}

		public void change(IObservable observable) {
			int index = createIntegerValue((IObservableValue) observable)
					.intValue();
			table.select(index);

			table.notifyListeners(SWT.Selection, null);
		}

		public Object createValue(IObservableValue observable) {
			return createIntegerValue(observable);
		}

		private Integer createIntegerValue(IObservableValue observable) {
			int value = ((Integer) observable.getValue()).intValue();
			switch (value) {
			case -1:
			case 1:
				return new Integer(0);
			case 0:
				return new Integer(1);
			}

			Assert.isTrue(false);
			return null;
		}
	}
}
