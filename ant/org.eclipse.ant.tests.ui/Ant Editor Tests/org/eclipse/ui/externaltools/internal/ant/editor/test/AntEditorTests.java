/**********************************************************************
This program and the accompanying materials are made available under 
the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
        suite.addTest(new TestSuite(CodeCompletionTest.class));
        suite.addTest(new TestSuite(TaskDescriptionProviderTest.class));
        suite.addTest(new TestSuite(AntEditorContentOutlineTests.class));
        suite.addTest(new TestSuite(EnclosingTargetSearchingHandlerTest.class));
        return suite;
    }
}
