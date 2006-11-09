/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.cheatsheet.parser;

import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.cheatsheet.util.CheatSheetModelSerializer;
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
	public void testParserValid() throws IOException {
		URL[] urls = ResourceFinder.findFiles(UserAssistanceTestPlugin.getDefault(), "data/cheatsheet/valid", ".xml", false);
		Assert.assertTrue("Unable to find sample cheat sheets to test parser", urls.length > 0);
		for (int i=0;i<urls.length;++i) {
			CheatSheetParser parser = new CheatSheetParser();
			CheatSheet sheet = (CheatSheet)parser.parse(urls[i], UserAssistanceTestPlugin.getPluginId(), CheatSheetParser.ANY);
			Assert.assertNotNull("Tried parsing a valid cheat sheet but parser returned null: " + urls[i], sheet);
			
			String path = urls[i].toString().substring("file:".length());
			String expected = FileUtil.getContents(FileUtil.getResultFile(path));
			String actual = CheatSheetModelSerializer.serialize(sheet);
			Assert.assertEquals("The model serialization generated for the cheatsheet did not match the expected result for: " + path, expected, actual);
		}
	}
}
