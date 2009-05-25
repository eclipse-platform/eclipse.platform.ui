/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 260329
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class SpinnerControlScenario extends ScenariosTestCase {

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

    public void testScenario01() {
        // Bind the adventure "maxNumberOfPeople" property to a spinner
        // Change the UI and verify the model changes
        // Change the model and verify the UI changes
        Spinner spinner = new Spinner(getComposite(), SWT.BORDER);
        getDbc().bindValue(SWTObservables.observeSelection(spinner),
                BeansObservables.observeValue(adventure, "maxNumberOfPeople"));

        assertEquals(adventure.getMaxNumberOfPeople(), spinner.getSelection());
        // Verify the model is updated when the GUI is changed
        spinner.setSelection(5);
        assertEquals(5, adventure.getMaxNumberOfPeople());
        // Verify the GUI is updated when the model changes
        adventure.setMaxNumberOfPeople(7);
        assertEquals(7, spinner.getSelection());
        adventure.setMaxNumberOfPeople(11);
        spinEventLoop(0);
        assertEquals(11, spinner.getSelection());
    }
}
