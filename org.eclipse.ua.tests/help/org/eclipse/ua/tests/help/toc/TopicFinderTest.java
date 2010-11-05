/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
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
import org.eclipse.help.internal.base.scope.FilterScope;
import org.eclipse.help.internal.base.scope.UniversalScope;
import org.eclipse.help.internal.webapp.data.TocData;
import org.eclipse.help.internal.webapp.data.TopicFinder;
import org.eclipse.help.internal.webapp.data.UrlUtil;

import junit.framework.TestCase;

public class TopicFinderTest extends TestCase{
	
	private IToc[] getTocs() {
		return HelpPlugin.getTocManager().getTocs("en");
	}
	
	public void testTocsFound() {
		assertTrue(getTocs().length != 0);
	}
	
	public void testNoTocs() {
		TopicFinder finder = new TopicFinder("http:", new IToc[0], new UniversalScope());
		assertEquals(-1, finder.getSelectedToc());
		assertNull(finder.getTopicPath());
	}
	
	public void testNoTopic() {
		TopicFinder finder = new TopicFinder(null, getTocs(), new UniversalScope());
		assertEquals(-1, finder.getSelectedToc());
		assertNull(finder.getTopicPath());
	}

	public void testTopicInToc() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/manual/filter.xhtml";
		TopicFinder finder = new TopicFinder(topic, getTocs(), new UniversalScope());
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
		TopicFinder finder = new TopicFinder(topic, getTocs(), new UniversalScope());
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
	
	public void testTopicInFilteredToc() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/toc/filteredToc/helpInstalled.html";
		TopicFinder finder = new TopicFinder(topic, getTocs(), new FilterScope());
		int selectedToc = finder.getSelectedToc();
		assertFalse(selectedToc == -1);
		ITopic[] path = finder.getTopicPath();
		assertNotNull(path);
		String tocHref = getTocs()[selectedToc].getHref();
		assertEquals("/org.eclipse.ua.tests/data/help/toc/root.xml", tocHref);
		assertEquals(2, path.length);
		assertEquals("filter", path[0].getLabel());
		assertEquals("The plugin org.eclipse.help is installed", path[1].getLabel());
		assertEquals("/org.eclipse.ua.tests/data/help/toc/filteredToc/helpInstalled.html", path[1].getHref());
	}
	
	public void testTopicNotInToc() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/manual/filter25.xhtml";
		TopicFinder finder = new TopicFinder(topic, getTocs(), new UniversalScope());
		assertEquals(-1, finder.getSelectedToc());
		assertNull(finder.getTopicPath());
	}
	
	public void testLookupFromPath() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/toc/filteredToc/helpInstalled.html";
		IToc[] tocs = getTocs();
		TopicFinder finder = new TopicFinder(topic, tocs, new FilterScope());
		String numericPath = finder.getNumericPath();
		int tocIndex = finder.getSelectedToc();
		String fullPath = "" + tocIndex + "_" + numericPath;
		int[] intPath = UrlUtil.splitPath(fullPath);
		ITopic[] topics = TocData.decodePath(intPath, tocs[tocIndex], new FilterScope());
		assertEquals(2, topics.length);
		assertEquals("filter", topics[0].getLabel());
		assertEquals("The plugin org.eclipse.help is installed", topics[1].getLabel());
		assertEquals("/org.eclipse.ua.tests/data/help/toc/filteredToc/helpInstalled.html", topics[1].getHref());
	}

	public void testTocNavURL() {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/toc/filteredToc/helpInstalled.html";
		IToc[] tocs = getTocs();
		TopicFinder finder = new TopicFinder(topic, tocs, new UniversalScope());
		int selectedToc = finder.getSelectedToc();
		String navPath = "http://127.0.0.1:1936/help/nav/" + selectedToc;
		TopicFinder finder2 = new TopicFinder(navPath, tocs, new UniversalScope());
		assertEquals(selectedToc, finder2.getSelectedToc());
		assertEquals(0, finder2.getTopicPath().length);
	}

	public void testTopic_0_0NavURL() {
		checkNavTopic(0, 0);
	}

	public void testTopic_0_1NavURL() {
		checkNavTopic(0, 1);
	}
	
	public void testTopic_1_0NavURL() {
		checkNavTopic(1, 0);
	}

	private void checkNavTopic(int index1, int index2) {
		String topic = "http://localhost:8082/help/topic/org.eclipse.ua.tests/data/help/toc/filteredToc/helpInstalled.html";
		IToc[] tocs = getTocs();
		TopicFinder finder = new TopicFinder(topic, tocs, new UniversalScope());
		int selectedToc = finder.getSelectedToc();
		String navPath = "http://127.0.0.1:1936/help/nav/" + selectedToc + 
           '_' + index1 + '_' + index2;
		TopicFinder finder2 = new TopicFinder(navPath, tocs, new UniversalScope());
		assertEquals(selectedToc, finder2.getSelectedToc());
		ITopic[] topicPath = finder2.getTopicPath();
		assertEquals(2, topicPath.length);
		ITopic[] topLevelTopics = tocs[selectedToc].getTopics();
		assertEquals(topLevelTopics[index1], topicPath[0]);
		ITopic[] secondLevelTopics = topLevelTopics[index1].getSubtopics();
		assertEquals(secondLevelTopics[index2], topicPath[1]);
		assertEquals("" + index1 + '_' + index2, finder2.getNumericPath());
	}
	
}
