/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial implementation
 *******************************************************************************/

package org.eclipse.ant.ui.internal.editor.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ant.ui.internal.editor.TaskDescriptionProvider;

/**
 * Tests the tasks description provider.
 * 
 */
public class TaskDescriptionProviderTest extends TestCase {

    /**
     * Constructor for TaskDescriptionProviderTest.
     * @param arg0
     */
    public TaskDescriptionProviderTest(String arg0) {
        super(arg0);
    }

    /** 
     * Tests getting the description of a task.
     */
    public void testGettingTaskDescription() {
        TaskDescriptionProvider provider = new TaskDescriptionProvider();
        String description = provider.getDescriptionForTask("apply");
        assertNotNull(description);
        assertTrue(description.length() > 0);
    }

    /**
     * Tests getting the description of an attribute.
     */
    public void testGettingAttribute() {
        TaskDescriptionProvider provider = new TaskDescriptionProvider();
        String description = provider.getDescriptionForTaskAttribute("apply", "executable");
        assertNotNull(description);
        assertTrue(description.length() > 0);
    }
    
    /**
     * Tests getting the required value of an attribute.
     */
    public void testGettingRequired() {
        TaskDescriptionProvider provider = new TaskDescriptionProvider();
        String required = provider.getRequiredAttributeForTaskAttribute("apply", "executable");
        assertNotNull(required);
        assertEquals("yes", required);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("TaskDescriptionProviderTest");

        suite.addTest(new TaskDescriptionProviderTest("testGettingAttribute"));
        suite.addTest(new TaskDescriptionProviderTest("testGettingRequired"));
        suite.addTest(new TaskDescriptionProviderTest("testGettingTaskDescription"));
        return suite;
    }
}
