/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.composite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ui.internal.cheatsheets.composite.model.CheatSheetTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetModel;
import org.eclipse.ui.internal.cheatsheets.composite.parser.CompositeCheatSheetParser;

public class TestCompositeParser extends TestCase {
	
	private static final String COMPOSITES_FOLDER = "data/cheatsheet/composite/";
	private CompositeCheatSheetParser parser;
	
	protected void setUp() throws Exception {
	    parser = new CompositeCheatSheetParser();
	}
	
	private CompositeCheatSheetModel parseTestFile(String path) {
		URL testURL = ResourceFinder.findFile(UserAssistanceTestPlugin.getDefault(), 
					       COMPOSITES_FOLDER + path);
		return parser.parseGuide(testURL);
	}
	
	public void testNullInput() {
		assertNull(parser.parseGuide(null));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Could not open");
	}
	
	public void testBadURL() {
		try {
			assertNull(parser.parseGuide(new URL("file:/nonexistent")));
		} catch (MalformedURLException e) {
			fail("Exception thrown");
		}
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Could not open");
	}
	
	public void testSimpleGuide() {
		CompositeCheatSheetModel model = parseTestFile("SingleTask.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
	}
	
	public void testNoTasks() {
		CompositeCheatSheetModel model = parseTestFile("GuideWithoutTasks.xml");
		assertNull(model);
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Missing root task");
	}
	
	public void testInvalidRoot() {
		assertNull(parseTestFile("InvalidRoot.xml"));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Root node is not");
	}
	
	public void testTwoRootTasksGuide() {
		assertNull(parseTestFile("TwoRootTasks.xml"));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "more than one root task");
	}
	
	public void testParamNoName() {
		assertNotNull(parseTestFile("ParamNoName.xml"));
		assertEquals(IStatus.WARNING, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Parameter has no name");		
	}

	public void testParamNoValue() {
		assertNotNull(parseTestFile("ParamNoValue.xml"));
		assertEquals(IStatus.WARNING, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Parameter has no value");		
	}
	
	public void testValidParameters() {
		CompositeCheatSheetModel model = parseTestFile("ValidParameter.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
		Dictionary params = model.getRootTask().getParameters();
		assertEquals(1, params.size());
		assertEquals("b", params.get("a"));
	}
	
	public void testDependency() {
		CompositeCheatSheetModel model = parseTestFile("TaskDependency.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
		Dictionary params = model.getRootTask().getParameters();
		CheatSheetTask task1 = model.getDependencies().getTask("task1");
		CheatSheetTask task2 = model.getDependencies().getTask("task2");
		assertTrue(task1.getRequiredTasks().length == 0);
		assertTrue(task1.getSuccessorTasks().length == 1);
		assertEquals(task2, task1.getSuccessorTasks()[0]);
		assertTrue(task2.getSuccessorTasks().length == 0);
		assertTrue(task2.getRequiredTasks().length == 1);
		assertEquals(task1, task2.getRequiredTasks()[0]);
	}
	
	public void testBackwardDependency() {
		CompositeCheatSheetModel model = parseTestFile("BackwardDependency.xml");
		assertNotNull(model);
		assertTrue(parser.getStatus().isOK());
		Dictionary params = model.getRootTask().getParameters();
		CheatSheetTask task1 = model.getDependencies().getTask("task1");
		CheatSheetTask task2 = model.getDependencies().getTask("task2");
		assertTrue(task1.getRequiredTasks().length == 0);
		assertTrue(task1.getSuccessorTasks().length == 1);
		assertEquals(task2, task1.getSuccessorTasks()[0]);
		assertTrue(task2.getSuccessorTasks().length == 0);
		assertTrue(task2.getRequiredTasks().length == 1);
		assertEquals(task1, task2.getRequiredTasks()[0]);
	}
	
	public void testDependencyWithoutId() {
		assertNull(parseTestFile("DependencyWithoutId.xml"));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Missing task id");
	}

	public void testDependencyWithInvalidId() {
		assertNull(parseTestFile("DependencyInvalidId.xml"));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Invalid id");
	}
	

	public void testCircularDependency() {
		assertNull(parseTestFile("CircularDependency.xml"));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertMultiStatusContains(parser.getStatus(), "Cycle detected");
	}
	
	public void testSelfDependency() {
		assertNull(parseTestFile("SelfDependency.xml"));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertMultiStatusContains(parser.getStatus(), "Cycle detected");
	}
	
	public void testDuplicateId() {
		assertNull(parseTestFile("DuplicateTaskId.xml"));
		assertEquals(IStatus.ERROR, parser.getStatus().getSeverity());
		assertStatusContains(parser.getStatus(), "Duplicate task id");
	}

	public void assertStatusContains(IStatus status, String text) {
		if (status.getMessage().indexOf(text) == -1) {
			fail("Expected status message to contain '" + text + "' actual message is '"
					+ status.getMessage() + "'");
		}
	}
	
	public void assertMultiStatusContains(IStatus status, String text) {
		assertTrue(status instanceof MultiStatus);
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i].getMessage().indexOf(text) >= 0) {
				return;
			}
		}
		if (status.getMessage().indexOf(text) == -1) {
			fail("Expected status message to contain '" + text + "' status.toString = '"
					+ status.toString() + "'");
		}
	}

}
