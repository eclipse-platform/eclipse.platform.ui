/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 * Test forfunctions which decode a topic string
 */

public class TopicPathTest extends TestCase {

	public void testTocOnly() {
		int[] topics = UrlUtil.splitPath("25");
		assertEquals(1, topics.length);
		assertEquals(25, topics[0]);
	}
	
	public void testTopic() {
		int[] topics = UrlUtil.splitPath("2_5");
		assertEquals(2, topics.length);
		assertEquals(2, topics[0]);
		assertEquals(5, topics[1]);
	}

	public void testNullPath() {
		int[] topics = UrlUtil.splitPath(null);
		assertNull(topics);
	}

	public void testEmptyPath() {
		int[] topics = UrlUtil.splitPath("");
		assertNull(topics);
	}

	public void testDoubleUnderscore() {
		int[] topics = UrlUtil.splitPath("1__2");
		assertEquals(2, topics.length);
		assertEquals(1, topics[0]);
		assertEquals(2, topics[1]);
	}
	
	public void testMalformedPath() {
		int[] topics = UrlUtil.splitPath("3_A");
		assertNull(topics);
	}
	
}
