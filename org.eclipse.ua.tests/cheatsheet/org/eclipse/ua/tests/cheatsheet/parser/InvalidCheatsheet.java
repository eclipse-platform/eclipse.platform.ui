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

package org.eclipse.ua.tests.cheatsheet.parser;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ua.tests.cheatsheet.util.StatusCheck;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetParser;
import org.eclipse.ui.internal.cheatsheets.data.ICheatSheet;

public class InvalidCheatsheet extends TestCase {
	private static final String INVALID_CHEATSHEET_FOLDER = "data/cheatsheet/invalid/";
	private CheatSheetParser parser;
	
	protected void setUp() throws Exception {
	    parser = new CheatSheetParser();
	}
	
	private ICheatSheet parseTestFile(String path) {
		URL testURL = ResourceFinder.findFile(UserAssistanceTestPlugin.getDefault(), 
					       INVALID_CHEATSHEET_FOLDER + path);
		return parser.parse(testURL, UserAssistanceTestPlugin.getPluginId(), CheatSheetParser.SIMPLE_ONLY);
	}
	
	public void testBadURL() {
		try {
			assertNull(parser.parse(new URL("file:/nonexistent"), UserAssistanceTestPlugin.getPluginId(), CheatSheetParser.SIMPLE_ONLY));
		} catch (MalformedURLException e) {
			fail("Exception thrown");
		}
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "Could not open");
	}

	public void testActionMissingClass() {
		ICheatSheet model = parseTestFile("ActionElement_MissingClass.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a class");
	}

	public void testActionMissingPluginId() {
		ICheatSheet model = parseTestFile("ActionElement_MissingPluginId.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a pluginId");
	}
	
	public void testCommandMissingSerialization() {
		ICheatSheet model = parseTestFile("Command_MissingSerialization.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a serialization");
	}

	public void testInvalidParamNumber() {
		ICheatSheet model = parseTestFile("ActionElement_ParamInvalidNumber.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "invalid parameter number");
	}

	public void testInvalidParamRange() {
		ICheatSheet model = parseTestFile("ActionElement_ParamInvalidRange.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "invalid range");
	}

	public void testMissingTitle() {
		ICheatSheet model = parseTestFile("CheatSheetElement_MissingTitle.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a title");
	}

	public void testNotDefined() {
		ICheatSheet model = parseTestFile("CheatSheetElement_NotDefined.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "The <cheatsheet> element must be the root");
	}

	public void testConditionalSubitemMissingCondition() {
		ICheatSheet model = parseTestFile("CondSubItem_MissingCondition.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a condition");
	}

	public void testConditionalSubitemMissingSubitem() {
		ICheatSheet model = parseTestFile("CondSubItem_MissingSubItem.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a subitem");
	}

	public void testIntroElementManyDefined() {
		ICheatSheet model = parseTestFile("IntroElement_ManyDefined.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "can only contain one <intro> element");
	}

	public void testIntroElementMissingDescription() {
		ICheatSheet model = parseTestFile("IntroElement_MissingDescription.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "The description for element 'intro' was not defined");
	}
	
	public void testIntroElementManyDescriptions() {
		ICheatSheet model = parseTestFile("IntroElement_ManyDescriptions.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "more than one description");
	}
	
	public void testIntroElementNotDefined() {
		ICheatSheet model = parseTestFile("IntroElement_NotDefined.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must contain an <intro>");
	}

	public void testItemElementMissingTitle() {
		ICheatSheet model = parseTestFile("ItemElement_MissingTitle.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a title");
	}
	
	public void testItemElementMissingDescription() {
		ICheatSheet model = parseTestFile("ItemElement_MissingDescription.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "The description for element 'item' was not defined");
	}
	
	public void testItemElementManyDescriptions() {
		ICheatSheet model = parseTestFile("ItemElement_ManyDescriptions.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "more than one description");
	}
	
	public void testItemElementNotDefined() {
		ICheatSheet model = parseTestFile("ItemElement_NotDefined.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "at least one <item>");
	}

	public void testPerformWhenMissingAction() {
		ICheatSheet model = parseTestFile("PerformWhen_MissingAction.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify an action");
	}

	public void testPerformWhenMissingCondition() {
		ICheatSheet model = parseTestFile("PerformWhen_MissingCondition.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a condition");
	}
	
	public void testSubitemElementMissingLabel() {
		ICheatSheet model = parseTestFile("SubItem_MissingLabel.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a label");
	}
	
	public void testRepeatedSubitemMissingSubitem() {
		ICheatSheet model = parseTestFile("RepSubItem_MissingSubItem.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a subitem");
	}

	public void testRepeatedSubitemMissingValues() {
		ICheatSheet model = parseTestFile("RepSubItem_MissingValues.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "must specify a values");
	}

	public void testActionAndPerformWhen() {
		ICheatSheet model = parseTestFile("ActionAndPerformWhen.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "incompatible");
	}

	public void testCommandAndAction() {
		ICheatSheet model = parseTestFile("CommandAndAction.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "incompatible");
	}

	public void testCommandAndSubitem() {
		ICheatSheet model = parseTestFile("CommandAndSubitem.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "incompatible");
	}

	public void testSubitemAndPerformWhen() {
		ICheatSheet model = parseTestFile("SubitemAndPerformWhen.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "incompatible");
	}

	public void testTwoActions() {
		ICheatSheet model = parseTestFile("TwoActions.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "more than one");
	}

	public void testTwoCommands() {
		ICheatSheet model = parseTestFile("TwoCommands.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "more than one");
	}

	public void testTwoPerformWhen() {
		ICheatSheet model = parseTestFile("TwoPerformWhen.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		StatusCheck.assertStatusContains(parser.getStatus(), "more than one");
	}

}
