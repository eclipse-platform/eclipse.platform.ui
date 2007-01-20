/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Brad Reynolds - bug 164653, 159768
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal;

import junit.framework.TestCase;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.DefaultBindSpec;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.ValueBinding;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 * 
 */
public class ValueBindingTest extends TestCase {
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
		
		super.tearDown();
	}

	/**
	 * Bug 152543.
	 * 
	 * @throws Exception
	 */
	public void testNoUpdateTargetFromModel() throws Exception {
		DefaultBindSpec spec = new DefaultBindSpec();
		spec.setUpdateModel(false);
		spec.setUpdateTarget(false);

		try {
			new ValueBinding(new DataBindingContext(),
					new ObservableValueStub(), new ObservableValueStub(), spec);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
    
    public void testValuePropagation() throws Exception {
        DefaultBindSpec spec = new DefaultBindSpec();
        String initialValue = "value";
        
        WritableValue target = new WritableValue(String.class);
        WritableValue model = new WritableValue(String.class, initialValue);
        
        DataBindingContext dbc = new DataBindingContext();
        
        assertFalse(model.getValue().equals(target.getValue()));
        new ValueBinding(dbc, target, model, spec);
        
        assertEquals(target.getValue(), model.getValue());
    }

	private static class ObservableValueStub extends AbstractObservableValue {
		protected Object doGetValue() {
			// do nothing
			return null;
		}

		public Object getValueType() {
			// do nothing
			return null;
		}

		protected void doSetValue(Object value) {

		}
	}
}
