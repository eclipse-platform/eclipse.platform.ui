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
 * Test suite for the Ant Editor
 * 
 * @author Alf Schiefelbein
 */
public class AntEditorTests extends TestSuite {

    public static Test suite() {

        TestSuite suite= new AntEditorTests();
        suite.setName("Ant Editor Unit Tests");
        suite.addTest(CodeCompletionTest.suite());
        suite.addTest(TaskDescriptionProviderTest.suite());
        suite.addTest(AntEditorContentOutlineTests.suite());
        suite.addTest(EnclosingTargetSearchingHandlerTest.suite());
        return suite;
    }
}
