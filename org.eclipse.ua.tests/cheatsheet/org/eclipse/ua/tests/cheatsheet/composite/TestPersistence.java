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

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ua.tests.cheatsheet.util.MockTaskEditor;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.composite.model.CheatSheetTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetModel;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetSaveHelper;

public class TestPersistence extends TestCase {
	
	/**
	 * Test that data can be saved and restored in Mementos
	 */
	
	private static final String MEMENTO_TAG = "Mtag";
	private final static String PATH1 = "Path1";
	private final static String PATH2 = "Path2";
	private final static String KEY = "key";
	private final static String DATA1 = "1";
	private final static String DATA2 = "2";
	
	private CompositeCheatSheetModel model;
	private CheatSheetTask rootTask;
	private CheatSheetTask task1;
	private CheatSheetTask task2;
	private MockTaskEditor editor1;
	private MockTaskEditor editor2;
	private CompositeCheatSheetSaveHelper helper;
	
	/**
	 * Initialize a composite cheatsheet with one root and two leaf tasks.
	 */
	private void createCompositeCheatSheet() {
		model = new CompositeCheatSheetModel("name", "description", "explorerId");
		model.setId("org.eclipse.ua.tests.testPersistence");
		rootTask = new CheatSheetTask(model, "root", "name", "kind", null, "description");
		task1 = new CheatSheetTask(model, "task1", "name", "kind", null, "description");
		task2 = new CheatSheetTask(model, "task2", "name", "kind", null, "description");
		helper = new CompositeCheatSheetSaveHelper();
		model.setSaveHelper(helper);
		editor1 = new MockTaskEditor();
		editor2 = new MockTaskEditor();
		task1.setEditor(editor1);
		task2.setEditor(editor2);
		model.setRootTask(rootTask);
		rootTask.addSubtask(task1);	
		rootTask.addSubtask(task2);	
	}
	
	/**
	 * Test that the routines saveMemento() and readMemento() can write
	 * mementos to different files and keep the contents distinct.
	 */
    public void testMementoSaveMultipleFiles() {
    	XMLMemento memento = XMLMemento.createWriteRoot(MEMENTO_TAG);
    	memento.putString(KEY, DATA1);
    	CheatSheetPlugin cheatSheetPlugin = CheatSheetPlugin.getPlugin();
		IStatus status = cheatSheetPlugin.saveMemento(memento, PATH1);
    	assertTrue(status.isOK());
    	memento = XMLMemento.createWriteRoot(MEMENTO_TAG);
    	memento.putString(KEY, DATA2);
    	status = cheatSheetPlugin.saveMemento(memento, PATH2);
    	assertTrue(status.isOK());
    	memento = cheatSheetPlugin.readMemento(PATH1);
    	assertEquals(DATA1, memento.getString(KEY));
    	memento = cheatSheetPlugin.readMemento(PATH2);
    	assertEquals(DATA2, memento.getString(KEY));
    }
    
    public void testSaveTaskState() {
    	createCompositeCheatSheet();
    	task1.setState(ICompositeCheatSheetTask.IN_PROGRESS);
    	task1.setPercentageComplete(25);
    	task2.setState(ICompositeCheatSheetTask.COMPLETED);
    	helper.saveCompositeState(model);

    	createCompositeCheatSheet();
    	model.loadState();
    	assertEquals(ICompositeCheatSheetTask.IN_PROGRESS, task1.getState());
    	assertEquals(ICompositeCheatSheetTask.COMPLETED, task2.getState());
    	assertEquals(25, task1.getPercentageComplete());
    }
    
    public void testSaveTaskMemento() {
    	final String value1 = "13579";
    	final String value2 = "AB24";
    	createCompositeCheatSheet();
    	// Start task with no memento
    	editor1.setInput(task1, null);
    	editor2.setInput(task2, null);
    	assertEquals(MockTaskEditor.NO_MEMENTO, editor1.getValue());
    	assertEquals(MockTaskEditor.NO_MEMENTO, editor2.getValue());
    
    	// Set the values to save in the memento
    	editor1.setValue(value1);
    	editor2.setValue(value2);
    	helper.saveCompositeState(model);
    	
    	createCompositeCheatSheet();
    	model.loadState();
    	editor1.setInput(task1, model.getTaskMemento(task1.getId()));
    	editor2.setInput(task2, model.getTaskMemento(task2.getId()));
    	assertEquals(value1, editor1.getValue());
    	assertEquals(value2, editor2.getValue());
    }
	
}
