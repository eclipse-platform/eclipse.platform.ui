/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.toc;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.webapp.data.EnabledTopicUtils;

public class EnabledTopicTest extends TestCase {
	
	private class ETopic implements ITopic {
		
		private String label;
		private boolean isEnabled;
		private List<ITopic> children = new ArrayList<ITopic>();

		public ETopic(String label, boolean isEnabled) {
			this.label = label; 
			this.isEnabled = isEnabled;
		}

		public ITopic[] getSubtopics() {
			return children.toArray(new ITopic[children.size()]);
		}

		public IUAElement[] getChildren() {

			return getSubtopics();
		}

		public boolean isEnabled(IEvaluationContext context) {
			return isEnabled;
		}

		public String getHref() {
			return "http://www.eclipse.org";
		}

		public String getLabel() {
			return label;
		}	
		
		public void addSubTopic(ITopic subTopic) {
			children.add(subTopic);
		}
	}
	
	private class NoHrefTopic extends ETopic {
		
		public NoHrefTopic(String label) {
			super(label, true);
		}
		
		public String getHref() {
			return null;
		}
		
	}
	
	private class EIndexEntry extends UAElement implements IIndexEntry  {
		
		private String keyword;
		private List<ITopic> topics = new ArrayList<ITopic>();
		private List<IIndexEntry> subEntries = new ArrayList<IIndexEntry>();

		public EIndexEntry(String keyword) {
			super(keyword);
			this.keyword = keyword;
		}

		public String getKeyword() {
			return keyword;
		}

		public void addSubEntry(IIndexEntry entry) {
			subEntries.add(entry);
		}
		
		public void addTopic(ITopic topic) {
			topics.add(topic);
		}

		public IIndexEntry[] getSubentries() {
			return subEntries.toArray(new IIndexEntry[subEntries.size()]);
		}

		public ITopic[] getTopics() {
			return topics.toArray(new ITopic[topics.size()]);
		}

		public synchronized IUAElement[] getChildren() {
			List<IUAElement> all = new ArrayList<IUAElement>();
			all.addAll(subEntries);
			all.addAll(topics);
			return all.toArray(new IUAElement[all.size()]);
		}	
	}
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(EnabledTopicTest.class);
	}
	
	public void testEnabledTopic() {
		assertTrue(EnabledTopicUtils.isEnabled(new ETopic("T1", true)));
		assertFalse(EnabledTopicUtils.isEnabled(new ETopic("T2", false)));
	}

	public void testEnabledTopicsEmptyArray() throws Exception {
         ITopic[] enabled = EnabledTopicUtils.getEnabled(new ITopic[0]);
         assertTrue(enabled.length == 0);
	}
	
	public void testEnabledTopicsAllEnabled() throws Exception {
        ITopic[] topics = new ITopic[2];
        topics[0] = new ETopic("T1", true);
        topics[1] = new ETopic("T2", true);
		ITopic[] enabled = EnabledTopicUtils.getEnabled(topics);
        assertTrue(enabled.length == 2);
        assertTrue(topics[0].getLabel().equals("T1"));
        assertTrue(topics[1].getLabel().equals("T2"));
	}

	public void testEnabledTopicsAllDisabled() throws Exception { 
		ITopic[] topics = new ITopic[2];
	    topics[0] = new ETopic("T1", false);
	    topics[1] = new ETopic("T2", false);
		ITopic[] enabled = EnabledTopicUtils.getEnabled(topics);
	    assertTrue(enabled.length == 0);
	}
	
	public void testEnabledTopicsMix() throws Exception {
		ITopic[] topics = new ITopic[4];
        topics[0] = new ETopic("T1", true);
        topics[1] = new ETopic("T2", false);
        topics[2] = new ETopic("T3", true);
        topics[3] = new ETopic("T4", false);
		ITopic[] enabled = EnabledTopicUtils.getEnabled(topics);
        assertEquals(2, enabled.length);
        assertEquals("T1", enabled[0].getLabel());
        assertEquals("T3", enabled[1].getLabel());
	}

	public void testNoHref() {
		ITopic noHref = new NoHrefTopic("N1");
		assertFalse(EnabledTopicUtils.isEnabled(noHref));
	}

	public void testNoHrefValidChild() {
		ETopic noHref = new NoHrefTopic("N1");
		noHref.addSubTopic(new ETopic("T1", true));
		assertTrue(EnabledTopicUtils.isEnabled(noHref));
	}

	public void testNoHrefInvalidChild() {
		ETopic noHref = new NoHrefTopic("N1");
		noHref.addSubTopic(new ETopic("T1", false));
		assertFalse(EnabledTopicUtils.isEnabled(noHref));
	}
	
	public void testNoHrefMixedChildren() {
		ETopic noHref = new NoHrefTopic("N1");
		noHref.addSubTopic(new ETopic("T1", false));
		noHref.addSubTopic(new ETopic("T2", true));
		assertTrue(EnabledTopicUtils.isEnabled(noHref));
	}

	public void testNoHrefValidGrandchild() {
		ETopic noHref = new NoHrefTopic("N1");
		ETopic subTopic = new NoHrefTopic("N2");
		noHref.addSubTopic(subTopic);
		subTopic.addSubTopic(new ETopic("T2", true));
		assertTrue(EnabledTopicUtils.isEnabled(noHref));
	}
	
	public void testNoHrefInvalidGrandchild() {
		ETopic noHref = new NoHrefTopic("N1");
		ETopic subTopic = new NoHrefTopic("N2");
		noHref.addSubTopic(subTopic);
		subTopic.addSubTopic(new ETopic("T2", false));
		assertFalse(EnabledTopicUtils.isEnabled(noHref));
	}

	public void testEmptyIndexEntry() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		assertFalse(EnabledTopicUtils.isEnabled(entry1));
	}
	
	public void testEnabledIndexEntry() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		entry1.addTopic(new ETopic("T1", true));
		assertTrue(EnabledTopicUtils.isEnabled(entry1));
	}
	
	public void testDisabledIndexEntry() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		entry1.addTopic(new ETopic("T1", false));
		assertFalse(EnabledTopicUtils.isEnabled(entry1));
	}

	public void testMixedIndexEntry() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		entry1.addTopic(new ETopic("T1", true));
		entry1.addTopic(new ETopic("T2", false));
		assertTrue(EnabledTopicUtils.isEnabled(entry1));
	}

	public void testIndexEntryEnabledChild() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		EIndexEntry entry2 = new EIndexEntry("def");
		entry2.addTopic(new ETopic("T1", true));
		entry1.addSubEntry(entry2);
		assertTrue(EnabledTopicUtils.isEnabled(entry1));
	}
	
	public void testIndexEntryEnabledGrandChild() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		EIndexEntry entry2 = new EIndexEntry("def");
		EIndexEntry entry3 = new EIndexEntry("ghi");
		entry1.addSubEntry(entry2);
		entry2.addSubEntry(entry3);
		entry3.addTopic(new ETopic("T1", true));
		assertTrue(EnabledTopicUtils.isEnabled(entry1));
	}

	public void testIndexEntryDisabledChild() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		EIndexEntry entry2 = new EIndexEntry("def");
		entry2.addTopic(new ETopic("T1", false));
		entry1.addSubEntry(entry2);
		assertFalse(EnabledTopicUtils.isEnabled(entry1));
	}
	
	public void testIndexEntryMixedChildren() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		EIndexEntry entry2 = new EIndexEntry("def");
		EIndexEntry entry3 = new EIndexEntry("ghi");
		entry2.addTopic(new ETopic("T1", false));
		entry3.addTopic(new ETopic("T2", true));
		entry1.addSubEntry(entry2);
		entry1.addSubEntry(entry3);
		assertTrue(EnabledTopicUtils.isEnabled(entry1));
	}

	public void testEnabledIndexArrayEmpty() {
		IIndexEntry[] entries = new EIndexEntry[0];
		IIndexEntry[] filtered =EnabledTopicUtils.getEnabled(entries);
		assertEquals(0, filtered.length);
	}

	public void testEnabledIndexArrayDisabled() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		EIndexEntry entry2 = new EIndexEntry("def");
		IIndexEntry[] entries = new EIndexEntry[]{entry1, entry2};
		IIndexEntry[] filtered =EnabledTopicUtils.getEnabled(entries);
		assertEquals(0, filtered.length);
	}

	public void testEnabledIndexArrayEnabled() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		EIndexEntry entry2 = new EIndexEntry("def");
		entry1.addTopic(new ETopic("T1", true));
		entry2.addTopic(new ETopic("T2", true));
		IIndexEntry[] entries = new EIndexEntry[]{entry1, entry2};
		IIndexEntry[] filtered =EnabledTopicUtils.getEnabled(entries);
		assertEquals(2, filtered.length);
		assertEquals(filtered[0].getKeyword(), "abc");
		assertEquals(filtered[1].getKeyword(), "def");
	}
	
	public void testEnabledIndexArrayMixed() {
		EIndexEntry entry1 = new EIndexEntry("abc");
		EIndexEntry entry2 = new EIndexEntry("def");
		EIndexEntry entry3 = new EIndexEntry("ghi");
		EIndexEntry entry4 = new EIndexEntry("jkl");
		entry2.addTopic(new ETopic("T1", true));
		entry4.addTopic(new ETopic("T2", true));
		IIndexEntry[] entries = new EIndexEntry[]{entry1, entry2, entry3, entry4};
		IIndexEntry[] filtered =EnabledTopicUtils.getEnabled(entries);
		assertEquals(2, filtered.length);
		assertEquals(filtered[0].getKeyword(), "def");
		assertEquals(filtered[1].getKeyword(), "jkl");
	}
	
}
