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

package org.eclipse.ua.tests.cheatsheet.composite;

/**
 * Test that dependencies get satisfied when the required tasks are completed
 */

import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetModel;
import org.eclipse.ui.internal.cheatsheets.composite.model.EditableTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.TaskGroup;

import junit.framework.TestCase;

public class TestDependency extends TestCase {
	
	private CompositeCheatSheetModel model;
	private TaskGroup rootTask;
	private TaskGroup subGroup;
	private EditableTask task1;
	private EditableTask task2;
	private EditableTask task3;
	private EditableTask task4;
	
	/**
	 * Initialize a composite cheatsheet whose root task is the parent of
	 * subGroup and task4. subGroup is the parent of task1, task2 and task3
	 */
	private void setupModel() {
		model = new CompositeCheatSheetModel("name", "description", "explorerId");
		model.setId("org.eclipse.ua.tests.testPersistence");
		rootTask = new TaskGroup(model, "root", "name", "set");
		subGroup = new TaskGroup(model, "subGroup", "name", "set");
		task1 = new EditableTask(model, "task1", "name", "ua.junit");
		task2 = new EditableTask(model, "task2", "name", "ua.junit");
		task3 = new EditableTask(model, "task3", "name", "ua.junit");
		task4 = new EditableTask(model, "task4", "name", "ua.junit");
		model.setRootTask(rootTask);
		rootTask.addSubtask(subGroup);	
		rootTask.addSubtask(task4);	
		subGroup.addSubtask(task1);
		subGroup.addSubtask(task2);
		subGroup.addSubtask(task3);
	}
	
	/**
	 * Create a task that depends on two other tasks. The task is not runnable 
	 * until both of its dependencies have been satisfied.
	 */
	public void testDualDependency() {
		setupModel();
		task3.addRequiredTask(task1);
		task3.addRequiredTask(task2);
		assertTrue(task1.requiredTasksCompleted());
		assertTrue(task2.requiredTasksCompleted());
		assertFalse(task3.requiredTasksCompleted());
		task1.complete();
		assertFalse(task3.requiredTasksCompleted());
		task2.complete();
		assertTrue(task3.requiredTasksCompleted());
	}
	
	/**
	 * Verify that skipping a task satisfies the dependency
	 */
	public void testSkippedDependency() {
		setupModel();
		task3.addRequiredTask(task1);
		task3.addRequiredTask(task2);
		assertFalse(task3.requiredTasksCompleted());
		task1.complete();
		assertFalse(task3.requiredTasksCompleted());
		task2.setState(ICompositeCheatSheetTask.SKIPPED);
		assertTrue(task3.requiredTasksCompleted());
	}
	
	/**
	 * Verify that if a task depends on a task group the dependency is satisfied when the group is completed
	 */
	public void testGroupDependency() {
		setupModel();
		task4.addRequiredTask(subGroup);
		assertFalse(task4.requiredTasksCompleted());
		task1.complete();
		assertFalse(task4.requiredTasksCompleted());
		task2.complete();
		assertFalse(task4.requiredTasksCompleted());
		task3.complete();
		assertTrue(task4.requiredTasksCompleted());
	}

}
