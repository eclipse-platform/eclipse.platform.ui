/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.filebuffers.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test Suite for org.eclipse.core.filebuffers.
 * 
 * @since 3.0
 */
public class FileBuffersTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Suite for org.eclipse.core.filebuffers"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(FileBufferCreation.class);
		suite.addTestSuite(FileBuffersForWorkspaceFiles.class);
		suite.addTestSuite(FileBuffersForExternalFiles.class);
		suite.addTestSuite(FileBuffersForLinkedFiles.class);
		suite.addTestSuite(FileBuffersForFilesInLinkedFolders.class);
		suite.addTestSuite(FileBuffersForNonExistingExternalFiles.class);
		suite.addTestSuite(FileBuffersForNonExistingWorkspaceFiles.class);
		suite.addTestSuite(FileBuffersForNonAccessibleWorkspaceFiles.class);
		//$JUnit-END$
		return suite;
	}
}
