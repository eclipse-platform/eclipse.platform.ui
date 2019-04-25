/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
 *     Matthew Hall - bug 213145, 194734, 195222
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableDelegateTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * @since 3.2
 */
public class ScaleObservableValueMaxTest extends ObservableDelegateTest {
	private Delegate delegate;

	private Scale scale;

	private IObservableValue observable;

	public ScaleObservableValueMaxTest() {
		this(null);
	}

	public ScaleObservableValueMaxTest(String testName) {
		super(testName, new Delegate());
	}

	@Override
	@Before
	public void setUp() throws Exception {
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

	@Test
	public void testGetValue() throws Exception {
		int max = 100;
		scale.setMaximum(max);
		assertEquals(Integer.valueOf(max), observable.getValue());
	}

	@Test
	public void testSetValue() throws Exception {
		int max = 100;
		observable.setValue(Integer.valueOf(max));
		assertEquals(max, scale.getMaximum());
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(SWTMutableObservableValueContractTest.suite(new Delegate()));
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
			return WidgetProperties.maximum().observe(realm, scale);
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
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
			return Integer.valueOf(((Integer) observable.getValue()).intValue() + 1);
		}
	}
}
