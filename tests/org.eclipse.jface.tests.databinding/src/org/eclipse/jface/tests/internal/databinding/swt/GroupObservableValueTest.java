/*******************************************************************************
 * Copyright (c) 2006, 2015 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 213145, 194734, 195222
 *     Eugen Neufeld - bug 461560
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableDelegateTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.swt.SWTObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class GroupObservableValueTest extends ObservableDelegateTest {
	private Delegate delegate;
	private IObservableValue observable;
	private Group group;

	public GroupObservableValueTest() {
		super(new Delegate());
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		delegate = (Delegate) getObservableContractDelegate();
		observable = (IObservableValue) getObservable();
		group = delegate.group;
	}

	@Override
	protected IObservable doCreateObservable() {
		return getObservableContractDelegate().createObservable(
				DisplayRealm.getRealm(Display.getDefault()));
	}

	@Test
	public void testSetValue() throws Exception {
		// preconditions
		assertEquals("", group.getText());
		assertEquals("", observable.getValue());

		String value = "value";
		observable.setValue(value);
		assertEquals("label text", value, group.getText());
		assertEquals("observable value", value, observable.getValue());
	}

	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(SWTMutableObservableValueContractTest.class, new Delegate());
		suite.addTest(SWTObservableValueContractTest.class, new Delegate());
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		Group group;

		@Override
		public void setUp() {
			shell = new Shell();
			group = new Group(shell, SWT.TITLE);
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.text().observe(realm, group);
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
