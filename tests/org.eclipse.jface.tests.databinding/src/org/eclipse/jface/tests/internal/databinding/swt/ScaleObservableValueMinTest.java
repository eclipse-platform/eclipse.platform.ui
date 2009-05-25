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
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class ScaleObservableValueMinTest extends ObservableDelegateTest {
	private Delegate delegate;

	private Scale scale;

	private IObservableValue observable;

	public ScaleObservableValueMinTest() {
		this(null);
	}

	public ScaleObservableValueMinTest(String testName) {
		super(testName, new Delegate());
	}

	protected void setUp() throws Exception {
		super.setUp();

		delegate = (Delegate) getObservableContractDelegate();
		observable = (IObservableValue) getObservable();
		scale = delegate.scale;
	}

	protected IObservable doCreateObservable() {
		return getObservableContractDelegate().createObservable(
				SWTObservables.getRealm(Display.getDefault()));
	}

	public void testGetValue() throws Exception {
		int min = 100;
		scale.setMinimum(min);
		assertEquals(new Integer(min), observable.getValue());
	}

	public void testSetValue() throws Exception {
		int min = 100;
		observable.setValue(new Integer(min));
		assertEquals(min, scale.getMinimum());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ScaleObservableValueMinTest.class
				.toString());
		suite.addTestSuite(ScaleObservableValueMinTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		Scale scale;

		public void setUp() {
			shell = new Shell();
			scale = new Scale(shell, SWT.NONE);
			scale.setMaximum(1000);
		}

		public void tearDown() {
			shell.dispose();
		}

		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.minimum().observe(realm, scale);
		}

		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		public Object getValueType(IObservableValue observable) {
			return Integer.TYPE;
		}

		public Object createValue(IObservableValue observable) {
			return createIntegerValue(observable);
		}

		private Integer createIntegerValue(IObservableValue observable) {
			return new Integer(((Integer) observable.getValue()).intValue() + 1);
		}
	}
}
