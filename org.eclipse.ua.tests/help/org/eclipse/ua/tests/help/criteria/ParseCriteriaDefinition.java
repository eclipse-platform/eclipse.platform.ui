/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.criteria.CriteriaDefinition;
import org.eclipse.help.internal.criteria.CriteriaDefinitionAssembler;
import org.eclipse.help.internal.criteria.CriteriaDefinitionContribution;
import org.eclipse.help.internal.criteria.CriteriaDefinitionFile;
import org.eclipse.help.internal.criteria.CriteriaDefinitionFileParser;
import org.eclipse.help.internal.dynamic.DocumentWriter;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class ParseCriteriaDefinition extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ParseCriteriaDefinition.class);
	}

	public void testAssemble() throws Exception {
		CriteriaDefinitionFileParser parser = new CriteriaDefinitionFileParser();
		CriteriaDefinitionContribution a = parser.parse(new CriteriaDefinitionFile(UserAssistanceTestPlugin.getPluginId(), "data/help/criteria/criteria_definition/a.xml", "en"));
		CriteriaDefinitionContribution b = parser.parse(new CriteriaDefinitionFile(UserAssistanceTestPlugin.getPluginId(), "data/help/criteria/criteria_definition/b.xml", "en"));
		CriteriaDefinitionContribution result_a_b = parser.parse(new CriteriaDefinitionFile(UserAssistanceTestPlugin.getPluginId(), "data/help/criteria/criteria_definition/result_a_b.xml", "en"));
		
		CriteriaDefinitionAssembler assembler = new CriteriaDefinitionAssembler();
		@SuppressWarnings("unchecked")
		List contributions = new ArrayList(Arrays.asList(new Object[] { a, b }));
		CriteriaDefinition assembled = assembler.assemble(contributions);
		
		String expected = serialize((UAElement)result_a_b.getCriteriaDefinition());
		String actual = serialize(assembled);
		assertEquals(trimWhiteSpace(expected), trimWhiteSpace(actual));
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
