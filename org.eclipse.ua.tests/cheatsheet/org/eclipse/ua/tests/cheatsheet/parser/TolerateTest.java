/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;

import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

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

	private void parseCheatsheet(String file) {
		Path path = new Path("data/cheatsheet/valid/tolerate/" + file);
		URL url = FileLocator.find(UserAssistanceTestPlugin.getDefault().getBundle(), path, null);
		CheatSheetParser parser = new CheatSheetParser();
		CheatSheet sheet = (CheatSheet)parser.parse(url, UserAssistanceTestPlugin.getPluginId(), CheatSheetParser.SIMPLE_ONLY);
		Assert.assertEquals("Warning not generated: " + url, IStatus.WARNING, parser.getStatus().getSeverity());
		Assert.assertNotNull("Tried parsing a tolerable cheat sheet but parser returned null: " + url, sheet);
	}

	public void testItemExtraAttr() {
		parseCheatsheet("ItemElement_ExtraAttr.xml");
	}

	public void testIntroExtraElement() {
		parseCheatsheet("IntroElement_ExtraElement.xml");
	}

	public void testIntroExtraAttr() {
		parseCheatsheet("IntroElement_ExtraAttr.xml");
	}

	public void testDescExtraElement() {
		parseCheatsheet("DescriptionElement_ExtraElements.xml");
	}

	public void testConditionalExtraElement() {
		parseCheatsheet("ConditionalSubItem_ExtraElement.xml");
	}

	public void testConditionalExtraAttr() {
		parseCheatsheet("ConditionalSubItem_ExtraAttr.xml");
	}

	public void testElementExtraElement() {
		parseCheatsheet("CheatSheetElement_ExtraElement.xml");
	}

	public void testElementExtraAttr() {
		parseCheatsheet("CheatSheetElement_ExtraAttr.xml");
	}

	public void testExtraElement() {
		parseCheatsheet("ActionElement_ExtraElement.xml");
	}

	public void testExtraAttr() {
		parseCheatsheet("ActionElement_ExtraAttr.xml");
	}

}
