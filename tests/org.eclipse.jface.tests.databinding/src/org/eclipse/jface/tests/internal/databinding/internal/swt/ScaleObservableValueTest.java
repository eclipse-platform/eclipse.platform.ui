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

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.jface.internal.databinding.internal.swt.ScaleObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 1.1
 */
public class ScaleObservableValueTest extends AbstractDefaultRealmTestCase {
	private Shell shell = null;
	private Scale scale;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		shell = new Shell();
		scale = new Scale(shell, SWT.NONE);
		scale.setMaximum(1000); 
	}
	
	public void testMinGetValue() throws Exception {
		ISWTObservableValue value = new ScaleObservableValue(scale, SWTProperties.MIN);
		int min = 100;
		scale.setMinimum(min);
		
		assertEquals(new Integer(100), value.getValue());
	}
	
	public void testMinTypeIsIntegerType() throws Exception {
		assertEquals(Integer.TYPE, new ScaleObservableValue(scale, SWTProperties.MIN).getValueType());
	}
	
	public void testMinSetValue() throws Exception {
		ISWTObservableValue value = new ScaleObservableValue(scale, SWTProperties.MIN);
		int min = 100;
		value.setValue(new Integer(min));
		assertEquals(min, scale.getMinimum());
	}
	
	public void testMaxGetValue() throws Exception {
		ISWTObservableValue value = new ScaleObservableValue(scale, SWTProperties.MAX);
		int max = 100;
		scale.setMaximum(max);
		
		assertEquals(new Integer(100), value.getValue());
	}
	
	public void testMaxSetValue() throws Exception {
		ISWTObservableValue value = new ScaleObservableValue(scale, SWTProperties.MAX);
		int max = 100;
		value.setValue(new Integer(max));
		assertEquals(max, scale.getMaximum());
	}
	
	public void testMaxTypeIsIntegerType() throws Exception {
		assertEquals(Integer.TYPE, new ScaleObservableValue(scale, SWTProperties.MAX).getValueType());
	}
	
	public void testSelectionChangeEvent() throws Exception {
		class ChangeListenerStub implements IChangeListener {
			int count;
			public void handleChange(ChangeEvent event) {
				count++;
			}
		}
		
		ISWTObservableValue value = new ScaleObservableValue(scale, SWTProperties.SELECTION);
		ChangeListenerStub listener = new ChangeListenerStub();
		value.addChangeListener(listener);
		
		scale.setSelection(scale.getSelection() + 1);
		
		assertEquals(0, listener.count);
		scale.notifyListeners(SWT.Selection, null);
		assertEquals(1, listener.count);
	}
	
	public void testSelectionGetValue() throws Exception {
		ISWTObservableValue value = new ScaleObservableValue(scale, SWTProperties.SELECTION);
		int selection = 5;
		scale.setSelection(selection);
		assertEquals(new Integer(5), value.getValue());
	}
	
	public void testSelectionSetValue() throws Exception {
		ISWTObservableValue value = new ScaleObservableValue(scale, SWTProperties.SELECTION);
		int selection = 5;
		value.setValue(new Integer(selection));
		assertEquals(selection, scale.getSelection());
	}	
	
	public void testSelectionTypeIsIntegerType() throws Exception {
		assertEquals(Integer.TYPE, new ScaleObservableValue(scale, SWTProperties.SELECTION).getValueType());
	}
}
