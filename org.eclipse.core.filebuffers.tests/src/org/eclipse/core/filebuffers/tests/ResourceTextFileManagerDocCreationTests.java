/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation - [api] enable document setup participants to customize behaviour based on resource being opened - https://bugs.eclipse.org/bugs/show_bug.cgi?id=208881
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;

import org.eclipse.core.internal.filebuffers.ResourceTextFileBufferManager;

import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.LocationKind;


/**
 * @since 3.4
 */
public class ResourceTextFileManagerDocCreationTests extends AbstractFileBufferDocCreationTests {

	@Before
	public void setUp() {
		fManager= new ResourceTextFileBufferManager();
	}

	@Override
	protected void assertDocumentContent(String expectedContent, String fullPath, LocationKind locKind) {
		assertEquals(expectedContent, fManager.createEmptyDocument(new Path(fullPath), locKind).get());
		if (locKind == LocationKind.IFILE) {
			IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
			assertEquals(expectedContent, ((ResourceTextFileBufferManager)fManager).createEmptyDocument(file).get());
		}
	}

	@Override
	protected LocationKind[] getSupportLocationKinds() {
		return new LocationKind[] {LocationKind.IFILE, LocationKind.LOCATION, LocationKind.NORMALIZE};
	}
}
