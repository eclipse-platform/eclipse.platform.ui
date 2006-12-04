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
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @since 3.2
 */
public class AbstractObservableValueTest extends TestCase {
	private RealmStub realm;
	
	protected void setUp() throws Exception {
		realm = new RealmStub();
	}
	
    public void testSetValueInvalidRealm() throws Exception {        
        ObservableValueStub stub = new ObservableValueStub(realm);
        try {
            stub.setValue(null);
            fail("exception should have been thrown");
        } catch (AssertionFailedException e) {
        }
    }
    
    public void testSetValueDoSetValue() throws Exception {
        class ValueStub extends ObservableValueStub {
            int doSetValue;
            
            ValueStub(Realm realm) {
                super(realm);
            }

            protected void doSetValue(Object value) {
                doSetValue++;
            }      
        }
        
        realm.current = true;
        ValueStub stub = new ValueStub(realm);
        assertEquals(0, stub.doSetValue);
        stub.setValue(new Object());
        assertEquals("doSetValue should have been invoked", 1, stub.doSetValue);
    }
    
    public void testFireValueChangeInvalidRealm() throws Exception {
    	realm.current = false;
    	ObservableValueStub observable = new ObservableValueStub(realm);
    	
    	try {
    		observable.fireValueChange(null);
    		fail("exception should have been thrown");
    	} catch (AssertionFailedException e) {	
    	}
	}
        
    public void testFireValueChangeCurrentRealm() throws Exception {
    	realm.current = true;
    	ObservableValueStub observable = new ObservableValueStub(realm);
    	
    	try {
    		observable.fireValueChange(null);
    	} catch (AssertionFailedException e) {
    	}
	}
    
    private static class RealmStub extends Realm {
    	boolean current;

		public boolean isCurrent() {
			return current;
		}
    }
    
    private static class ObservableValueStub extends AbstractObservableValue {
    	private ObservableValueStub(Realm realm) {
    		super(realm);
    	}
    	
		protected Object doGetValue() {
			return null;
		}

		public Object getValueType() {
			return null;
		}
		
		protected void fireValueChange(ValueDiff diff) {
			super.fireValueChange(diff);
		}
    }
}
