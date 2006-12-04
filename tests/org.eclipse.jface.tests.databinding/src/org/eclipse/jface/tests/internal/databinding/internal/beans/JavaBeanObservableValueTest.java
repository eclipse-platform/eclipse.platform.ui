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

package org.eclipse.jface.tests.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class JavaBeanObservableValueTest extends TestCase {
    public void testSetValue() throws Exception {
        Realm realm = SWTObservables.getRealm(Display.getDefault());
        Bean bean = new Bean();
        
        JavaBeanObservableValue observableValue = new JavaBeanObservableValue(realm, bean, new PropertyDescriptor("value", Bean.class), String.class);
        String value = "value";
        assertNull(observableValue.getValue());
        observableValue.setValue(value);
        assertEquals("value", value, observableValue.getValue());
    }
    
    public class Bean {
        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        private String value;
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            support.firePropertyChange("value", this.value, this.value = value);
        }
    }
}
