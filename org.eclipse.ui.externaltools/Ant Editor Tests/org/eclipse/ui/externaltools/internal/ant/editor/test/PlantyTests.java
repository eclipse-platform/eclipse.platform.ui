//
// PlantyTests.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.test;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test suite for Planty.
 * 
 * @version 25.09.2002
 * @author Alf Schiefelbein
 */
public class PlantyTests extends TestSuite {

    public static Test suite() {

        TestSuite suite= new PlantyTests();
        suite.setName("Planty Unit Test");
        suite.addTest(CodeCompletionTest.suite());
        suite.addTest(TaskDescriptionProviderTest.suite());
        suite.addTest(PlantyContentOutlineTest.suite());
        suite.addTest(EnclosingTargetSearchingHandlerTest.suite());
        return suite;
    }
        
}
