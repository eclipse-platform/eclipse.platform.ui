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
import org.eclipse.swt.widgets.Label;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class LabelControlScenario extends ScenariosTestCase {

	private Adventure adventure;	
	private Label label;

	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		label = new Label(getComposite(), SWT.NONE);		
		adventure = SampleData.WINTER_HOLIDAY;
	}

	protected void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
		label.dispose();
		label = null;
	}

	public void testScenario01() {
		// Bind the adventure "name" property to a label control
		// Change the UI and verify the model and UI are the same value
		// Change the model and verify the UI changes
		getDbc().bind(label, new Property(adventure, "name"), null);
		assertEquals(adventure.getName(), label.getText());
		adventure.setName("France");
		assertEquals("France", label.getText());
		// Verify that the model can be changed in a non-UI thread and the SWT Label still gets updated OK
		invokeNonUI(new Runnable(){
			public void run(){
				adventure.setName("Climb Everest");
			}
		});		
		spinEventLoop(0);
		assertEquals("Climb Everest",label.getText());		
	}
}
