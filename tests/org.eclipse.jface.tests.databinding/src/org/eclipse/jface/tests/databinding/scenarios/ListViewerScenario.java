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
 *     Brad Reynolds - bugs 116920, 160000
 *     Matthew Hall - bugs 260329, 260337
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.Catalog;
import org.eclipse.jface.examples.databinding.model.Lodging;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class ListViewerScenario extends ScenariosTestCase {

	private Catalog catalog;

	private List list;

	private ListViewer listViewer;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		list = new List(getComposite(), SWT.READ_ONLY | SWT.SINGLE);
		listViewer = new ListViewer(list);
		catalog = SampleData.CATALOG_2005; // Lodging source
	}

	@After
	@Override
	public void tearDown() throws Exception {
		list.dispose();
		list = null;
		listViewer = null;
		super.tearDown();
	}

	@Test
	public void testScenario01() {
		// Bind the catalog's lodgings to the combo

		IObservableList lodgings = BeanProperties.list("lodgings").observe(catalog);
		ViewerSupport.bind(listViewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		// Verify that the combo's items are the lodgings
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			assertEquals(catalog.getLodgings()[i], listViewer.getElementAt(i));
		}
		// Verify that the String being shown in the list viewer is the
		// "toString" of the combo viewer
		String[] lodgingStrings = new String[catalog.getLodgings().length];
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			lodgingStrings[i] = catalog.getLodgings()[i].getName();
		}
		assertArrayEquals(lodgingStrings, list.getItems());

		// Verify that the list has no selected item
		assertEquals(null, listViewer.getStructuredSelection().getFirstElement());

		// Now bind the selection of the combo to the "defaultLodging" property
		// of an adventure
		final Adventure adventure = SampleData.WINTER_HOLIDAY;

		IObservableValue selection = ViewerProperties.singleSelection().observe(listViewer);
		getDbc().bindValue(selection, BeanProperties.value("defaultLodging").observe(adventure));

		// Verify that the list selection is the default lodging
		assertEquals(listViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());

		// Change the model and verify that the list selection changes
		adventure.setDefaultLodging(SampleData.CAMP_GROUND);
		assertEquals(adventure.getDefaultLodging(), SampleData.CAMP_GROUND);
		assertEquals(listViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());

		// Change the list selection and verify that the model changes
		listViewer.getList().select(3);
		assertEquals(listViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());

		adventure.setDefaultLodging(SampleData.YOUTH_HOSTEL);
		spinEventLoop(0);
		assertEquals(listViewer.getStructuredSelection().getFirstElement(), adventure.getDefaultLodging());

	}
}
