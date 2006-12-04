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

package org.eclipse.jface.tests.databinding.observable.value;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @since 3.2
 */
public class AbstractVetoableValueTest extends TestCase {
	private RealmStub realm;
	
	protected void setUp() throws Exception {
		realm = new RealmStub();
		realm.current = true;
	}
	
    /**
     * Asserts that doSetVetoableValue is invoked.
     * 
     * @throws Exception
     */
    public void testDoSetApprovedValue() throws Exception {
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
        
        realm.current = true;
        VetoableValue vetoableValue = new VetoableValue(realm);
        assertEquals(0, vetoableValue.count);
        assertEquals(null, vetoableValue.value);
        
        Object value = new Object();
        vetoableValue.setValue(value);
        assertEquals(1, vetoableValue.count);
        assertEquals(value, vetoableValue.value);
    }
    
    public void testFireValueChangingInvalidRealm() throws Exception {
    	VetoableValueStub observable = new VetoableValueStub(realm);
    	realm.current = false;
    	
    	try {
    		observable.fireValueChanging(null);
    		fail("exception should have been thrown");
    	} catch (AssertionFailedException e) {
    	}
	}
    
    public void testFireValueChangingCurrentRealm() throws Exception {
		VetoableValueStub observable = new VetoableValueStub(realm);
		realm.current = true;
		
		try {
			observable.fireValueChanging(null);
		} catch (AssertionFailedException e) {
			fail("exception should not have been thrown");
		}
	}
    
    private static class RealmStub extends Realm {
    	boolean current;
    	
		public boolean isCurrent() {
			return current;
		}    	
    }
    
    private static class VetoableValueStub extends AbstractVetoableValue {
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
