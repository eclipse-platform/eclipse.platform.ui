/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
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
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.swt.LabelObservableValue;
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
	
	protected void setUp() throws Exception {
		super.setUp();
		
		delegate = (Delegate) getObservableContractDelegate();
		observable = (IObservableValue) getObservable();
		label = delegate.label;
	}
	
	protected IObservable doCreateObservable() {
		return getObservableContractDelegate().createObservable(SWTObservables.getRealm(Display.getDefault()));
	}
	
    public void testSetValue() throws Exception {
    	//preconditions
        assertEquals("", label.getText());
        assertEquals("", observable.getValue());
        
        String value = "value";
        observable.setValue(value);
        assertEquals("label text", value, label.getText());
        assertEquals("observable value", value, observable.getValue());
    }
    
    public static Test suite() {
		Delegate delegate = new Delegate();
		return new SuiteBuilder().addTests(LabelObservableValueTest.class).addObservableContractTest(
				SWTObservableValueContractTest.class, delegate)
				.addObservableContractTest(
						SWTMutableObservableValueContractTest.class, delegate)
				.build();
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		Label label;

		public void setUp() {
			shell = new Shell();
			label = new Label(shell, SWT.NONE);
		}

		public void tearDown() {
			shell.dispose();
		}

		public IObservableValue createObservableValue(Realm realm) {
			return new LabelObservableValue(realm, label);
		}

		public void change(IObservable observable) {
			IObservableValue value = (IObservableValue) observable;
			value.setValue(value.getValue() + "a");
		}
		
		public Object getValueType(IObservableValue observable) {
			return String.class;
		}
		
		public Object createValue(IObservableValue observable) {
			return observable.getValue() + "a";
		}
	}
}
