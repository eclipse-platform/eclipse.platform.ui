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
import org.eclipse.jface.databinding.swt.SWTUpdatableFactory;
import org.eclipse.jface.tests.databinding.scenarios.model.Adventure;
import org.eclipse.jface.tests.databinding.scenarios.model.SampleData;
import org.eclipse.jface.tests.databinding.scenarios.model.Transportation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class TextControlScenario extends ScenariosTestCase {

	private Adventure adventure;
	private Transportation transportation;	

	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		adventure = SampleData.WINTER_HOLIDAY;
		transportation = SampleData.EXECUTIVE_JET;
	}

	protected void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
	}
/**
	public void testScenario01() {
		// Bind the adventure "name" property to a text field
		// Change the UI and verify the model changes
		// Change the model and verify the UI changes
		final Text text = new Text(getComposite(), SWT.BORDER);
		getDbc().bind(text, new Property(adventure, "name"), null);
		assertEquals(adventure.getName(), text.getText());
		text.setText("England");
		text.notifyListeners(SWT.FocusOut, null);		
		assertEquals("England", adventure.getName());
		adventure.setName("France");
		assertEquals("France", text.getText());
		invokeNonUI(new Runnable(){
			public void run(){
				adventure.setName("Germany");			
			}
		});
		spinEventLoop(0);
		assertEquals("Germany",text.getText());
	}
	
	public void testScenario02() {
		// Bind the transportation "price" property to a text field
		// This is a Double.TYPE so we check that conversion and validation occurs
		// Change the UI and verify the model changes
		// Change the model and verify the UI changes
		Text text = new Text(getComposite(), SWT.BORDER);
		getDbc().bind(text, new Property(transportation, "price"), null);
		assertEquals(Double.toString(transportation.getPrice()), text.getText());
		text.setText("9876.54");
		text.notifyListeners(SWT.FocusOut, null);		
		assertEquals(9876.54, transportation.getPrice(),0);
		transportation.setPrice(1234.56);
		assertEquals("1234.56", text.getText());
	}	
	
	public void testScenario03(){
		// Show that the Escape key can be pressed in the middle of editing and the value will revert
		// the updatePolicy for this test is TIME_LATE so it occurs when focus is lost from the Text control 
		getSWTUpdatableFactory().setUpdateTime(SWTUpdatableFactory.TIME_LATE);		
		final Text text = new Text(getComposite(), SWT.BORDER);
		getDbc().bind(text, new Property(adventure, "name"), null);
		String currentText = text.getText();
		text.setText("Switzerland");
		// We do not notify FocusOut
		// Verify that the model hasn't changed
		assertEquals(currentText,adventure.getName());
		Event event = new Event();
		event.character = SWT.ESC;
		event.keyCode = 27;
		text.notifyListeners(SWT.KeyDown,event);
		// Verify that the text has reverted
		assertEquals(currentText,text.getText());
		// And that the model didn't change
		assertEquals(adventure.getName(),currentText);
		// Now change the GUI and commit this change
		currentText = "Austria";
		text.setText(currentText);
		text.notifyListeners(SWT.FocusOut,null);
		assertEquals(text.getText(),adventure.getName());
		// Now change the text again and press escape a second time
		text.setText("Turkey");
		// Send escape
		text.notifyListeners(SWT.KeyDown,event);
		// Verify it has reverted to "Austria" and not any other value, i.e. the last value it displayed
		assertEquals(currentText,text.getText());
		
	}
**/
	public void testScenario04(){
		// Show that the Escape key can be pressed in the middle of editing and the value will revert
		// the updatePolicy for this test is TIME_EARLY so it occurs when each keystroke occurs 
		getSWTUpdatableFactory().setUpdateTime(SWTUpdatableFactory.TIME_EARLY);	
		final Text text = new Text(getComposite(), SWT.BORDER);
		getDbc().bind(text, new Property(adventure, "name"), null);
		String originalName = adventure.getName();
		// Change the text field character by character and ensure that the model changes
		String newName = "Switzerland";
		for (int i = 0; i < newName.length(); i++) {
			text.setText(newName.substring(0,i+1));
			// Verify the model has changed			
			assertEquals(newName.substring(0,i+1),adventure.getName());
		}
		// Now send an escape key and verify that the model reverts
		Event event = new Event();
		event.character = SWT.ESC;
		event.keyCode = 27;
		text.notifyListeners(SWT.KeyDown,event);
		assertEquals(adventure.getName(),originalName);
		// Now send "Austria" key by key
		newName = "Austria";
		for (int i = 0; i < newName.length(); i++) {
			text.setText(newName.substring(0,i+1));
			// Verify the model has changed			
			assertEquals(newName.substring(0,i+1),adventure.getName());
		}
		// Send a focus lost event to commit the change
		text.notifyListeners(SWT.FocusOut,null);
		// Send an escape key	
		text.notifyListeners(SWT.KeyDown,event);		
		// Verify that the model has changed and has not reverted
		assertEquals(newName,adventure.getName());		
	}
}
