/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;
import org.osgi.framework.Bundle;

import org.eclipse.ua.tests.cheatsheet.util.CheatSheetModelSerializer;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

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
	
	private void parseCheatsheet(String file) throws IOException {
		Path path = new Path("data/cheatsheet/valid/" + file);
		Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle();
		URL url = FileLocator.find(bundle, path, null);
		CheatSheetParser parser = new CheatSheetParser();
		CheatSheet sheet = (CheatSheet)parser.parse(url, UserAssistanceTestPlugin.getPluginId(), CheatSheetParser.ANY);
		Assert.assertNotNull("Tried parsing a valid cheat sheet but parser returned null: " + url, sheet);		
		String expectedPath = "data/cheatsheet/valid/" + getExpected(file);
		String expected = FileUtil.getContents(bundle, expectedPath);
		String actual = CheatSheetModelSerializer.serialize(sheet);
		Assert.assertEquals("The model serialization generated for the cheatsheet did not match the expected result for: " + path, expected, actual);
	}

	private String getExpected(String file) {
		int suffix = file.lastIndexOf(".xml");
		return file.substring(0, suffix) + "_expected.txt";
	}

	public void testSubItems() throws IOException {
		parseCheatsheet("TestSubItems.xml");
	}

	public void testParameters() throws IOException {
		parseCheatsheet("TestParameters.xml");
	}

	public void testOpeningURL() throws IOException {
		parseCheatsheet("TestOpeningURL.xml");
	}

	public void testDynamicSubitems() throws IOException {
		parseCheatsheet("TestDynamicSubItems.xml");
	}

	public void testDescriptionFormatting() throws IOException {
		parseCheatsheet("TestDescriptionFormatting.xml");
	}

	public void testCSActions() throws IOException {
		parseCheatsheet("TestCSActions.xml");
	}

	public void testContextHelp() throws IOException {
		parseCheatsheet("TestContext_Help.xml");
	}

	public void testActions() throws IOException {
		parseCheatsheet("TestActions.xml");
	}

	public void testHelloWorldWithSubitems() throws IOException {
		parseCheatsheet("HelloWorldWithSubitems.xml");
	}

	public void testHelloWorldWithExtensions() throws IOException {
		parseCheatsheet("HelloWorldWithExtensions.xml");
	}

	public void testHelloWorld() throws IOException {
		parseCheatsheet("HelloWorld.xml");
	}
	
}
