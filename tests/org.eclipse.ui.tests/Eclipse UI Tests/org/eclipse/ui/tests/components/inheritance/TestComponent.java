/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components.inheritance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IServiceProvider;

/**
 * @since 3.1
 */
public class TestComponent {
    private IServiceProvider container;
    private List dependencies = new ArrayList();
    
    private static final class Dep {
        public Dep(Object k, Object v) {
            key = k;
            value = v;
        }
        
        public Object key;
        public Object value;
    }
    
    public TestComponent(IServiceProvider container) {
        this.container = container;
    }
    
    public Object getDep(Object dependencyKey) throws ComponentException {
        Object result = container.getService(dependencyKey);
        dependencies.add(new Dep(dependencyKey, result));
        return result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        
        result.append(this.getClass().getName());
        
        boolean first = true;
        
        for (Iterator iter = dependencies.iterator(); iter.hasNext();) {
            Dep d = (Dep) iter.next();
            
            if (first) {
                result.append(" (");
            } else {
                result.append(", ");
            }
            
            result.append(d.key.toString());
            result.append(" -> ");
            result.append(d.value.toString());
            
            first = false;
        }
        
        if (!first) {
            result.append(")");
        }
        
        return result.toString();
    }
}
