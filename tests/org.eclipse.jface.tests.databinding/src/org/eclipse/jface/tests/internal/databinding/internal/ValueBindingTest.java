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

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.DefaultBindSpec;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.ValueBinding;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class ValueBindingTest extends AbstractDefaultRealmTestCase {
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
			new ValueBinding(
					new ObservableValueStub(), new ObservableValueStub(), spec).init(new DataBindingContext());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
    
    public void testValuePropagation() throws Exception {
        DefaultBindSpec spec = new DefaultBindSpec();
        String initialValue = "value";
        
        WritableValue target = new WritableValue(null, String.class);
        WritableValue model = new WritableValue(initialValue, String.class);
        
        DataBindingContext dbc = new DataBindingContext();
        
        assertFalse(model.getValue().equals(target.getValue()));
        new ValueBinding(target, model, spec).init(dbc);
        
        assertEquals(target.getValue(), model.getValue());
    }
    
    public void testGetTarget() throws Exception {
    	WritableValue target = new WritableValue();
    	WritableValue model = new WritableValue();
		ValueBinding valueBinding = new ValueBinding(target, model, new BindSpec());
    	
		assertEquals(target, valueBinding.getTarget());
	}
    
    public void testGetModel() throws Exception {
    	WritableValue target = new WritableValue();
    	WritableValue model = new WritableValue();
		ValueBinding valueBinding = new ValueBinding(target, model, new BindSpec());
    	
		assertEquals(model, valueBinding.getModel());
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
