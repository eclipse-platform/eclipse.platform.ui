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
package org.eclipse.ua.tests.help.toc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.IAnchor;
import org.eclipse.help.INode;
import org.eclipse.help.IFilter;
import org.eclipse.help.IInclude;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.toc.TocFile;
import org.eclipse.help.internal.toc.TocFileParser;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class TocFileParserTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(TocFileParserTest.class);
	}

	public void testParse() throws Exception {
		String pluginId = UserAssistanceTestPlugin.getPluginId();
		String file = "data/help/toc/parser/toc.xml";
		boolean isPrimary = false;
		String locale = "en_CA";
		String extraDir = "data/help/toc/parser/extraDir";
		String category = null;
		
		TocFile tocFile = new TocFile(pluginId, file, isPrimary, locale, extraDir, category);
		TocFileParser parser = new TocFileParser();
		ITocContribution contribution = parser.parse(tocFile);
		assertNotNull(contribution);
		assertEquals("/org.eclipse.ua.tests/data/help/toc/parser/toc.xml", contribution.getId());
		assertEquals(isPrimary, contribution.isPrimary());
		assertEquals(locale, contribution.getLocale());
		
		String[] extraDocs = contribution.getExtraDocuments();
		Set set = new HashSet(Arrays.asList(extraDocs));
		assertTrue(set.contains("/org.eclipse.ua.tests/data/help/toc/parser/extraDir/page1.html"));
		assertTrue(set.contains("/org.eclipse.ua.tests/data/help/toc/parser/extraDir/page2.html"));
		assertTrue(set.contains("/org.eclipse.ua.tests/data/help/toc/parser/extraDir/otherDir/page3.html"));
		
		assertEquals(category, contribution.getCategoryId());
		assertEquals("/org.eclipse.ua.tests/myOtherToc.xml", contribution.getLinkTo());

		IToc toc = contribution.getToc();
		assertNotNull(toc);
		
		assertEquals("myToc", toc.getLabel());
		assertEquals("myToc", toc.getTopic(null).getLabel());
		assertEquals("/org.eclipse.ua.tests/myFakeTopic.html", toc.getTopic(null).getHref());
		assertEquals(3, toc.getChildren().length);
		assertEquals(1, toc.getTopics().length);
		assertNotNull(toc.getTopic(null));
		assertNull(toc.getParent());
		
		INode node = toc.getChildren()[0];
		assertTrue(node instanceof IAnchor);
		IAnchor anchor = (IAnchor)node;
		assertEquals("myAnchor", anchor.getId());
		assertEquals(0, anchor.getChildren().length);
		assertEquals(toc, anchor.getParent());

		node = toc.getChildren()[1];
		assertEquals(node, toc.getTopics()[0]);
		assertTrue(node instanceof ITopic);
		ITopic topic = (ITopic)node;
		assertEquals("myTopic", topic.getLabel());
		assertEquals("/org.eclipse.ua.tests/myTopic.html", topic.getHref());
		assertEquals(1, topic.getChildren().length);
		assertEquals(toc, topic.getParent());
		
		node = topic.getChildren()[0];
		assertTrue(node instanceof IFilter);
		IFilter filter = (IFilter)node;
		assertEquals("os=win32", filter.getExpression());
		assertEquals(1, filter.getChildren().length);
		assertEquals(topic, filter.getParent());
		
		node = filter.getChildren()[0];
		assertTrue(node instanceof ITopic);
		topic = (ITopic)node;
		assertEquals("mySubtopic", topic.getLabel());
		assertEquals("/org.eclipse.ua.tests/mySubtopic.html", topic.getHref());
		assertEquals(0, topic.getChildren().length);
		assertEquals(filter, topic.getParent());
		
		node = toc.getChildren()[2];
		assertTrue(node instanceof IFilter);
		filter = (IFilter)node;
		assertEquals("ws!=gtk", filter.getExpression());
		assertEquals(1, filter.getChildren().length);
		assertEquals(toc, filter.getParent());

		node = filter.getChildren()[0];
		assertTrue(node instanceof IInclude);
		IInclude include = (IInclude)node;
		assertEquals("/org.eclipse.ua.tests/myOtherToc2.xml", include.getTarget());
		assertEquals(0, include.getChildren().length);
		assertEquals(filter, include.getParent());
	}
}
