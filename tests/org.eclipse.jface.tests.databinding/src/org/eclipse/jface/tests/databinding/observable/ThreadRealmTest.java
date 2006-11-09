/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable;


import junit.framework.TestCase;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.observable.Realm;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.observable.value.IValueChangeListener;
import org.eclipse.jface.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.observable.value.WritableValue;

/**
 * @since 3.2
 */
public class ThreadRealmTest extends TestCase {
    private Thread thread;

    private Thread safetyThread;

    private Thread testThread;

    protected void setUp() throws Exception {
        super.setUp();

        testThread = Thread.currentThread();

        // attempt to ensure the test doesn't run too long
        safetyThread = new Thread() {
            public void run() {
                try {
                    sleep(3000);

                    if (thread != null && thread.isAlive()) {
                        thread.interrupt();
                        testThread.interrupt();
                    }
                } catch (InterruptedException e) {
                    // exit
                }
            }
        };
        
        safetyThread.start();
        Realm.setDefault(null);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        if (safetyThread != null && safetyThread.isAlive()) {
            safetyThread.interrupt();
        }

        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public void testInvokeObservableFromInvalidRealm() throws Exception {
        final ThreadRealm threadRealm = new ThreadRealm();
        thread = new Thread();
        threadRealm.init(thread);

        WritableValue value = new WritableValue(threadRealm, String.class);

        try {
            value.getValue();
            fail("exception should have been thrown");
        } catch (AssertionFailedException e) {
        }
    }

    public void testThreadRealm() throws Exception {
        final String endValue = "value";

        final ThreadRealm threadRealm = new ThreadRealm();
        Runnable runnable = new Runnable() {
            public void run() {
                threadRealm.block();
            }
        };

        thread = new Thread(runnable);
        thread.start();

        threadRealm.init(thread);

        final WritableValue value = new WritableValue(threadRealm, String.class);
        ValueChangeCounter counter = new ValueChangeCounter();
        value.addValueChangeListener(counter);

        assertEquals(0, counter.count);

        value.getRealm().exec(new Runnable() {
            public void run() {
                assertTrue("Realm was not the current.", threadRealm.isCurrent());

                value.setValue(endValue);
            }
        });

        
        // Wait for change events to be dispatched.
        synchronized (counter) {
            while (counter.count == 0) {
                counter.wait();
            }
                        
            threadRealm.unblock();
        }

        assertFalse("ThreadRealm should not be blocking.", threadRealm.isBlocking());
        assertEquals("Value change event count was incorrect.", 1, counter.count);
        assertEquals("Thread of the value change event was not the realm thread.",
                threadRealm.getThread(),
                counter.changeEventThread);
        assertFalse("ThreadRealm should not be the current realm.", threadRealm.isCurrent());
        assertNull("Default realm should be null", Realm.getDefault());
    }

    /**
     * Tracks value change events. Synchronizes on <code>this</code>.
     * 
     * @since 3.2
     */
    private static class ValueChangeCounter implements IValueChangeListener {
        int count;

        Thread changeEventThread;

        public void handleValueChange(IObservableValue source, ValueDiff diff) {
            synchronized (this) {
                count++;
                changeEventThread = Thread.currentThread();
                this.notifyAll();
            }
        }
    }
}
