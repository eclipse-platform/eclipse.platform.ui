/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components.inheritance;

import org.eclipse.ui.components.Container;
import org.eclipse.ui.components.FactoryMap;
import org.eclipse.ui.components.ReflectionFactory;

/**
 * @since 3.1
 */
public class TestInheritance {

    public static void main(String[] args) {
        FactoryMap defaultContext = new FactoryMap()
            .map(ITestNameable.class, new ReflectionFactory(DefaultTestNameable.class))
            .map(ITestDirtyHandler.class, new ReflectionFactory(DefaultTestDirtyHandler.class));
        
        FactoryMap multiplexContext = new FactoryMap()
            .map(ITestNameable.class, new ReflectionFactory(ChildTestNameable.class))
            .add(defaultContext);
        
        Container myContainer = new Container(multiplexContext);
        
        
    }
    
    /**
     * Tests the "fallthrough" shorthand. If a component refers to its own type,
     * it should get the implementation inherited from its inherited context
     *
     */
    public void testFallthrough() {
        FactoryMap parentContext = new FactoryMap()
            .map(ITestNameable.class, new ReflectionFactory(DefaultTestNameable.class));
        
        FactoryMap childContext = new FactoryMap()
            .map(ITestNameable.class, new ReflectionFactory(ChildTestNameable.class));
        
        Container myContainer = new Container(childContext);
        
    }
}
