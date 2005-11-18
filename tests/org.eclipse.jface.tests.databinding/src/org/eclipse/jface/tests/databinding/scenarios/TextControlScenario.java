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

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.PropertyDescription;
import org.eclipse.jface.tests.databinding.scenarios.model.Adventure;
import org.eclipse.jface.tests.databinding.scenarios.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class TextControlScenario extends ScenariosTestCase {

	private Adventure adventure;

	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		adventure = SampleData.WINTER_HOLIDAY;
	}

	protected void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
	}

	public void testScenario01() throws BindingException {
		// Bind the adventure "name" property to a text field
		// Change the UI and verify the model changes
		// Change the model and verify the UI changes
		Text text = new Text(getComposite(), SWT.BORDER);
		getDbc().bind(text, new PropertyDescription(adventure, "name"), null);
		assertEquals(adventure.getName(), text.getText());
		text.setText("England");
		text.notifyListeners(SWT.FocusOut, null);		
		assertEquals("England", adventure.getName());
		adventure.setName("France");
		assertEquals("France", text.getText());
	}
}
