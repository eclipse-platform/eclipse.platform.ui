/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM - moved to debug platform tests from JDT
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import junit.framework.TestCase;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.XMLMemento;

/**
 * Test the serialization of presentation context properties.
 * 
 * @since 3.4
 */
public class PresentationContextTests extends TestCase {

    public PresentationContextTests(String name) {
        super(name);
    }
    
    /**
     * Tests saving and restoring presentation context properties.
     */
    public void testSaveRestore () {
        PresentationContext context = new PresentationContext("test");
        context.setProperty("string", "string");
        context.setProperty("integer", new Integer(1));
        context.setProperty("boolean", new Boolean(true));
        context.setProperty("persistable", ResourcesPlugin.getWorkspace().getRoot().getAdapter(IPersistableElement.class));

        final XMLMemento memento = XMLMemento.createWriteRoot("TEST"); 
        context.saveProperites(memento);
        
        context = new PresentationContext("test");
        context.initProperties(memento);
        assertEquals("Wrong value restored", "string", context.getProperty("string"));
        assertEquals("Wrong value restored", new Integer(1), context.getProperty("integer"));
        assertEquals("Wrong value restored", new Boolean(true), context.getProperty("boolean"));
        assertEquals("Wrong value restored", ResourcesPlugin.getWorkspace().getRoot(), context.getProperty("persistable"));
        context.dispose();
    }
    
}
