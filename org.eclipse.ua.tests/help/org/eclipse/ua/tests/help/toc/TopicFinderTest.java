/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.toc;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.webapp.data.TopicFinder;

import junit.framework.TestCase;

public class TopicFinderTest extends TestCase{
	
	private IToc[] getTocs() {
		return HelpPlugin.getTocManager().getTocs("en");
	}
	
	public void testTocsFound() {
		assertTrue(getTocs().length != 0);
	}
	
	public void testNoTocs() {
		TopicFinder finder = new TopicFinder("http:", new IToc[0]);
		assertEquals(-1, finder.getSelectedToc());
		assertNull(finder.getTopicPath());
	}
	
	public void testNoTopic() {
		TopicFinder finder = new TopicFinder(null, getTocs());
		assertEquals(-1, finder.getSelectedToc());
		assertNull(finder.getTopicPath());
	}

	public void testTopicInToc() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/manual/filter.xhtml";
		TopicFinder finder = new TopicFinder(topic, getTocs());
		int selectedToc = finder.getSelectedToc();
		assertFalse(selectedToc == -1);
		ITopic[] path = finder.getTopicPath();
		assertNotNull(path);
		String tocHref = getTocs()[selectedToc].getHref();
		assertEquals("/org.eclipse.ua.tests/data/help/toc/root.xml", tocHref);
		assertEquals(2, path.length);
		assertEquals("manual", path[0].getLabel());
		assertEquals("filter", path[1].getLabel());
	}
	
	public void testTopicInTocWithAnchor() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/manual/filter.xhtml#ABC";
		TopicFinder finder = new TopicFinder(topic, getTocs());
		int selectedToc = finder.getSelectedToc();
		assertFalse(selectedToc == -1);
		ITopic[] path = finder.getTopicPath();
		assertNotNull(path);
		String tocHref = getTocs()[selectedToc].getHref();
		assertEquals("/org.eclipse.ua.tests/data/help/toc/root.xml", tocHref);
		assertEquals(2, path.length);
		assertEquals("manual", path[0].getLabel());
		assertEquals("filter", path[1].getLabel());
	}
	
	public void testTopicNotInToc() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/manual/filter25.xhtml";
		TopicFinder finder = new TopicFinder(topic, getTocs());
		assertEquals(-1, finder.getSelectedToc());
		assertNull(finder.getTopicPath());
	}
	
}
