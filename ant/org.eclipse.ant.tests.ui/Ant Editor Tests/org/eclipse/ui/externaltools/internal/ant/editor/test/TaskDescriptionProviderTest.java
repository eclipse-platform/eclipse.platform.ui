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

package org.eclipse.ui.externaltools.internal.ant.editor.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ant.ui.internal.editor.TaskDescriptionProvider;

/**
 * Tests the taks description provider.
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
        TaskDescriptionProvider tempProvider = new TaskDescriptionProvider();
        String tempDescription = tempProvider.getDescriptionForTask("apply");
        assertNotNull(tempDescription);
        assertTrue(tempDescription.length() > 0);
    }

    /**
     * Tests getting the description of an attribute.
     */
    public void testGettingAttribute() {
        TaskDescriptionProvider tempProvider = new TaskDescriptionProvider();
        String tempDescription = tempProvider.getDescriptionForTaskAttribute("apply", "executable");
        assertNotNull(tempDescription);
        assertTrue(tempDescription.length() > 0);
    }
    
    /**
     * Tests getting the required value of an attribute.
     */
    public void testGettingRequired() {
        TaskDescriptionProvider tempProvider = new TaskDescriptionProvider();
        String tempRequired = tempProvider.getRequiredAttributeForTaskAttribute("apply", "executable");
        assertNotNull(tempRequired);
        assertEquals("yes", tempRequired);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("TaskDescriptionProviderTest");

        suite.addTest(new TaskDescriptionProviderTest("testGettingAttribute"));
        suite.addTest(new TaskDescriptionProviderTest("testGettingRequired"));
        suite.addTest(new TaskDescriptionProviderTest("testGettingTaskDescription"));
        return suite;
    }
}
