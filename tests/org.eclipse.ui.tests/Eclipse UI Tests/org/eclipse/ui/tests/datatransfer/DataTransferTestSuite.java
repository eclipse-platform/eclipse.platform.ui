/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer;

import org.junit.runner.RunWith;

import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(org.junit.runners.AllTests.class)
public class DataTransferTestSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new DataTransferTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public DataTransferTestSuite() {
        addTest(new TestSuite(ImportOperationTest.class));
        addTest(new TestSuite(ImportArchiveOperationTest.class));
        addTest(new TestSuite(ExportFileSystemOperationTest.class));
        addTest(new TestSuite(ExportArchiveFileOperationTest.class));
        addTest(ImportExistingProjectsWizardTest.suite());
        addTest(new TestSuite(ImportExportWizardsCategoryTests.class));
		addTest(new TestSuite(SmartImportTests.class));
		addTest(new TestSuite(ZipSlipTests.class));
    }
}
