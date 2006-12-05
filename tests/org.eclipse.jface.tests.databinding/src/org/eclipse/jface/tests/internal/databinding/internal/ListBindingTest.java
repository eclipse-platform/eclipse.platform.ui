/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal;

import junit.framework.TestCase;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.IBindingListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 *
 */
public class ListBindingTest extends TestCase {
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
    }
    
    public void testBindingListeners() {
        WritableList v1 = new WritableList();
        WritableList v2 = new WritableList();

        DataBindingContext dbc = new DataBindingContext();
        
        Binding binding = dbc.bindList(v1, v2, null);
        BindingListener listener = new BindingListener();
        binding.addBindingEventListener(listener);

        v2.add(0, "test");
        assertEquals("events count was incorrect", 2, listener.purgeCount());
        
        v2.remove(0);
        assertEquals("events count was incorrect", 2, listener.purgeCount());
        
        v2.add(0, "test2");
        assertEquals("events count was incorrect", 2, listener.purgeCount());

        v2.set(0, "test3");
        assertEquals("events count was incorrect", 2, listener.purgeCount());
    }
    
    private static class BindingListener implements IBindingListener {
        private int count;
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.databinding.IBindingListener#bindingEvent(org.eclipse.jface.databinding.BindingEvent)
         */
        public IStatus bindingEvent(BindingEvent e) {
            count++;
            return Status.OK_STATUS;
        }
        
        int purgeCount() {
            int c = count;
            count = 0;
            return c;
        }
    }
}
