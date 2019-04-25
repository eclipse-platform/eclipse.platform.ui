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
 *     Brad Reynolds - bug 160000
 *     Matthew Hall - bugs 260329, 260337
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.Catalog;
import org.eclipse.jface.examples.databinding.model.Lodging;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class ComboViewerScenario extends ScenariosTestCase {

	private Catalog catalog;

	private Combo combo;

	private ComboViewer comboViewer;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		combo = new Combo(getComposite(), SWT.READ_ONLY | SWT.DROP_DOWN);
		comboViewer = new ComboViewer(combo);
		catalog = SampleData.CATALOG_2005; // Lodging source
	}

	@Override
	@After
	public void tearDown() throws Exception {
		combo.dispose();
		combo = null;
		comboViewer = null;
		super.tearDown();
	}

	@Test
	public void testScenario01() {
		// Bind the catalog's lodgings to the combo
		IObservableList<Lodging> lodgings = BeanProperties.list(Catalog.class, "lodgings", Lodging.class).observe(realm,
				catalog);

		ViewerSupport.bind(comboViewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		// Verify that the combo's items are the lodgings
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			assertEquals(catalog.getLodgings()[i], comboViewer.getElementAt(i));
		}
		// Verify that the String being shown in the combo viewer is the
		// "toString" of the combo viewer
		String[] lodgingStrings = new String[catalog.getLodgings().length];
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			lodgingStrings[i] = catalog.getLodgings()[i].getName();
		}
		assertArrayEquals(lodgingStrings, combo.getItems());

		// Verify that the combo has no selected item
		assertEquals(null, comboViewer.getStructuredSelection().getFirstElement());

		// Now bind the selection of the combo to the "defaultLodging" property
		// of an adventure
		final Adventure adventure = SampleData.WINTER_HOLIDAY;
		IObservableValue<Lodging> selection = ViewerProperties.singleSelection(Lodging.class).observe(comboViewer);
		getDbc().bindValue(selection, BeanProperties.value("defaultLodging").observe(adventure));

		// Verify that the combo selection is the default lodging
		assertEquals(comboViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());

		// Change the model and verify that the combo selection changes
		adventure.setDefaultLodging(SampleData.CAMP_GROUND);
		assertEquals(adventure.getDefaultLodging(), SampleData.CAMP_GROUND);
		assertEquals(comboViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());

		// Change the combo selection and verify that the model changes
		comboViewer.getCombo().select(3);
		assertEquals(comboViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());

		adventure.setDefaultLodging(SampleData.YOUTH_HOSTEL);
		spinEventLoop(0);
		assertEquals(comboViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());
	}

}
