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

package org.eclipse.ua.tests.cheatsheet.composite;

import org.eclipse.ui.internal.cheatsheets.composite.model.AbstractTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetModel;
import org.eclipse.ui.internal.cheatsheets.composite.model.EditableTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.TaskGroup;
import org.eclipse.ui.internal.cheatsheets.composite.model.TaskStateUtilities;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.provisional.cheatsheets.ITaskGroup;

import junit.framework.TestCase;

/**
 * Tests for the functions which determine the state of tasks
 */

public class TestState extends TestCase {
	
	private CompositeCheatSheetModel model;
	
	protected void setUp() throws Exception {
		model = new CompositeCheatSheetModel("name", "description", "explorerId");
	}
	
	private void skip(ICompositeCheatSheetTask task) {
		((AbstractTask)task).setState(ICompositeCheatSheetTask.SKIPPED);
	}

	public void testNoParent() {
		EditableTask task = new EditableTask(model, "id", "name", "ua.junit");
		assertNull(TaskStateUtilities.findSkippedAncestor(task));
		assertNull(TaskStateUtilities.findCompletedAncestor(task));
		assertNull(TaskStateUtilities.findBlockedAncestor(task));
	}
	
	public void testSelfSkip() {
		EditableTask task = new EditableTask(model, "id", "name", "ua.junit");
		skip(task);
		assertNull(TaskStateUtilities.findSkippedAncestor(task));
		assertNull(TaskStateUtilities.findCompletedAncestor(task));
		assertNull(TaskStateUtilities.findBlockedAncestor(task));
	}

	public void testSkippedParent() {
		TaskGroup root = new TaskGroup(model, "root", "rname", ITaskGroup.SEQUENCE);
		TaskGroup group = new TaskGroup(model, "group", "gname", ITaskGroup.SEQUENCE);
		EditableTask task = new EditableTask(model, "id", "name", "ua.junit");
		root.addSubtask(group);
		group.addSubtask(task);
		skip(root);
		skip(group);
		assertEquals(group, TaskStateUtilities.findSkippedAncestor(task));
		assertNull(TaskStateUtilities.findBlockedAncestor(task));
	}
	
	public void testSkippedGrandparent() {
		TaskGroup root = new TaskGroup(model, "root", "rname", ITaskGroup.SEQUENCE);
		TaskGroup group = new TaskGroup(model, "group", "gname", ITaskGroup.SEQUENCE);
		EditableTask task = new EditableTask(model, "id", "name", "ua.junit");
		root.addSubtask(group);
		group.addSubtask(task);
		skip(root);
		assertEquals(root, TaskStateUtilities.findSkippedAncestor(task));
		assertNull(TaskStateUtilities.findBlockedAncestor(task));
	}

	public void testCompletedGrandparent() {
		TaskGroup root = new TaskGroup(model, "root", "rname", ITaskGroup.CHOICE);
		TaskGroup group = new TaskGroup(model, "group", "gname", ITaskGroup.SEQUENCE);
		EditableTask task = new EditableTask(model, "id", "name", "ua.junit");
		EditableTask completed = new EditableTask(model, "id2", "name2", "ua.junit");
		root.addSubtask(group);
		group.addSubtask(task);
		root.addSubtask(completed);
		assertNull(TaskStateUtilities.findCompletedAncestor(task));
		assertNull(TaskStateUtilities.findBlockedAncestor(task));
		assertNull(TaskStateUtilities.findSkippedAncestor(task));
		completed.complete();
		assertEquals(root, TaskStateUtilities.findCompletedAncestor(task));
	}
	
	public void testBlockedGrandparent() {
		TaskGroup root = new TaskGroup(model, "root", "rname", ITaskGroup.CHOICE);
		TaskGroup grandparent = new TaskGroup(model, "group1", "gname1", ITaskGroup.SEQUENCE);
		TaskGroup parent = new TaskGroup(model, "group2", "gname2", ITaskGroup.SEQUENCE);
		EditableTask task = new EditableTask(model, "id", "name", "ua.junit");
		EditableTask required = new EditableTask(model, "id2", "name2", "ua.junit");
		root.addSubtask(grandparent);
		grandparent.addSubtask(parent);
		parent.addSubtask(task);
		root.addSubtask(required);
		grandparent.addRequiredTask(required);
		assertNull(TaskStateUtilities.findCompletedAncestor(task));
		assertNull(TaskStateUtilities.findSkippedAncestor(task));
		assertEquals(grandparent, TaskStateUtilities.findBlockedAncestor(task));
		assertNull(TaskStateUtilities.findBlockedAncestor(grandparent));
		required.complete();
		assertNull(TaskStateUtilities.findBlockedAncestor(task));
	}

}
