/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IIndexEntry2;
import org.eclipse.help.IIndexSee;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.dynamic.DocumentWriter;
import org.eclipse.help.internal.index.Index;
import org.eclipse.help.internal.index.IndexAssembler;
import org.eclipse.help.internal.index.IndexContribution;
import org.eclipse.help.internal.index.IndexEntry;
import org.eclipse.help.internal.index.IndexFile;
import org.eclipse.help.internal.index.IndexFileParser;
import org.eclipse.help.internal.index.IndexSee;
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
		IndexContribution a = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/a.xml", "en"));
		IndexContribution b = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/b.xml", "en"));
		IndexContribution c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/c.xml", "en"));
		IndexContribution result_a_b_c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/result_a_b_c.xml", "en"));
		
		IndexAssembler assembler = new IndexAssembler();
		@SuppressWarnings("unchecked")
		List contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c }));
		Index assembled = assembler.assemble(contributions, Platform.getNL());
		
		String expected = serialize((UAElement)result_a_b_c.getIndex());
		String actual = serialize(assembled);
		assertEquals(trimWhiteSpace(expected), trimWhiteSpace(actual));
	}
	
	public void testAssembleWithSeeAlso() throws Exception {
		IndexFileParser parser = new IndexFileParser();
		IndexContribution contrib = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/d.xml", "en"));
		IndexAssembler assembler = new IndexAssembler();
		@SuppressWarnings("unchecked")
		List contributions = new ArrayList(Arrays.asList(new Object[] { contrib }));
		Index index = assembler.assemble(contributions, Platform.getNL());
	    IIndexEntry[] children = (IIndexEntry[]) index.getEntries();
	    assertEquals(2,children.length);
	    IIndexEntry eclipseEntry = children[0];
		assertEquals("eclipse", eclipseEntry.getKeyword());
		IUAElement[] eclipseChildren = eclipseEntry.getChildren();
		assertEquals(4, eclipseChildren.length);
		assertTrue(eclipseChildren[0] instanceof Topic);
		assertTrue(eclipseChildren[1] instanceof IndexEntry);
		assertTrue(eclipseChildren[2] instanceof IndexSee);
		assertTrue(eclipseChildren[3] instanceof IndexSee);
		IndexSee seeHelios = (IndexSee) eclipseChildren[2];
		IndexSee seeHeliosRelease = (IndexSee) eclipseChildren[3];
		assertEquals(0, seeHelios.getSubpathElements().length);
		assertEquals(1, seeHeliosRelease.getSubpathElements().length);
	    IIndexEntry heliosEntry = children[1];
		assertEquals("helios", heliosEntry.getKeyword());
	    IIndexSee[] heliosSees = ((IIndexEntry2)heliosEntry).getSees();
	    assertEquals(1, heliosSees.length);
	    assertEquals("eclipse", heliosSees[0].getKeyword());
	}
	
	public void testTitle() throws Exception{
		IndexFileParser parser = new IndexFileParser();
		IndexContribution contrib = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/hasTitle.xml", "en"));	
		IndexAssembler assembler = new IndexAssembler();
		@SuppressWarnings("unchecked")
		List contributions = new ArrayList(Arrays.asList(new Object[] { contrib }));
		Index index = assembler.assemble(contributions, Platform.getNL());
	    IIndexEntry[] children = index.getEntries();
	    assertEquals(1,children.length);
	    assertEquals("keyword1", children[0].getKeyword());
	    ITopic[] topics = children[0].getTopics();
	    assertEquals(3, topics.length);
	    assertEquals("topic0", topics[0].getLabel());
	    assertEquals("topic1", topics[1].getLabel());
	    assertEquals("topic2", topics[2].getLabel());
	}
	
	// Replaces white space between ">" and "<" by a single newline
	
	private String trimWhiteSpace(String input) {
		StringBuffer result = new StringBuffer();
		boolean betweenElements = false;
		for (int i = 0; i < input.length(); i++) {
			char next = input.charAt(i);
			if (betweenElements) {
				if (!Character.isWhitespace(next)) {
					result.append(next);
					if (next == '<') {
						betweenElements = false;
					}
				}			
			} else {
				result.append(next);
				if (next == '>') {
					betweenElements = true;
					result.append('\r');
					result.append('\n');
				}
			}
		}
		String resString = result.toString();
		return resString;
	}

	private String serialize(UAElement element) throws Exception {
		DocumentWriter writer = new DocumentWriter();
		return writer.writeString(element, true);
	}
}
