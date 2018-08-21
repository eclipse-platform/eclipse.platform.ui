/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.core.filebuffers.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * Test Suite for org.eclipse.core.filebuffers.
 *
 * @since 3.0
 */
@RunWith(Suite.class)
@SuiteClasses({ FileBufferCreation.class,
		FileBuffersForWorkspaceFiles.class,
		FileBuffersForExternalFiles.class,
		FileBuffersForLinkedFiles.class,
		FileBuffersForFilesInLinkedFolders.class,
		FileBuffersForNonExistingExternalFiles.class,
		FileBuffersForNonExistingWorkspaceFiles.class,
		FileBuffersForNonAccessibleWorkspaceFiles.class,
		FileStoreFileBuffersForWorkspaceFiles.class,
		FileStoreFileBuffersForExternalFiles.class,
		FileStoreFileBuffersForNonExistingExternalFiles.class,
		FileStoreFileBuffersForNonExistingWorkspaceFiles.class,
		TextFileManagerDocCreationTests.class,
		ResourceTextFileManagerDocCreationTests.class
})
public class FileBuffersTestSuite {
	// see @SuiteClasses
}
