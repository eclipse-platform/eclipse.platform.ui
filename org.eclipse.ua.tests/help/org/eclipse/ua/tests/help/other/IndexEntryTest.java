/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpEvaluationContext;
import org.eclipse.help.internal.index.IndexEntry;
import org.eclipse.ua.tests.help.util.DocumentCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IndexEntryTest extends TestCase {

	private static final String ECLIPSE = "eclipse";
	private static final String BUGZILLA = "bugzilla";
	private static final String BUGZILLA_HREF = "https://bugs.eclipse.org/bugs/";	
	private static final String INVALID_INSTALLED = "<with variable=\"platform\">" +
	"<test property=\"org.eclipse.core.runtime.isBundleInstalled\" args=\"org.eclipse.ui.invalid\"/></with>";
	private static final String CS_INSTALLED = "<with variable=\"platform\">" +
	    "<test property=\"org.eclipse.core.runtime.isBundleInstalled\" args=\"org.eclipse.ui.cheatsheets\"/></with>";
	private static final String ENABLEMENT_CHEATSHEETS = "<enablement>" + CS_INSTALLED + "</enablement>";
	private static final String ENABLEMENT_INVALID = "<enablement>" +  INVALID_INSTALLED  + "</enablement>";
	private static final String FILTER_IN = "<filter name = \"plugin\" value = \"org.eclipse.ua.tests\"/>";
	private static final String FILTER_OUT = "<filter name = \"plugin\" value = \"org.eclipse.ua.invalid\"/>";
	private static final String NEGATED_FILTER_IN = "<filter name = \"plugin\" value = \"!org.eclipse.ua.tests\"/>";
	private static final String NEGATED_FILTER_OUT = "<filter name = \"plugin\" value = \"!org.eclipse.ua.invalid\"/>";
	private static final String ENTRY_END = "</entry>";
	private static final String ENTRY_HEAD_ECLIPSE = "<entry keyword=\"eclipse\">";
	private final String ENTRY_ECLIPSE = "<entry keyword=\"eclipse\"/>";
	private final String ENTRY_BUGZILLA = "<entry keyword=\"bugzilla\"/>";
	private final String TOPIC_BUGZILLA = "<topic href=\"https://bugs.eclipse.org/bugs/\" label=\"bugzilla\"/>";
	
	private final String ENTRY_WITH_ENABLEMENT = ENTRY_HEAD_ECLIPSE + ENABLEMENT_CHEATSHEETS + ENTRY_END;
	private final String ENTRY_NOT_ENABLED = ENTRY_HEAD_ECLIPSE + ENABLEMENT_INVALID + ENTRY_END;
	private final String ENTRY_FILTER_IN = ENTRY_HEAD_ECLIPSE + FILTER_IN + ENTRY_END;
	private final String ENTRY_FILTER_OUT = ENTRY_HEAD_ECLIPSE + FILTER_OUT + ENTRY_END;
	private final String ENTRY_FILTER_MIXED = ENTRY_HEAD_ECLIPSE + FILTER_IN + FILTER_OUT + ENTRY_END;
	private final String ENTRY_OLD_FILTER = "<entry filter=\"plugin=org.eclipse.ua.tests\" " 
	    + " keyword=\"Transformations and transformation configurations\"/>";
	private final String ENTRY_OLD_FILTER_DISABLED = "<entry filter=\"plugin=org.eclipse.ua.invalid\" " 
	    + " keyword=\"Transformations and transformation configurations\"/>";
	private final String ENTRY_OLD_FILTER_IN__NEGATED = "<entry filter=\"plugin!=org.eclipse.ua.tests\" " 
	    + " keyword=\"Transformations and transformation configurations\"/>";
	private final String ENTRY_OLD_FILTER_OUT_NEGATED = "<entry filter=\"plugin!=org.eclipse.ua.invalid\" " 
	    + " keyword=\"Transformations and transformation configurations\"/>";
	private final String ENTRY_WITH_CHILD = ENTRY_HEAD_ECLIPSE + ENTRY_BUGZILLA + ENTRY_END;
	private final String ENTRY_WITH_TOPIC = ENTRY_HEAD_ECLIPSE + TOPIC_BUGZILLA + ENTRY_END;
	
	public static Test suite() {
		return new TestSuite(IndexEntryTest.class);
	}
	
	protected void setUp() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_WORKBENCH);
	}

	private IndexEntry createEntry(final String elementSource) {
		IndexEntry element;
		Document doc;
		try {
		    doc = DocumentCreator.createDocument(elementSource);
		} catch (Exception e) {
			fail("Caught Exception");
			doc = null;
		}
		element = new IndexEntry((Element) doc.getFirstChild());
		return element;
	}

	public void testSimpleIndexEntry() {
		IndexEntry entry;
		entry = createEntry(ENTRY_ECLIPSE);
		assertEquals(ECLIPSE, entry.getKeyword());
		assertEquals(0, entry.getTopics().length);
		assertEquals(0, entry.getSubentries().length);
		//assertEquals(0, entry.getSees().length);
	}

	public void testCopySimpleIndexEntry() {
		IndexEntry entry1;
		entry1 = createEntry(ENTRY_ECLIPSE);
		IndexEntry entry2 = new IndexEntry(entry1);
		assertEquals(ECLIPSE, entry1.getKeyword());
		assertEquals(ECLIPSE, entry2.getKeyword());
	}

	public void testCopyIndexEntryWithChild() {
		IndexEntry entry1;
		entry1 = createEntry(ENTRY_WITH_CHILD);
		IndexEntry entry2 = new IndexEntry(entry1);

		assertEquals(1, entry1.getSubentries().length);
		IndexEntry child1 = (IndexEntry)entry1.getSubentries()[0];
		assertEquals(BUGZILLA, child1.getKeyword());
		
		assertEquals(1, entry2.getSubentries().length);
		IndexEntry child2 = (IndexEntry)entry2.getSubentries()[0];
		assertEquals(BUGZILLA, child2.getKeyword());
		assertEquals(1, entry2.getSubentries().length);
	}
	
	public void testCopyIndexEntryWithTopic() {
		IndexEntry entry1;
		entry1 = createEntry(ENTRY_WITH_TOPIC);
		IndexEntry entry2 = new IndexEntry(entry1);

		assertEquals(0, entry1.getSubentries().length);
		assertEquals(1, entry1.getTopics().length);
		Topic child1 = (Topic)entry1.getTopics()[0];
		assertEquals(BUGZILLA, child1.getLabel());
		assertEquals(BUGZILLA_HREF, child1.getHref());

		assertEquals(0, entry2.getSubentries().length);
		assertEquals(1, entry2.getTopics().length);
		Topic child2 = (Topic)entry2.getTopics()[0];
		assertEquals(BUGZILLA, child2.getLabel());
		assertEquals(BUGZILLA_HREF, child2.getHref());
	}

	public void testEnabledIndexEntry() {
		IndexEntry entry;
		entry = createEntry(ENTRY_WITH_ENABLEMENT);
		assertTrue(entry.isEnabled(HelpEvaluationContext.getContext()));
	}

	public void testDisabledIndexEntry() {
		IndexEntry entry;
		entry = createEntry(ENTRY_NOT_ENABLED);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testCopyDisabledIndexEntry() {
		IndexEntry entry1;
		entry1 = createEntry(ENTRY_NOT_ENABLED);
		IndexEntry entry2 = new IndexEntry(entry1);
		IndexEntry entry3 = new IndexEntry(entry2);
		assertFalse(entry1.isEnabled(HelpEvaluationContext.getContext()));
		assertFalse(entry2.isEnabled(HelpEvaluationContext.getContext()));
		assertFalse(entry3.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testCompoundEnablement() {
		IndexEntry entry;
		entry = createEntry(ENTRY_HEAD_ECLIPSE + "<enablement>"
				+ CS_INSTALLED 
				+ INVALID_INSTALLED 
				+ "</enablement>" + ENTRY_END);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
		entry = createEntry(ENTRY_HEAD_ECLIPSE + "<enablement><and>" 
				+ INVALID_INSTALLED 
				+ CS_INSTALLED 
				+ "</and></enablement>" + ENTRY_END);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
	}

	public void testOldStyleEnablement() {
		IndexEntry entry;
		entry = createEntry(ENTRY_OLD_FILTER);
		assertTrue(entry.isEnabled(HelpEvaluationContext.getContext()));
	}

	public void testOldStyleDisabled() {
		IndexEntry entry;
		entry = createEntry(ENTRY_OLD_FILTER_DISABLED);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testOldStyleNegated() {
		IndexEntry entry;
		entry = createEntry(ENTRY_OLD_FILTER_IN__NEGATED);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
		entry = createEntry(ENTRY_OLD_FILTER_OUT_NEGATED);
		assertTrue(entry.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testCopyOldStyleDisabled() {
		IndexEntry entry1;
		entry1 = createEntry(ENTRY_OLD_FILTER_DISABLED);
		IndexEntry entry2 = new IndexEntry(entry1);
		IndexEntry entry3 = new IndexEntry(entry2);
		assertFalse(entry1.isEnabled(HelpEvaluationContext.getContext()));
		assertFalse(entry2.isEnabled(HelpEvaluationContext.getContext()));
		assertFalse(entry3.isEnabled(HelpEvaluationContext.getContext()));
	}

	public void testFilterIn() {
		IndexEntry entry;
		entry = createEntry(ENTRY_FILTER_IN);
		assertTrue(entry.isEnabled(HelpEvaluationContext.getContext()));
	}

	public void testFilterOut() {
		IndexEntry entry;
		entry = createEntry(ENTRY_FILTER_OUT);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testFilterMixed() {
		IndexEntry entry;
		entry = createEntry(ENTRY_FILTER_MIXED);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testNegatedFilters() {
		IndexEntry entry;
		entry = createEntry(ENTRY_HEAD_ECLIPSE + NEGATED_FILTER_IN + ENTRY_END);
		assertFalse(entry.isEnabled(HelpEvaluationContext.getContext()));
		entry = createEntry(ENTRY_HEAD_ECLIPSE + NEGATED_FILTER_OUT + ENTRY_END);
		assertTrue(entry.isEnabled(HelpEvaluationContext.getContext()));
	}

	public void testCopyFilterOut() {
		IndexEntry entry1;
		entry1 = createEntry(ENTRY_FILTER_OUT);
		IndexEntry entry2 = new IndexEntry(entry1);
		IndexEntry entry3 = new IndexEntry(entry2);
		assertFalse(entry1.isEnabled(HelpEvaluationContext.getContext()));
		assertFalse(entry2.isEnabled(HelpEvaluationContext.getContext()));
		assertFalse(entry3.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	/*
	 * Disabled, see Bug 210024 [Help] IndexEntry element problems constructing from an IIndexEntry
	public void testUserTopicWithFilteredChildren() {
		UserTopic u1 = new UserTopic(ECLIPSE, ECLIPSE_HREF, true);
		UserTopic u2 = new UserTopic(BUGZILLA, BUGZILLA_HREF, false);
		u1.addTopic(u2);
		Topic t1 = new Topic(u1);
		assertEquals(ECLIPSE, t1.getLabel());
		assertEquals(ECLIPSE_HREF, t1.getHref());
		assertTrue(t1.isEnabled(HelpEvaluationContext.getContext()));
		assertEquals(1, t1.getChildren().length);
		ITopic t2 = t1.getSubentrys()[0];
		assertEquals(BUGZILLA, t2.getLabel());
		assertEquals(BUGZILLA_HREF, t2.getHref());
		assertFalse(t2.isEnabled(HelpEvaluationContext.getContext()));
	}
	*/
	
	/*
	public void testCopyUserTopicWithChildren() {
		UserTopic u1 = new UserTopic(ECLIPSE, ECLIPSE_HREF, true);
		UserTopic u2 = new UserTopic(BUGZILLA, BUGZILLA_HREF, true);
		u1.addTopic(u2);
		Topic t1 = new Topic(u1);
		Topic t2 = new Topic(t1);

		assertEquals(ECLIPSE, t1.getLabel());
		assertEquals(ECLIPSE_HREF, t1.getHref());
		assertTrue(t1.isEnabled(HelpEvaluationContext.getContext()));
		assertEquals(1, t1.getChildren().length);
		ITopic t1s = t1.getSubentrys()[0];
		assertEquals(BUGZILLA, t1s.getLabel());
		assertEquals(BUGZILLA_HREF, t1s.getHref());
		
		assertEquals(ECLIPSE, t2.getLabel());
		assertEquals(ECLIPSE_HREF, t2.getHref());
		assertTrue(t2.isEnabled(HelpEvaluationContext.getContext()));
		assertEquals(1, t2.getChildren().length);
		ITopic t2s = t2.getSubentrys()[0];
		assertEquals(BUGZILLA, t2s.getLabel());
		assertEquals(BUGZILLA_HREF, t2s.getHref());
	}
	*/
		
}
