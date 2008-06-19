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

package org.eclipse.ua.tests.help.webapp;

import org.eclipse.help.internal.webapp.data.UrlUtil;

import junit.framework.TestCase;

/**
 * Test for functions which decode a topic string
 */

public class UrlUtilsTests extends TestCase {

	public void testHelpNav() {
		assertTrue(UrlUtil.isNavPath("/help/nav/1_2"));
	}

	public void testOtherNav() {
		assertTrue(UrlUtil.isNavPath("/other/nav/1_2"));
	}

	public void testHelpHelpNav() {
		assertFalse(UrlUtil.isNavPath("/help/help/nav/1_2"));
	}

	public void testNoNav() {
		assertFalse(UrlUtil.isNavPath("/helpcontext"));
	}
	
	public void testNoSlash() {
		assertFalse(UrlUtil.isNavPath("help/nav/1_2"));
	}
	
	public void testNavTopicPath() {
		int[] path = UrlUtil.getTopicPath("/nav/23_4_5", "en_us");
		assertEquals(3, path.length);
		assertEquals(23, path[0]);
		assertEquals(4, path[1]);
		assertEquals(5, path[2]);
    }
	
}
