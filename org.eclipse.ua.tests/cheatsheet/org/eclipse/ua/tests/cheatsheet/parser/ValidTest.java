/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.cheatsheet.parser;

import java.net.URL;
import java.util.StringTokenizer;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.cheatsheet.util.CheatSheetModelSerializer;
import org.eclipse.ua.tests.intro.util.IntroModelSerializerTest;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetParser;

/*
 * Tests the cheat sheets parser on valid cheat sheets.
 */
public class ValidTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ValidTest.class);
	}
	
	/*
	 * Test valid cheat sheets.
	 */
	public void testParserValid() {
		URL[] urls = ResourceFinder.findFiles(UserAssistanceTestPlugin.getDefault(), "data/cheatsheet/valid", ".xml", false);
		Assert.assertTrue("Unable to find sample cheat sheets to test parser", urls.length > 0);
		for (int i=0;i<urls.length;++i) {
			CheatSheetParser parser = new CheatSheetParser();
			CheatSheet sheet = (CheatSheet)parser.parse(urls[i]);
			Assert.assertNotNull("Tried parsing a valid cheat sheet but parser returned null: " + urls[i], sheet);
			
			String resultFile = IntroModelSerializerTest.getResultFile(urls[i].toString().substring("file:".length()));

			try {
				String expected = FileUtil.getContents(resultFile);
				String actual = CheatSheetModelSerializer.serialize(sheet);
				
				StringTokenizer tok1 = new StringTokenizer(expected, "\n");
				StringTokenizer tok2 = new StringTokenizer(actual, "\n");
				
				/*
				 * Report the line number and line text where it didn't match,
				 * as well as the extension id and expected results file.
				 */
				int lineNumber = 0;
				while (tok1.hasMoreTokens() && tok2.hasMoreTokens()) {
					String a = tok1.nextToken();
					String b = tok2.nextToken();
					Assert.assertEquals("Serialized cheat sheet model text for \"" + resultFile + "\" did not match expected result. First difference occured on line " + lineNumber + ".", a, b);
					++lineNumber;
				}
			}
			catch(Exception e) {
				Assert.fail("An error occured while loading expected result file for intro at: " + resultFile + ": " + e);
			}
		}
	}
}
