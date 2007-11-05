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

package org.eclipse.ua.tests.help.other;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.base.HelpEvaluationContext;
import org.eclipse.ua.tests.help.util.DocumentCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TopicTest extends TestCase {

	private static final String INVALID_INSTALLED = "<with variable=\"platform\">" +
	"<test property=\"org.eclipse.core.runtime.isBundleInstalled\" args=\"org.eclipse.ui.invalid\"/></with>";
	private static final String CS_INSTALLED = "<with variable=\"platform\">" +
	    "<test property=\"org.eclipse.core.runtime.isBundleInstalled\" args=\"org.eclipse.ui.cheatsheets\"/></with>";
	private static final String ENABLEMENT_CHEATSHEETS = "<enablement>" + CS_INSTALLED + "</enablement>";
	private static final String ENABLEMENT_INVALID = "<enablement>" +  INVALID_INSTALLED  + "</enablement>";
	private static final String TOPIC_END = "</topic>";
	private static final String TOPIC_HEAD_ECLIPSE = "<topic href=\"http://www.eclipse.org\" label=\"enabled\">";
	private final String TOPIC_ECLIPSE = "<topic href=\"http://www.eclipse.org\" label=\"eclipse\"/>";
	private final String TOPIC_WITH_ENABLEMENT = TOPIC_HEAD_ECLIPSE + ENABLEMENT_CHEATSHEETS + TOPIC_END;
	private final String TOPIC_NOT_ENABLED = TOPIC_HEAD_ECLIPSE + ENABLEMENT_INVALID + TOPIC_END;
	private final String TOPIC_OLD_FILTER = "<topic filter=\"plugin=org.eclipse.ua.tests\" href=\"www.eclipse.org\"" 
	    + " label=\"Transformations and transformation configurations\"/>";
	private final String TOPIC_OLD_FILTER_DISABLED = "<topic filter=\"plugin=org.eclipse.ua.invalid\" href=\"www.eclipse.org\"" 
	    + " label=\"Transformations and transformation configurations\"/>";
	public static Test suite() {
		return new TestSuite(TopicTest.class);
	}

	private Topic createTopic(final String topicSource) {
		Topic topic;
		Document doc;
		try {
		    doc = DocumentCreator.createDocument(topicSource);
		} catch (Exception e) {
			fail("Caught Exception");
			doc = null;
		}
		topic = new Topic((Element) doc.getFirstChild());
		return topic;
	}

	public void testSimpleTopic() {
		Topic topic;
		topic = createTopic(TOPIC_ECLIPSE);
		assertEquals("eclipse", topic.getLabel());
		assertEquals("http://www.eclipse.org", topic.getHref());
	}
	
	public void testCopySimpleTopic() {
		Topic topic1;
		topic1 = createTopic(TOPIC_ECLIPSE);
		Topic topic2 = new Topic(topic1);
		assertEquals("eclipse", topic1.getLabel());
		assertEquals("http://www.eclipse.org", topic1.getHref());
		assertEquals("eclipse", topic2.getLabel());
		assertEquals("http://www.eclipse.org", topic2.getHref());
	}

	public void testEnabledTopic() {
		Topic topic;
		topic = createTopic(TOPIC_WITH_ENABLEMENT);
		assertTrue(topic.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testDisabledTopic() {
		Topic topic;
		topic = createTopic(TOPIC_NOT_ENABLED);
		assertFalse(topic.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testCompoundEnablement() {
		Topic topic;
		topic = createTopic(TOPIC_HEAD_ECLIPSE + "<enablement>"
				+ CS_INSTALLED 
				+ INVALID_INSTALLED 
				+ "</enablement>" + TOPIC_END);
		assertFalse(topic.isEnabled(HelpEvaluationContext.getContext()));
		topic = createTopic(TOPIC_HEAD_ECLIPSE + "<enablement><and>" 
				+ INVALID_INSTALLED 
				+ CS_INSTALLED 
				+ "</and></enablement>" + TOPIC_END);
		assertFalse(topic.isEnabled(HelpEvaluationContext.getContext()));
	}

	public void testOldStyleEnablement() {
		Topic topic;
		topic = createTopic(TOPIC_OLD_FILTER);
		assertTrue(topic.isEnabled(HelpEvaluationContext.getContext()));
	}
	
	public void testOldStyleDisabled() {
		Topic topic;
		topic = createTopic(TOPIC_OLD_FILTER_DISABLED);
		assertFalse(topic.isEnabled(HelpEvaluationContext.getContext()));
	}
		
}
