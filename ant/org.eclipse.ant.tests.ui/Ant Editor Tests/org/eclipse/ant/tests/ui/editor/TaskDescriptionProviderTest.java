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

package org.eclipse.ant.tests.ui.editor;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.ant.internal.ui.editor.TaskDescriptionProvider;

/**
 * Tests the tasks description provider.
 * 
 */
public class TaskDescriptionProviderTest extends AbstractAntUITest {

    /**
     * Constructor for TaskDescriptionProviderTest.
     * @param name
     */
    public TaskDescriptionProviderTest(String name) {
        super(name);
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
		return new TestSuite(TaskDescriptionProviderTest.class);
    }
}
