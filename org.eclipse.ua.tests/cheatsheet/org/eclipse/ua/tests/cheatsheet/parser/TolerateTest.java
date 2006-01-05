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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetParser;

/*
 * Tests the cheat sheets parser on tolerable cheat sheets. This means they're not strictly correct,
 * but the parser will tolerate them.
 */
public class TolerateTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(TolerateTest.class);
	}
	
	/*
	 * Test cheat sheets that are not quite correct, but are tolerated by the parser.
	 */
	public void testParserTolerate() {
		URL[] urls = ResourceFinder.findFiles(UserAssistanceTestPlugin.getDefault(), "data/cheatsheet/valid/tolerate", ".xml", true);
		Assert.assertTrue("Unable to find sample cheat sheets to test parser", urls.length > 0);
		for (int i=0;i<urls.length;++i) {
			CheatSheetParser parser = new CheatSheetParser();
			CheatSheet sheet = parser.parse(urls[i]);
			Assert.assertNotNull("Tried parsing a tolerable cheat sheet but parser returned null: " + urls[i], sheet);
		}
	}
}
