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

import org.junit.Before;

import org.eclipse.core.internal.filebuffers.TextFileBufferManager;

import org.eclipse.core.filebuffers.LocationKind;


/**
 * @since 3.4
 */
public class TextFileManagerDocCreationTests extends AbstractFileBufferDocCreationTests {

	@Before
	public void setUp() {
		fManager= new TextFileBufferManager();
	}

	@Override
	protected void assertDocumentContent(String expectedContent, String path, LocationKind locKind) {
		if (locKind != LocationKind.IFILE) {
			/**  {@link TextFileBufferManager} does not deal with {@link LocationKind#IFILE} */
			super.assertDocumentContent(expectedContent, path, locKind);
		}
	}

	@Override
	protected LocationKind[] getSupportLocationKinds() {
		return new LocationKind[] {LocationKind.LOCATION, LocationKind.NORMALIZE};
	}
}
