/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 164247
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import junit.framework.TestCase;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.swt.widgets.Display;

/**
 * @since 1.1
 */
public class ObservableMapLabelProviderTest extends TestCase {
    protected void setUp() throws Exception {
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
    }
    
    public void testGetColumnText() throws Exception {
        WritableSet set = new WritableSet(Item.class);
        Item item = new Item();
        String value = "value";
        item.setValue(value);
        set.add(item);
        
        ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(BeansObservables.observeMap(set, Item.class, "value"));
        assertEquals(item.getValue(), labelProvider.getColumnText(item, 0));
    }
    
    public void testGetColumnTextNullValue() throws Exception {
        WritableSet set = new WritableSet(Item.class);
        Item item = new Item();
        set.add(item);   
        
        ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(BeansObservables.observeMap(set, Item.class, "value"));
        assertNull(item.getValue());
        assertEquals("", labelProvider.getColumnText(item, 0));
    }
    
    private static class Item extends ModelObject {
        private String value;
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            String old = this.value;
            
            firePropertyChange("value", old, this.value = value);
        }
    }
}
