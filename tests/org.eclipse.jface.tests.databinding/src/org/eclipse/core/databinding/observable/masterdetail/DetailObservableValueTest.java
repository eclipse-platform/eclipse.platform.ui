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

package org.eclipse.core.databinding.observable.masterdetail;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class DetailObservableValueTest extends TestCase {
    public void testSetValue() throws Exception {
        Realm realm = SWTObservables.getRealm(Display.getDefault());
        final WritableValue innerObservable = new WritableValue(realm, Object.class);
        WritableValue outerObservable = new WritableValue(realm, Object.class, innerObservable);
        outerObservable.setValue(innerObservable);
        
        IObservableFactory factory = new IObservableFactory() {
            public IObservable createObservable(Object target) {
                return innerObservable;
            }
        };
        
        IObservableValue detailObservable = MasterDetailObservables.detailValue(outerObservable, factory, Object.class);
        Object value = new Object();
        
        assertFalse(value.equals(innerObservable.getValue()));
        detailObservable.setValue(value);
        assertEquals("inner value", value, innerObservable.getValue());
    }
}
