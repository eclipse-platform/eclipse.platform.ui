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
package org.eclipse.ui.tests.components;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.components.Container;
import org.eclipse.ui.internal.components.ComponentUtil;
import org.osgi.framework.Bundle;

/**
 * @since 3.1
 */
public class ComponentTest extends TestCase {
    String namespace;
    String componentTypeName;
    Class type;
    String context;
    
    Container container;
    
    /**
     * 
     */
    public ComponentTest(String namespace, String componentType, String context) {
        super("Create component " + componentType);
        this.componentTypeName = componentType;
        this.context = context;
        this.namespace = namespace;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        Bundle bundle = Platform.getBundle(namespace);
        Assert.assertNotNull(bundle);
        type = bundle.loadClass(componentTypeName);
        Assert.assertNotNull(type);
        
        container = new Container(ComponentUtil.getContext(context));
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    protected void runTest() throws Throwable {
        Object component = container.getService(type);
        assertNotNull(component);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if (container != null) {
            container.dispose();
            container = null;
        }
        super.tearDown();
    }
}
