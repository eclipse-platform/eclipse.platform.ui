/*******************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 206839)
 *     Matthew Hall - bug 213145, 194734, 195222
 *******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.viewers;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for ViewerInputObservableValue.
 * 
 * @since 1.2
 */
public class ViewerInputObservableValueTest extends
		AbstractDefaultRealmTestCase {
	private TableViewer viewer;
	private static String[] model = new String[] { "0", "1" };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Shell shell = new Shell();
		viewer = new TableViewer(shell, SWT.NONE);
		viewer.setContentProvider(new ContentProvider());
	}

	@Override
	protected void tearDown() throws Exception {
		Shell shell = viewer.getTable().getShell();
		if (!shell.isDisposed())
			shell.dispose();
		super.tearDown();
	}

	public void testConstructor_IllegalArgumentException() {
		try {
			ViewersObservables.observeInput(null);
			fail("Expected IllegalArgumentException for null argument");
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testSetInputOnViewer_FiresChangeEventOnGetValue() {
		IObservableValue observable = ViewersObservables.observeInput(viewer);
		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);

		assertNull(viewer.getInput());
		assertEquals(0, listener.count);

		viewer.setInput(model);

		assertEquals(model, viewer.getInput());
		assertEquals(0, listener.count);

		// Call to getValue() causes observable to discover change
		assertEquals(model, observable.getValue());
		assertEquals(1, listener.count);

		viewer.setInput(null);
		assertEquals(null, viewer.getInput());

		assertEquals(null, observable.getValue());
		assertEquals(2, listener.count);
	}

	public void testGetSetValue_FiresChangeEvents() {
		IObservableValue observable = ViewersObservables.observeInput(viewer);
		ValueChangeEventTracker listener = new ValueChangeEventTracker();
		observable.addValueChangeListener(listener);

		assertNull(observable.getValue());
		assertEquals(0, listener.count);

		observable.setValue(model);

		assertEquals(model, observable.getValue());
		assertEquals(1, listener.count);
		assertValueChangeEventEquals(observable, null, model, listener.event);

		observable.setValue(null);

		assertNull(observable.getValue());
		assertEquals(2, listener.count);
		assertValueChangeEventEquals(observable, model, null, listener.event);
	}

	public void testGetValueType_AlwaysNull() throws Exception {
		IObservableValue observable = ViewersObservables.observeInput(viewer);
		assertEquals(null, observable.getValueType());
	}

	public void testDispose() throws Exception {
		IObservableValue observable = ViewersObservables.observeInput(viewer);
		observable.dispose();
		try {
			observable.setValue(model);
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
		}
	}

	private void assertValueChangeEventEquals(
			IObservableValue expectedObservable, Object expectedOldValue,
			Object expectedNewValue, ValueChangeEvent event) {
		assertSame(expectedObservable, event.getObservableValue());
		assertEquals(expectedOldValue, event.diff.getOldValue());
		assertEquals(expectedNewValue, event.diff.getNewValue());
	}

	static class ContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return (String[]) inputElement;
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ViewerInputObservableValueTest.class
				.getName());
		suite.addTestSuite(ViewerInputObservableValueTest.class);
		suite.addTest(MutableObservableValueContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		TableViewer viewer;

		@Override
		public void setUp() {
			super.setUp();
			Shell shell = new Shell();
			viewer = new TableViewer(shell, SWT.NONE);
			viewer.setContentProvider(new ContentProvider());
		}

		@Override
		public void tearDown() {
			Shell shell = viewer.getTable().getShell();
			if (!shell.isDisposed())
				shell.dispose();
			super.tearDown();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return ViewerProperties.input().observe(realm, viewer);
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue value = (IObservableValue) observable;
			value.setValue(createValue(value));
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return new String[] { "one", "two" };
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return null;
		}
	}
}
