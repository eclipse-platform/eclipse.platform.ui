/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.junit.Test;

/**
 * Test for functions which decode a topic string
 */

public class TopicPathTest {
	@Test
	public void testTocOnly() {
		int[] topics = UrlUtil.splitPath("25");
		assertEquals(1, topics.length);
		assertEquals(25, topics[0]);
	}

	@Test
	public void testTopic() {
		int[] topics = UrlUtil.splitPath("2_5");
		assertEquals(2, topics.length);
		assertEquals(2, topics[0]);
		assertEquals(5, topics[1]);
	}

	@Test
	public void testNullPath() {
		int[] topics = UrlUtil.splitPath(null);
		assertNull(topics);
	}

	@Test
	public void testEmptyPath() {
		int[] topics = UrlUtil.splitPath("");
		assertNull(topics);
	}

	@Test
	public void testDoubleUnderscore() {
		int[] topics = UrlUtil.splitPath("1__2");
		assertEquals(2, topics.length);
		assertEquals(1, topics[0]);
		assertEquals(2, topics[1]);
	}

	@Test
	public void testMalformedPath() {
		int[] topics = UrlUtil.splitPath("3_A");
		assertNull(topics);
	}

}
