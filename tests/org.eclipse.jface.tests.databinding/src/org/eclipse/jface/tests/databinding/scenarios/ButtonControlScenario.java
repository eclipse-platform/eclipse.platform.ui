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
package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class ButtonControlScenario extends ScenariosTestCase {

	private Adventure adventure;
	private Button button;

	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		button = new Button(getComposite(), SWT.CHECK);		
		adventure = SampleData.WINTER_HOLIDAY;
	}

	protected void tearDown() throws Exception {
		button.dispose();
		super.tearDown();
	}

	public void testScenario01() {
		// Bind the button's selection to the adventure "isPetsAllowed"
		getDbc().bind(button, new Property(adventure, "petsAllowed"),null);
		// Check the model and GUI are in the same state
		assertEquals(button.getSelection(),adventure.isPetsAllowed());
		// Change the model and check the GUI is updated
		boolean newBoolean = !adventure.isPetsAllowed();
		adventure.setPetsAllowed(newBoolean);
		assertEquals(newBoolean,adventure.isPetsAllowed());
		assertEquals(button.getSelection(),newBoolean);
		// Change the GUI and check the model
		newBoolean = !newBoolean;
		button.setSelection(newBoolean);
		button.notifyListeners(SWT.Selection,null);
		assertEquals(newBoolean,adventure.isPetsAllowed());
		// Verify that changes to the model can occur in a non UI thread
		newBoolean = !newBoolean;
		final boolean finalNewBoolean = newBoolean;
		invokeNonUI(new Runnable(){
			public void run(){
				adventure.setPetsAllowed(finalNewBoolean);
			}
		});
		spinEventLoop(0);
		assertEquals(newBoolean,button.getSelection());
		
	}
	
	public void testScenario02() {
		// Test with an SWT.Toggle button
		button.dispose();
		button = new Button(getComposite(), SWT.TOGGLE);			
		// Bind the button's selection to the adventure "isPetsAllowed"
		getDbc().bind(button, new Property(adventure, "petsAllowed"),null);
		// Check the model and GUI are in the same state
		assertEquals(button.getSelection(),adventure.isPetsAllowed());
		// Change the model and check the GUI is updated
		boolean newBoolean = !adventure.isPetsAllowed();
		adventure.setPetsAllowed(newBoolean);
		assertEquals(newBoolean,adventure.isPetsAllowed());
		assertEquals(button.getSelection(),newBoolean);
		// Change the GUI and check the model
		newBoolean = !newBoolean;
		button.setSelection(newBoolean);
		button.notifyListeners(SWT.Selection,null);
		assertEquals(newBoolean,adventure.isPetsAllowed());
	}
	
	public void testScenario03() {
		// Test with an SWT.Radio button
		button.dispose();
		button = new Button(getComposite(), SWT.RADIO);
		// Bind the button's selection to the adventure "isPetsAllowed"
		getDbc().bind(button, new Property(adventure, "petsAllowed"),null);
		// Check the model and GUI are in the same state
		assertEquals(button.getSelection(),adventure.isPetsAllowed());
		// Change the model and check the GUI is updated
		boolean newBoolean = !adventure.isPetsAllowed();
		adventure.setPetsAllowed(newBoolean);
		assertEquals(newBoolean,adventure.isPetsAllowed());
		assertEquals(button.getSelection(),newBoolean);
		// Change the GUI and check the model
		newBoolean = !newBoolean;
		button.setSelection(newBoolean);
		button.notifyListeners(SWT.Selection,null);
		assertEquals(newBoolean,adventure.isPetsAllowed());
	}		
}
