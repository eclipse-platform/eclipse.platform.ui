//
// TaskDescriptionProviderTest.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ui.externaltools.internal.ant.editor.TaskDescriptionProvider;

/**
 * Tests the taks description provider.
 * 
 * @version 25.09.2002
 * @author Alf Schiefelbein
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
