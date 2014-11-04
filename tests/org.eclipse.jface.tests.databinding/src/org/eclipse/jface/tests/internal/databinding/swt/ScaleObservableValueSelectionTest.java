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
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableDelegateTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class ScaleObservableValueSelectionTest extends ObservableDelegateTest {
	private Delegate delegate;

	private Scale scale;

	private IObservableValue observable;

	public ScaleObservableValueSelectionTest() {
		this(null);
	}

	public ScaleObservableValueSelectionTest(String testName) {
		super(testName, new Delegate());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		delegate = (Delegate) getObservableContractDelegate();
		observable = (IObservableValue) getObservable();
		scale = delegate.scale;
	}

	@Override
	protected IObservable doCreateObservable() {
		return getObservableContractDelegate().createObservable(
				DisplayRealm.getRealm(Display.getDefault()));
	}

	public void testGetValue() throws Exception {
		int value = 100;
		scale.setSelection(value);
		assertEquals(new Integer(value), observable.getValue());
	}

	public void testSetValue() throws Exception {
		int value = 100;
		observable.setValue(new Integer(value));
		assertEquals(value, scale.getSelection());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ScaleObservableValueSelectionTest.class
				.toString());
		suite.addTestSuite(ScaleObservableValueSelectionTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		Scale scale;

		@Override
		public void setUp() {
			shell = new Shell();
			scale = new Scale(shell, SWT.NONE);
			scale.setMaximum(1000);
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.selection().observe(realm, scale);
		}

		@Override
		public void change(IObservable observable) {
			scale
					.setSelection(createIntegerValue(
							(IObservableValue) observable).intValue());
			scale.notifyListeners(SWT.Selection, null);
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Integer.TYPE;
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return createIntegerValue(observable);
		}

		private Integer createIntegerValue(IObservableValue observable) {
			return new Integer(((Integer) observable.getValue()).intValue() + 1);
		}
	}
}
