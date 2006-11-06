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

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IndexContribution;
import org.eclipse.help.Node;
import org.eclipse.help.internal.dynamic.NodeWriter;
import org.eclipse.help.internal.index.Index;
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
		IndexContribution a = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/a.xml", "en"));
		IndexContribution b = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/b.xml", "en"));
		IndexContribution c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/c.xml", "en"));
		IndexContribution result_a_b_c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/result_a_b_c.xml", "en"));
		
		IndexAssembler assembler = new IndexAssembler();
		List contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c }));
		Index assembled = assembler.assemble(contributions, Platform.getNL());
		
		String expected = serialize(result_a_b_c.getIndex());
		String actual = serialize(assembled);
		assertEquals(expected, actual);
	}
	private String serialize(Node node) {
		StringBuffer buf = new StringBuffer();
		String indent = "";
		NodeWriter writer = new NodeWriter();
		writer.write(node, buf, true, indent, false);
		return buf.toString();
	}
}
