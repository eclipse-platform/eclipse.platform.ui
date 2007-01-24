/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/**
 * Tests which should generate no error or warning when parsing
 */

package org.eclipse.ua.tests.cheatsheet.parser;

import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetParser;
import org.eclipse.ui.internal.cheatsheets.data.ICheatSheet;

public class NoError extends TestCase {
	private static final String NO_ERROR_FOLDER = "data/cheatsheet/no_error/";
	private static final String OTHER_FOLDER = "data/cheatsheet/other/";

	private CheatSheetParser parser;
	
	protected void setUp() throws Exception {
	    parser = new CheatSheetParser();
	}
	
	private ICheatSheet parseTestFile(String path) {
		URL testURL = ResourceFinder.findFile(UserAssistanceTestPlugin.getDefault(), 
					       path);
		return parser.parse(testURL, UserAssistanceTestPlugin.getPluginId(), CheatSheetParser.SIMPLE_ONLY);
	}
	
	public void testConfirmRequiredCombinations() {
		ICheatSheet model = parseTestFile(NO_ERROR_FOLDER + "ConfirmRequired.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
	}

	public void testRestrictedAction() {
		ICheatSheet model = parseTestFile(OTHER_FOLDER + "TestActions.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
		assertTrue(model instanceof CheatSheet);
		assertTrue(((CheatSheet)model).isContainsCommandOrAction());
	}

	public void testRestrictedCommand() {
		ICheatSheet model = parseTestFile(OTHER_FOLDER + "TestCommand.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
		assertTrue(model instanceof CheatSheet);
		assertTrue(((CheatSheet)model).isContainsCommandOrAction());
	}
	
	public void testNoRestriction() {
		ICheatSheet model = parseTestFile(OTHER_FOLDER + "NoActions.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
		assertTrue(model instanceof CheatSheet);
		assertFalse(((CheatSheet)model).isContainsCommandOrAction());
	}
	
	
}
