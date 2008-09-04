/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.toc;

import junit.framework.TestCase;

import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.swt.graphics.Image;

public class TocIconTest extends TestCase {

	public void testNullId() {
		assertNull(HelpUIResources.getImageFromId(null, false, false));
		assertNull(HelpUIResources.getImageFromId(null, true, false));
		assertNull(HelpUIResources.getImageFromId(null, false, true));
	}
	
	public void testBadId() {
		assertNull(HelpUIResources.getImageFromId("nosuchid", false, false));
		assertNull(HelpUIResources.getImageFromId("nosuchid", true, false));
		assertNull(HelpUIResources.getImageFromId("nosuchid", false, true));
	}

	public void testIconSet() {
		Image closedImage = HelpUIResources.getImageFromId("org.eclipse.ua.tests.iconSet", false, false);
		Image openImage = HelpUIResources.getImageFromId("org.eclipse.ua.tests.iconSet", true, false);
		Image leafImage = HelpUIResources.getImageFromId("org.eclipse.ua.tests.iconSet", false, true);
		assertNotNull(openImage);
		assertNotNull(closedImage);
		assertNotNull(leafImage);
		assertFalse(openImage.equals(closedImage));
		assertFalse(openImage.equals(leafImage));
		assertFalse(closedImage.equals(leafImage));
	}
	
	public void testSingleIcon() {
		Image closedImage = HelpUIResources.getImageFromId("org.eclipse.ua.tests.openOnly", false, false);
		Image openImage = HelpUIResources.getImageFromId("org.eclipse.ua.tests.openOnly", true, false);
		Image leafImage = HelpUIResources.getImageFromId("org.eclipse.ua.tests.openOnly", false, true);
		assertNotNull(openImage);
		assertNotNull(closedImage);
		assertNotNull(leafImage);
		assertTrue(openImage.equals(closedImage));
		assertTrue(openImage.equals(leafImage));
		assertTrue(closedImage.equals(leafImage));
	}
	
}
