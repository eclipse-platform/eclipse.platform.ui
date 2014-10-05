/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 116920, 160000
 *     Matthew Hall - bugs 260329, 260337
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.Catalog;
import org.eclipse.jface.examples.databinding.model.Lodging;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		list = new List(getComposite(), SWT.READ_ONLY | SWT.SINGLE);
		listViewer = new ListViewer(list);
		catalog = SampleData.CATALOG_2005; // Lodging source
	}

	@Override
	protected void tearDown() throws Exception {
		list.dispose();
		list = null;
		listViewer = null;
		super.tearDown();
	}

	public void testScenario01() {
		// Bind the catalog's lodgings to the combo
		IObservableList lodgings = BeansObservables.observeList(Realm
				.getDefault(), catalog, "lodgings");
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
		assertEquals(null, ((IStructuredSelection) listViewer.getSelection())
				.getFirstElement());

		// Now bind the selection of the combo to the "defaultLodging" property
		// of an adventure
		final Adventure adventure = SampleData.WINTER_HOLIDAY;

		IObservableValue selection = ViewersObservables
				.observeSingleSelection(listViewer);
		getDbc().bindValue(selection,
				BeansObservables.observeValue(adventure, "defaultLodging"));

		// Verify that the list selection is the default lodging
		assertEquals(((IStructuredSelection) listViewer.getSelection())
				.getFirstElement(), adventure.getDefaultLodging());

		// Change the model and verify that the list selection changes
		adventure.setDefaultLodging(SampleData.CAMP_GROUND);
		assertEquals(adventure.getDefaultLodging(), SampleData.CAMP_GROUND);
		assertEquals(((IStructuredSelection) listViewer.getSelection())
				.getFirstElement(), adventure.getDefaultLodging());

		// Change the list selection and verify that the model changes
		listViewer.getList().select(3);
		assertEquals(((IStructuredSelection) listViewer.getSelection())
				.getFirstElement(), adventure.getDefaultLodging());

		adventure.setDefaultLodging(SampleData.YOUTH_HOSTEL);
		spinEventLoop(0);
		assertEquals(((IStructuredSelection) listViewer.getSelection())
				.getFirstElement(), adventure.getDefaultLodging());

	}
}
