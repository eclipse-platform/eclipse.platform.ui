/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 213145, 194734, 195222
 ******************************************************************************/

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class LabelObservableValueTest extends ObservableDelegateTest {
	private Delegate delegate;
	private IObservableValue observable;
	private Label label;

	public LabelObservableValueTest() {
		this(null);
	}

	public LabelObservableValueTest(String testName) {
		super(testName, new Delegate());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		delegate = (Delegate) getObservableContractDelegate();
		observable = (IObservableValue) getObservable();
		label = delegate.label;
	}

	@Override
	protected IObservable doCreateObservable() {
		return getObservableContractDelegate().createObservable(
				DisplayRealm.getRealm(Display.getDefault()));
	}

	public void testSetValue() throws Exception {
		// preconditions
		assertEquals("", label.getText());
		assertEquals("", observable.getValue());

		String value = "value";
		observable.setValue(value);
		assertEquals("label text", value, label.getText());
		assertEquals("observable value", value, observable.getValue());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(LabelObservableValueTest.class
				.toString());
		suite.addTestSuite(LabelObservableValueTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		Label label;

		@Override
		public void setUp() {
			shell = new Shell();
			label = new Label(shell, SWT.NONE);
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.text().observe(realm, label);
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue value = (IObservableValue) observable;
			value.setValue(value.getValue() + "a");
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return String.class;
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return observable.getValue() + "a";
		}
	}
}
