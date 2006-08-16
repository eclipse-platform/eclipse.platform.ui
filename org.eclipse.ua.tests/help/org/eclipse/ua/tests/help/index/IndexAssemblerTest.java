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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexContribution;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.INode;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.index.IndexAssembler;
import org.eclipse.help.internal.index.IndexFile;
import org.eclipse.help.internal.index.IndexFileParser;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class IndexAssemblerTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(IndexAssemblerTest.class);
	}

	public void testAssemble() throws Exception {
		IndexFileParser parser = new IndexFileParser();
		IIndexContribution a = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/a.xml", "en"));
		IIndexContribution b = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/b.xml", "en"));
		IIndexContribution c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/c.xml", "en"));
		IIndexContribution result_a_b_c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/result_a_b_c.xml", "en"));
		
		IndexAssembler assembler = new IndexAssembler();
		List contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c }));
		IIndex assembled = assembler.assemble(contributions);
		
		String expected = serialize(result_a_b_c.getIndex());
		String actual = serialize(assembled);
		assertEquals(expected, actual);
	}

	private String serialize(IIndex index) {
		StringBuffer buf = new StringBuffer();
		buf.append("<index>\n");
		
		IIndexEntry[] entries = index.getEntries();
		String indent = "   ";
		for (int i=0;i<entries.length;++i) {
			buf.append(serialize(entries[i], indent));
		}
		
		buf.append("</index>\n");
		return buf.toString();
	}
	
	private String serialize(IIndexEntry entry, String indent) {
		StringBuffer buf = new StringBuffer();
		buf.append(indent + "<entry");
		if (entry.getKeyword() != null) {
			buf.append(" keyword=\"" + entry.getKeyword() + "\"");
		}
		INode[] children = entry.getChildren();
		if (children.length == 0) {
			buf.append("/>\n");
		}
		else {
			buf.append(">\n");
			for (int i=0;i<children.length;++i) {
				if (children[i] instanceof IIndexEntry) {
					buf.append(serialize((IIndexEntry)children[i], indent + "   "));
				}
				else {
					buf.append(serialize((ITopic)children[i], indent + "   "));
				}
			}
			buf.append(indent + "</entry>\n");
		}
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
				buf.append(serialize(subtopics[i], indent + "   "));
			}
			buf.append(indent + "</topic>\n");
		}
		return buf.toString();
	}
}
