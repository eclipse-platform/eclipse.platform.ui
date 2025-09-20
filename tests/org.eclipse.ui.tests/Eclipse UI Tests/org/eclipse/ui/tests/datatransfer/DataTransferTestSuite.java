/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 546546: migrate to JUnit4 suite
 *     Latha Patil (ETAS GmbH) - GitHub Issue 748
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ //
		ImportOperationTest.class, //
		ImportArchiveOperationTest.class, //
		ExportFileSystemOperationTest.class, //
		ExportArchiveFileOperationTest.class, //
		ImportExistingProjectsWizardTest.class, //
		ImportExistingArchiveProjectFilterTest.class, //
		ImportExportWizardsCategoryTests.class, //
		SmartImportTests.class, //
		ZipSlipTests.class, //
})
public class DataTransferTestSuite {
}
