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
import org.eclipse.jface.tests.databinding.scenarios.model.Adventure;
import org.eclipse.jface.tests.databinding.scenarios.model.Catalog;
import org.eclipse.jface.tests.databinding.scenarios.model.SampleData;
import org.eclipse.jface.tests.databinding.scenarios.model.Transportation;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class ComboViewerScenario extends ScenariosTestCase {

	private Adventure adventure;
	private Transportation transportation;
	private Catalog catalog;
	private Combo combo;
	private ComboViewer comboViewer;

	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		combo = new Combo(getComposite(), SWT.READ_ONLY | SWT.DROP_DOWN);
		comboViewer = new ComboViewer(combo);		
		adventure = SampleData.WINTER_HOLIDAY;
		transportation = SampleData.EXECUTIVE_JET;
		catalog = SampleData.CATALOG_2005; // Lodging source		
	}

	protected void tearDown() throws Exception {
		combo.dispose();
		combo = null;
		comboViewer = null;
		super.tearDown();
	}

	public void testScenario01() {
		// Bind the catalog's lodgings to the combo
		getDbc().bind(comboViewer, new Property(catalog, "lodgings"),null);
		// Verify that the combo's items are the lodgings
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			assertEquals(catalog.getLodgings()[i],comboViewer.getElementAt(i));
		}
		// Verify that the String being shown in the combo viewer is the "toString" of the combo viewer
		String[] lodgingStrings = new String[catalog.getLodgings().length];		
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			lodgingStrings[i] = catalog.getLodgings()[i].toString();
		}
		assertArrayEquals(lodgingStrings,combo.getItems());
	}
}
