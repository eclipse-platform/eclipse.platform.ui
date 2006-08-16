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
package org.eclipse.ua.tests.help.index;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexContribution;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.INode;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.index.IndexFile;
import org.eclipse.help.internal.index.IndexFileParser;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class IndexFileParserTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(IndexFileParserTest.class);
	}

	public void testParse() throws Exception {
		String pluginId = UserAssistanceTestPlugin.getPluginId();
		String file = "data/help/index/parser/index.xml";
		String locale = "en_CA";
		
		IndexFile indexFile = new IndexFile(pluginId, file, locale);
		IndexFileParser parser = new IndexFileParser();
		IIndexContribution contribution = parser.parse(indexFile);
		assertNotNull(contribution);
		assertEquals("/org.eclipse.ua.tests/data/help/index/parser/index.xml", contribution.getId());
		assertEquals(locale, contribution.getLocale());
		
		IIndex index = contribution.getIndex();
		assertNotNull(index);
		assertEquals(2, index.getChildren().length);
		assertEquals(2, index.getEntries().length);
		assertNull(index.getParent());
		
		INode node = index.getChildren()[0];
		assertTrue(node instanceof IIndexEntry);
		IIndexEntry entry = (IIndexEntry)node;
		assertEquals("keyword1", entry.getKeyword());
		assertEquals(2, entry.getChildren().length);
		assertEquals(2, entry.getTopics().length);
		assertEquals(0, entry.getSubentries().length);
		assertEquals(index, entry.getParent());

		node = entry.getChildren()[0];
		assertEquals(node, entry.getTopics()[0]);
		assertTrue(node instanceof ITopic);
		ITopic topic = (ITopic)node;
		assertEquals("label1", topic.getLabel());
		assertEquals("/org.eclipse.ua.tests/topic1.html", topic.getHref());
		assertEquals(0, topic.getChildren().length);
		assertEquals(entry, topic.getParent());
		
		node = entry.getChildren()[1];
		assertEquals(node, entry.getTopics()[1]);
		assertTrue(node instanceof ITopic);
		topic = (ITopic)node;
		assertNull(topic.getLabel());
		assertEquals("/org.eclipse.ua.tests/topic2.html", topic.getHref());
		assertEquals(0, topic.getChildren().length);
		assertEquals(entry, topic.getParent());

		node = index.getChildren()[1];
		assertEquals(node, index.getEntries()[1]);
		assertTrue(node instanceof IIndexEntry);
		entry = (IIndexEntry)node;
		assertEquals("keyword2", entry.getKeyword());
		assertEquals(3, entry.getChildren().length);
		assertEquals(2, entry.getSubentries().length);
		assertEquals(1, entry.getTopics().length);
		assertEquals(index, entry.getParent());

		node = entry.getChildren()[0];
		assertEquals(node, entry.getTopics()[0]);
		assertTrue(node instanceof ITopic);
		topic = (ITopic)node;
		assertNull(topic.getLabel());
		assertEquals("/org.eclipse.ua.tests/topic3.html", topic.getHref());
		assertEquals(0, topic.getChildren().length);
		assertEquals(entry, topic.getParent());
	
		node = entry.getChildren()[1];
		assertEquals(node, entry.getSubentries()[0]);
		assertTrue(node instanceof IIndexEntry);
		IIndexEntry subentry = (IIndexEntry)node;
		assertEquals("keyword3", subentry.getKeyword());
		assertEquals(0, subentry.getChildren().length);
		assertEquals(0, subentry.getSubentries().length);
		assertEquals(0, subentry.getTopics().length);
		assertEquals(entry, subentry.getParent());

		node = entry.getChildren()[2];
		assertEquals(node, entry.getSubentries()[1]);
		assertTrue(node instanceof IIndexEntry);
		subentry = (IIndexEntry)node;
		assertEquals("keyword4", subentry.getKeyword());
		assertEquals(1, subentry.getChildren().length);
		assertEquals(0, subentry.getSubentries().length);
		assertEquals(1, subentry.getTopics().length);
		assertEquals(entry, subentry.getParent());

		node = subentry.getChildren()[0];
		assertEquals(node, subentry.getTopics()[0]);
		assertTrue(node instanceof ITopic);
		topic = (ITopic)node;
		assertNull(topic.getLabel());
		assertEquals("/org.eclipse.ua.tests/topic4.html", topic.getHref());
		assertEquals(0, topic.getChildren().length);
		assertEquals(subentry, topic.getParent());
	}
}
