/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.toc;

import junit.framework.TestCase;

import org.eclipse.help.internal.webapp.data.IconFinder;

public class TocIconPathTest extends TestCase {

	public void testNullId() {
		assertNull(IconFinder.getIconAltFromId(null));
		assertNull(IconFinder.getImagePathFromId(null, IconFinder.TYPEICON_CLOSED));
		assertNull(IconFinder.getImagePathFromId(null, IconFinder.TYPEICON_OPEN));
		assertNull(IconFinder.getImagePathFromId(null, IconFinder.TYPEICON_LEAF));
	}
	
	public void testBadId() {
		assertNull(IconFinder.getIconAltFromId("nosuchid"));
		assertNull(IconFinder.getImagePathFromId("nosuchid", IconFinder.TYPEICON_CLOSED));
		assertNull(IconFinder.getImagePathFromId("nosuchid", IconFinder.TYPEICON_OPEN));
		assertNull(IconFinder.getImagePathFromId("nosuchid", IconFinder.TYPEICON_LEAF));
	}

	public void testIconSet() {
		String closedPath = IconFinder.getImagePathFromId("org.eclipse.ua.tests.iconSet", IconFinder.TYPEICON_CLOSED);
		String openPath = IconFinder.getImagePathFromId("org.eclipse.ua.tests.iconSet", IconFinder.TYPEICON_OPEN);
		String leafPath = IconFinder.getImagePathFromId("org.eclipse.ua.tests.iconSet", IconFinder.TYPEICON_LEAF);
		String altId = IconFinder.getIconAltFromId("org.eclipse.ua.tests.iconSet");
		assertEquals("altSample", altId);
		assertEquals("org.eclipse.ua.tests/icons/sample.gif", openPath);
		assertEquals("org.eclipse.ua.tests/icons/sample2.gif", closedPath);
		assertEquals("org.eclipse.ua.tests/icons/sample3.gif", leafPath);
	}
	
	public void testSingleIcon() {
		String closedPath = IconFinder.getImagePathFromId("org.eclipse.ua.tests.openOnly", IconFinder.TYPEICON_CLOSED);
		String openPath = IconFinder.getImagePathFromId("org.eclipse.ua.tests.openOnly", IconFinder.TYPEICON_OPEN);
		String leafPath = IconFinder.getImagePathFromId("org.eclipse.ua.tests.openOnly", IconFinder.TYPEICON_LEAF);
		String altId = IconFinder.getIconAltFromId("org.eclipse.ua.tests.openOnly");
		assertNull(altId);
		assertEquals("org.eclipse.ua.tests/icons/sample.gif", openPath);
		assertEquals("org.eclipse.ua.tests/icons/sample.gif", closedPath);
		assertEquals("org.eclipse.ua.tests/icons/sample.gif", leafPath);
	}

	public void testNullIconNotDefined() {
		assertFalse(IconFinder.isIconDefined(null));
	}

	public void testEmptyIconNotDefined() {
		assertFalse(IconFinder.isIconDefined(null));
	}
	
	public void testUnknownIconNotDefined() {
		assertFalse(IconFinder.isIconDefined("nosuchid"));
	}

	public void testKnownIconDefined() {
		assertTrue(IconFinder.isIconDefined("org.eclipse.ua.tests.iconSet"));
	}
	
	public void testOpenOnlyDefined() {
		assertTrue(IconFinder.isIconDefined("org.eclipse.ua.tests.openOnly"));
	}
	
}
