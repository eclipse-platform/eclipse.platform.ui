/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.tests.ui.separateVM;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.ant.tests.ui.testplugin.ConsoleLineTracker;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests everything about code completion and code assistance.
 * 
 */
public class SeparateVMTests extends AbstractAntUITest {

    /**
     * Constructor for CodeCompletionTest.
     * @param name
     */
    public SeparateVMTests(String name) {
        super(name);
    }
    
	public static Test suite() {
		return new TestSuite(SeparateVMTests.class);
	}

    /**
     * Tests the code completion for attributes of tasks.
     */
    public void testAttributeProposals() throws CoreException {
      	launch("echoing");
      	assertTrue("Incorrect number of messages logged for build. Should be 3. Was " + ConsoleLineTracker.getNumberOfMessages(), ConsoleLineTracker.getNumberOfMessages() == 3);
    }
}