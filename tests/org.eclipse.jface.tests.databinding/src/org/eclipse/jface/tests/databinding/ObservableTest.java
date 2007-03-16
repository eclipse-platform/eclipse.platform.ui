/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.WritableValue;

public class ObservableTest extends AbstractDefaultRealmTestCase {

	private static class MyObservable extends WritableValue {
		/**
		 * @param initialValue
		 */
		public MyObservable(Object initialValue) {
			super(initialValue, null);
		}

		public void fireChange(Object oldValue, Object newValue) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}

	private MyObservable observable;

	protected void setUp() throws Exception {
		super.setUp();
        
		observable = new MyObservable(null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (observable != null) {
			try {
				observable.dispose();
			} catch (Exception ex) {
				// ignore
			}
		}
		observable = null;
	}

    private static class ChangeListener implements IChangeListener {
        int count;
        IObservable source;
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.databinding.observable.IChangeListener#handleChange(org.eclipse.jface.databinding.observable.IObservable)
         */
        public void handleChange(ChangeEvent event) {
            count++;
            this.source = event.getObservable();
        }        
    }
    
	/*
	 * Test method for
	 * 'org.eclipse.jface.databinding.Observable.addChangeListener(IChangeListener)'
	 */
	public void testFireChangeListener() {
        ChangeListener listener = new ChangeListener();
        observable.addChangeListener(listener);
        assertEquals("listener should not have been invoked", 0, listener.count);
        
        observable.fireChange(null, null);
        assertEquals("listener should have been invoked", 1, listener.count);
	}

	/*
	 * Test method for
	 * 'org.eclipse.jface.databinding.Observable.removeChangeListener(IChangeListener)'
	 */
	public void testRemoveChangeListener() {
        ChangeListener listener = new ChangeListener();
        observable.addChangeListener(listener);
        
        observable.removeChangeListener(listener);
        observable.fireChange(null, null);        
        assertEquals("the listener should have been removed", 0, listener.count);
	}
}
