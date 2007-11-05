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

import org.eclipse.help.IHelpResource;
import org.eclipse.help.IUAElement;
import org.eclipse.help.internal.context.Context;
import org.eclipse.ua.tests.help.util.DocumentCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ContextTest extends TestCase {

	private static final String ENABLEMENT_CHEATSHEETS = "<enablement><with variable=\"platform\">" +
    "<test property=\"org.eclipse.core.runtime.isBundleInstalled\" args=\"org.eclipse.ui.cheatsheets\"/>" +
	 "</with></enablement>";
	private static final String TOPIC_END = "</topic>";
	private static final String TOPIC_HEAD_ECLIPSE = "<topic href=\"http://www.eclipse.org\" label=\"enabled\">";
	private static final String CONTEXT_DESCRIPTION = "<description>Context Description</description>";
	private static final String CONTEXT_HEAD = "<context id=\"viewer\" title=\"Sample View\">";
	private final String TOPIC_ECLIPSE = "<topic href=\"http://www.eclipse.org\" label=\"eclipse\"/>";
	//private final String TOPIC_ECLIPSE_WITH_ATTRIBUTE = "<topic href=\"http://www.eclipse.org\" label=\"eclipse\" att=\"abc\"/>";
	private final String TOPIC_BUGZILLA = "<topic href=\"http://www.eclipse.org/bugzilla\" label=\"bugzilla\"/>";
	private final String TOPIC_WITH_ENABLEMENT = TOPIC_HEAD_ECLIPSE + ENABLEMENT_CHEATSHEETS + TOPIC_END;
    private final String END_CONTEXT = "</context>";

	public static Test suite() {
		return new TestSuite(ContextTest.class);
	}

	private Context createContext(final String contextSource) {
		Context context;
		Document doc;
		try {
		    doc = DocumentCreator.createDocument(contextSource);
		} catch (Exception e) {
			fail("Caught Exception");
			doc = null;
		}
		context = new Context((Element) doc.getFirstChild());
		return context;
	}

	public void testSimpleContext() {
		final String contextSource = CONTEXT_HEAD +
		   CONTEXT_DESCRIPTION +
		   TOPIC_ECLIPSE +	
	       END_CONTEXT;
		Context context;
		context = createContext(contextSource);
		assertEquals("Sample View", context.getTitle());
		assertEquals("Context Description", context.getText());
		IHelpResource[] related = context.getRelatedTopics();
		assertEquals(1, related.length);
		assertEquals("eclipse", related[0].getLabel());
	}

	public void testContextWithEnablement() {
		final String contextSource = CONTEXT_HEAD +
		   CONTEXT_DESCRIPTION +
		   TOPIC_WITH_ENABLEMENT +	
	       END_CONTEXT;
		Context context;
		context = createContext(contextSource);
		assertEquals("Sample View", context.getTitle());
		assertEquals("Context Description", context.getText());
		assertEquals("viewer", context.getId());
		IHelpResource[] related = context.getRelatedTopics();
		assertEquals(1, related.length);
		assertEquals("enabled", related[0].getLabel());
		assertTrue(related[0] instanceof IUAElement);
		IUAElement topic = (IUAElement)related[0];
		IUAElement[] topicChildren = topic.getChildren();
		assertEquals(1, topicChildren.length);
	}
	
	public void testCopyContextWithEnablement() {
		final String contextSource = CONTEXT_HEAD +
		   CONTEXT_DESCRIPTION +
		   TOPIC_WITH_ENABLEMENT +	
	       END_CONTEXT;
		Context context  = createContext(contextSource);
		Context context2 = new Context(context, "new id");
		Context context3 = new Context(context2, "new id2");
		assertEquals("Sample View", context.getTitle());
		assertEquals("Sample View", context2.getTitle());
		assertEquals("Context Description", context.getText());
		assertEquals("Context Description", context2.getText());
		assertEquals("viewer", context.getId());
		assertEquals("new id", context2.getId());
		assertEquals("new id2", context3.getId());
		
		IHelpResource[] related = context.getRelatedTopics();
		assertEquals(1, related.length);
		assertEquals("enabled", related[0].getLabel());
		assertTrue(related[0] instanceof IUAElement);
		IUAElement topic = (IUAElement)related[0];
		IUAElement[] topicChildren = topic.getChildren();
		assertEquals(1, topicChildren.length);

		related = context2.getRelatedTopics();
		assertEquals(1, related.length);
		assertEquals("enabled", related[0].getLabel());
		assertTrue(related[0] instanceof IUAElement);
		topic = (IUAElement)related[0];
		topicChildren = topic.getChildren();
		assertEquals(1, topicChildren.length);
		
		related = context3.getRelatedTopics();
		assertEquals(1, related.length);
		assertEquals("enabled", related[0].getLabel());
		assertTrue(related[0] instanceof IUAElement);
		topic = (IUAElement)related[0];
		topicChildren = topic.getChildren();
		assertEquals(1, topicChildren.length);
	}
	
	public void testContextMerge() {
		final String contextSource1 = CONTEXT_HEAD +
		   CONTEXT_DESCRIPTION +
		   TOPIC_ECLIPSE +	
		   TOPIC_WITH_ENABLEMENT +	
	       END_CONTEXT;
		final String contextSource2 = CONTEXT_HEAD +
		   CONTEXT_DESCRIPTION +
		   TOPIC_BUGZILLA +	
	       END_CONTEXT;
		Context context1;
		Context context2;
		context1 = createContext(contextSource1);
		context2 = createContext(contextSource2);
		context1.mergeContext(context2);
		assertEquals("Sample View", context1.getTitle());
		assertEquals("Context Description", context1.getText());
		assertEquals("viewer", context1.getId());
		IHelpResource[] related = context1.getRelatedTopics();
		assertEquals(3, related.length);
		assertEquals("eclipse", related[0].getLabel());
		assertEquals("enabled", related[1].getLabel());
		assertEquals("bugzilla", related[2].getLabel());
		assertTrue(related[0] instanceof IUAElement);
		IUAElement topic = (IUAElement)related[1];
		IUAElement[] topicChildren = topic.getChildren();
		assertEquals(1, topicChildren.length);
	}

	/*
	public void testContextWithAttribute() {
		final String contextSource = CONTEXT_HEAD +
		   CONTEXT_DESCRIPTION +
		   TOPIC_ECLIPSE_WITH_ATTRIBUTE +	
	       END_CONTEXT;
		Context context;
		context = createContext(contextSource);
		assertEquals("abc", context.getAttribute("att"));
	}
	*/
		
}
