/*******************************************************************************
 * Copyright (c) 2006, 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;

/**
 * @since 3.2
 */
public class AbstractVetoableValueTest extends TestCase {
    public void testSetValueInvokesDoSetApprovedValue() throws Exception {
        class VetoableValue extends VetoableValueStub {
            int count;
            Object value;
            
            VetoableValue(Realm realm) {
                super(realm);
            }
            
            protected void doSetApprovedValue(Object value) {
                count++;
                this.value = value;
            }      
        }
        
        Realm realm = new CurrentRealm(true);
        VetoableValue vetoableValue = new VetoableValue(realm);
        assertEquals(0, vetoableValue.count);
        assertEquals(null, vetoableValue.value);
        
        Object value = new Object();
        vetoableValue.setValue(value);
        assertEquals(1, vetoableValue.count);
        assertEquals(value, vetoableValue.value);
    }
    
    public void testFireValueChangeRealmChecks() throws Exception {
    	RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				VetoableValueStub observable = new VetoableValueStub();
				observable.fireValueChanging(null);
			}
    	});
	}
    
    private static class VetoableValueStub extends AbstractVetoableValue {
    	VetoableValueStub() {
    		this(Realm.getDefault());
    	}
    	
    	VetoableValueStub(Realm realm) {
    		super(realm);
    	}
    	
		protected void doSetApprovedValue(Object value) {
		}

		protected Object doGetValue() {
			return null;
		}


		public Object getValueType() {
			return null;
		}    	
		
		protected boolean fireValueChanging(ValueDiff diff) {
			return super.fireValueChanging(diff);
		}
    }
}
