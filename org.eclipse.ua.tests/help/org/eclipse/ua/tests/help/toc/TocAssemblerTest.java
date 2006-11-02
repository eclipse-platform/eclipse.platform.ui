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

import org.eclipse.help.TocContribution;
import org.eclipse.help.internal.dynamic.NodeWriter;
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
		TocContribution b = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/b.xml", true, "en", null, null));
		TocContribution c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/c.xml", true, "en", null, null));
		TocContribution result_b_c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/result_b_c.xml", true, "en", null, null));
		
		TocAssembler assembler = new TocAssembler();
		List contributions = new ArrayList(Arrays.asList(new Object[] { b, c }));
		contributions = assembler.assemble(contributions);
		assertEquals(1, contributions.size());
		String expected = serialize(result_b_c);
		String actual = serialize((TocContribution)contributions.get(0));
		assertEquals(expected, actual);

		TocContribution a = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/a.xml", true, "en", null, null));
		b = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/b.xml", true, "en", null, null));
		c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/c.xml", true, "en", null, null));
		TocContribution d = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/d.xml", false, "en", null, null));
		TocContribution result_a_b_c_d = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/result_a_b_c_d.xml", true, "en", null, null));

		contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c, d }));
		contributions = assembler.assemble(contributions);
		assertEquals(1, contributions.size());
		expected = serialize(result_a_b_c_d);
		actual = serialize((TocContribution)contributions.get(0));
		assertEquals(expected, actual);
	}

	private String serialize(TocContribution contribution) {
		StringBuffer buf = new StringBuffer();
		String indent = "";
		NodeWriter writer = new NodeWriter();
		writer.write(contribution.getToc(), buf, true, indent, false);
		return buf.toString();
	}
}
