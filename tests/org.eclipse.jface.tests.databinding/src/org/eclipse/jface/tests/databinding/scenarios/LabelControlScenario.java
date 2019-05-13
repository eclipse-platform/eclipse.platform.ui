/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 260329
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class LabelControlScenario extends ScenariosTestCase {

	private Adventure adventure;

	private Label label;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		label = new Label(getComposite(), SWT.NONE);
		adventure = SampleData.WINTER_HOLIDAY;
	}

	@After
	@Override
	public void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
		label.dispose();
		label = null;
	}

	@Test
	public void testScenario01() {
		// Bind the adventure "name" property to a label control
		// Change the UI and verify the model and UI are the same value
		// Change the model and verify the UI changes
		getDbc().bindValue(SWTObservables.observeText(label), BeansObservables.observeValue(adventure, "name"));

		assertEquals(adventure.getName(), label.getText());
		adventure.setName("France");
		assertEquals("France", label.getText());
		adventure.setName("Climb Everest");
		spinEventLoop(0);
		assertEquals("Climb Everest", label.getText());
	}
}
