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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.toc.TocAssembler;
import org.eclipse.help.internal.toc.TocFile;
import org.eclipse.help.internal.toc.TocFileParser;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class TocAssemblerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(TocAssemblerTest.class);
	}

	public void testAssemble() throws Exception {
		TocFileParser parser = new TocFileParser();
		ITocContribution b = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/b.xml", true, "en", null, null));
		ITocContribution c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/c.xml", true, "en", null, null));
		ITocContribution result_b_c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/result_b_c.xml", true, "en", null, null));
		
		TocAssembler assembler = new TocAssembler();
		List contributions = new ArrayList(Arrays.asList(new Object[] { b, c }));
		contributions = assembler.assemble(contributions);
		assertEquals(1, contributions.size());
		String expected = serialize(result_b_c);
		String actual = serialize((ITocContribution)contributions.get(0));
		assertEquals(expected, actual);

		ITocContribution a = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/a.xml", true, "en", null, null));
		b = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/b.xml", true, "en", null, null));
		c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/c.xml", true, "en", null, null));
		ITocContribution result_a_b_c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/result_a_b_c.xml", true, "en", null, null));

		contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c }));
		contributions = assembler.assemble(contributions);
		assertEquals(1, contributions.size());
		expected = serialize(result_a_b_c);
		actual = serialize((ITocContribution)contributions.get(0));
		assertEquals(expected, actual);
	}

	private String serialize(ITocContribution contribution) {
		return serialize(contribution.getToc());
	}
	
	private String serialize(IToc toc) {
		StringBuffer buf = new StringBuffer();
		buf.append("<toc");
		if (toc.getLabel() != null) {
			buf.append(" label=\"" + toc.getLabel() + "\"");
		}
		if (toc.getTopic(null).getHref() != null) {
			buf.append(" topic=\"" + toc.getTopic(null).getHref() + "\"");
		}
		buf.append(">\n");
		
		ITopic[] topics = toc.getTopics();
		String indent = "   ";
		for (int i=0;i<topics.length;++i) {
			if (!UAContentFilter.isFiltered(topics[i])) {
				buf.append(serialize(topics[i], indent));
			}
		}
		
		buf.append("</toc>\n");
		return buf.toString();
	}
	
	private String serialize(ITopic topic, String indent) {
		StringBuffer buf = new StringBuffer();
		buf.append(indent + "<topic");
		if (topic.getLabel() != null) {
			buf.append(" label=\"" + topic.getLabel() + "\"");
		}
		String href = topic.getHref();
		if (href != null) {
			buf.append(" href=\"" + href.substring(href.indexOf('/', 1) + 1) + "\"");
		}

		ITopic[] subtopics = topic.getSubtopics();
		if (subtopics.length == 0) {
			buf.append("/>\n");
		}
		else {
			buf.append(">\n");
			for (int i=0;i<subtopics.length;++i) {
				if (!UAContentFilter.isFiltered(subtopics[i])) {
					buf.append(serialize(subtopics[i], indent + "   "));
				}
			}
			buf.append(indent + "</topic>\n");
		}
		return buf.toString();
	}
}
