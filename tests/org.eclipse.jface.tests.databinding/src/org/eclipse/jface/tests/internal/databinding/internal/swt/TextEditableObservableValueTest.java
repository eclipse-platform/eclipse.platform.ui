/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.conformance.databinding.AbstractObservableValueContractDelegate;
import org.eclipse.jface.conformance.databinding.ObservableDelegateTest;
import org.eclipse.jface.conformance.databinding.SWTMutableObservableValueContractTest;
import org.eclipse.jface.conformance.databinding.SWTObservableValueContractTest;
import org.eclipse.jface.conformance.databinding.SuiteBuilder;
import org.eclipse.jface.internal.databinding.internal.swt.TextEditableObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.1
 */
public class TextEditableObservableValueTest extends
		ObservableDelegateTest {
	
	private Delegate delegate;
	private Text text;
	private IObservableValue observable;

	public TextEditableObservableValueTest() {
		this(null);
	}
	
	public TextEditableObservableValueTest(String testName) {
		super(testName, new Delegate());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.conformance.databinding.ObservableDelegateTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		delegate = (Delegate) getObservableContractDelegate();
		observable = (IObservableValue) getObservable();
		text = delegate.text;
	}
	
	protected IObservable doCreateObservable() {
		return super.doCreateObservable();
	}
	
	public void testGetValue() throws Exception {
		text.setEditable(false);
		assertEquals(Boolean.valueOf(text.getEditable()), observable.getValue());
		
		text.setEditable(true);
		assertEquals(Boolean.valueOf(text.getEditable()), observable.getValue());
	}
	
	public void testSetValue() throws Exception {
		text.setEditable(false);
		observable.setValue(Boolean.TRUE);
		assertEquals(Boolean.TRUE, Boolean.valueOf(text.getEditable()));
		
		observable.setValue(Boolean.FALSE);
		assertEquals(Boolean.FALSE, Boolean.valueOf(text.getEditable()));
	}
	
	public static Test suite() {
		Delegate delegate = new Delegate();
		return new SuiteBuilder().addTests(
				TextEditableObservableValueTest.class)
				.addObservableContractTest(
						SWTObservableValueContractTest.class, delegate)
				.addObservableContractTest(
						SWTMutableObservableValueContractTest.class, delegate).build();
	}
	
	/*package*/ static class Delegate extends AbstractObservableValueContractDelegate {
		private Shell shell;
		Text text;
		
		public void setUp() {			
			shell = new Shell();
			text = new Text(shell, SWT.NONE);
		}

		public void tearDown() {			
			shell.dispose();
		}
		
		public IObservableValue createObservableValue(Realm realm) {
			return new TextEditableObservableValue(realm, text);
		}
		
		public Object getValueType(IObservableValue observable) {
			return Boolean.TYPE;
		}
		
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}
		
		public Object createValue(IObservableValue observable) {
			return (Boolean.TRUE.equals(observable.getValue()) ? Boolean.FALSE: Boolean.TRUE);
		}
	}
}
