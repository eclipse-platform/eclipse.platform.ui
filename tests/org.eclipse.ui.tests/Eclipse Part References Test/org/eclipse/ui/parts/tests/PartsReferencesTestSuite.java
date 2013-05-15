/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.parts.tests;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.parts.tests.util.PartsTestUtil;
import org.eclipse.ui.tests.harness.util.FileUtil;

/**
 * Test suite to check the behavior of parts (editors and views) creation on
 * start up. Suite written for Bug 66065.
 */
public class PartsReferencesTestSuite {

    /**
     * Constructor.
     * 
     * @return the test.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();

        suite.addTest(new TestSuite(ViewsReferencesTest.class));
        suite.addTest(new TestSuite(EditorsReferencesTest.class));

        /**
         * Wrapper to set up the tests. Ensures the creation of files on set up
         * and the deletion on tear down.
         */
        TestSetup wrapper = new TestSetup(suite) {

            /*
             * (non-Javadoc)
             * 
             * @see junit.extensions.TestSetup#setUp()
             */
            protected void setUp() {
                try {
                    IProject testProject = FileUtil
                            .createProject(PartsTestUtil.projectName); //$NON-NLS-1$
                    for (int index = 0; index < PartsTestUtil.numOfParts; index++) {
                        FileUtil.createFile(PartsTestUtil.getFileName(index),
                                testProject);
                    }
                } catch (CoreException e) {
                    e.printStackTrace(System.err);
                }

            }

            /*
             * (non-Javadoc)
             * 
             * @see junit.extensions.TestSetup#tearDown()
             */
            protected void tearDown() {
                try {
                    FileUtil.deleteProject(PartsTestUtil.getProject());
                } catch (CoreException e) {
                    e.printStackTrace(System.err);
                }
            }
        };

        return wrapper;
    }
}
