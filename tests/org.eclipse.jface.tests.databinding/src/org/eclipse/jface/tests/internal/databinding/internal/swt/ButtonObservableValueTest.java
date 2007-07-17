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
import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.conformance.databinding.AbstractObservableValueContractDelegate;
import org.eclipse.jface.conformance.databinding.ObservableValueContractTests;
import org.eclipse.jface.conformance.databinding.SuiteBuilder;
import org.eclipse.jface.internal.databinding.internal.swt.ButtonObservableValue;
import org.eclipse.jface.tests.databinding.EventTrackers.ValueChangeEventTracker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class ButtonObservableValueTest extends TestCase {
	public void testSetSelectionNotifiesObservable() throws Exception {
		Shell shell = new Shell();
		Button button = new Button(shell, SWT.CHECK);
		
		ButtonObservableValue observableValue = new ButtonObservableValue(button);
		ValueChangeEventTracker listener = new ValueChangeEventTracker();
		observableValue.addValueChangeListener(listener);
		button.setSelection(true);
		
		//precondition
		assertEquals(0, listener.count);
		button.notifyListeners(SWT.Selection, null);
		
		assertEquals("Selection event should notify observable.", 1, listener.count);
		shell.dispose();
	}

	public static Test suite() {
		Object[] params = new Object[] { new Delegate() };

		return new SuiteBuilder().addTests(ButtonObservableValueTest.class)
				.addParameterizedTests(ObservableValueContractTests.class,
						params).build();
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		Shell shell;

		Button button;

		public void setUp() {
			super.setUp();

			shell = new Shell();
			button = new Button(shell, SWT.CHECK);
		}

		public void tearDown() {
			super.tearDown();

			shell.dispose();
		}

		public IObservableValue createObservableValue() {
			return new ButtonObservableValue(button);
		}

		public Object getValueType(IObservableValue observable) {
			return Boolean.TYPE;
		}

		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(Boolean.TRUE.equals(observableValue
					.getValue()) ? Boolean.FALSE : Boolean.TRUE);
		}
	}
}
