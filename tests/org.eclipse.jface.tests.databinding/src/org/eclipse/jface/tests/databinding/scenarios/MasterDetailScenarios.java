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
 *     Brad Reynolds - bug 160000
 *     Matthew Hall - bugs 260329, 260337
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.Catalog;
import org.eclipse.jface.examples.databinding.model.Category;
import org.eclipse.jface.examples.databinding.model.Lodging;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class MasterDetailScenarios extends ScenariosTestCase {

	protected Object getViewerSelection(ContentViewer contentViewer) {
		return ((IStructuredSelection) contentViewer.getSelection()).getFirstElement();
	}

	/**
	 * @return the ComboViewer's domain object list
	 */
	protected List<Object> getViewerContent(ContentViewer contentViewer) {
		Object[] elements = ((IStructuredContentProvider) contentViewer.getContentProvider()).getElements(null);
		if (elements != null) {
			return Arrays.asList(elements);
		}
		return null;
	}

	@Test
	public void testScenario01() {
		// Displaying the catalog's list of Lodging objects in a list viewer,
		// using their names. The name of the currently selected Lodging can
		// be edited in a text widget. There is always a selected Lodging
		// object.
		ListViewer listViewer = new ListViewer(getComposite(), SWT.BORDER);
		Realm realm = DisplayRealm.getRealm(listViewer.getControl().getDisplay());
		listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		Catalog catalog = SampleData.CATALOG_2005;

		IObservableList<Lodging> lodgings = BeanProperties.list("lodgings", Lodging.class).observe(realm, catalog);
		ViewerSupport.bind(listViewer, lodgings, BeanProperties.value(Lodging.class, "name"));

		assertArrayEquals(catalog.getLodgings(), getViewerContent(listViewer).toArray());

		IObservableValue<Lodging> selectedLodging = ViewerProperties.singleSelection(Lodging.class).observe(listViewer);

		selectedLodging.setValue(SampleData.CAMP_GROUND);

		assertEquals(SampleData.CAMP_GROUND, getViewerSelection(listViewer));
		Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(txtName),
				BeanProperties.value(Lodging.class, "name", String.class).observeDetail(selectedLodging));

		assertEquals(txtName.getText(), SampleData.CAMP_GROUND.getName());
		enterText(txtName, "foobar");
		assertEquals("foobar", SampleData.CAMP_GROUND.getName());
		listViewer.setSelection(new StructuredSelection(SampleData.FIVE_STAR_HOTEL));
		assertEquals(SampleData.FIVE_STAR_HOTEL, selectedLodging.getValue());
		assertEquals(SampleData.FIVE_STAR_HOTEL.getName(), txtName.getText());
		SampleData.FIVE_STAR_HOTEL.setName("barfoo");
		assertEquals("barfoo", txtName.getText());

		// Now make sure that the event listeners get removed on dispose()
		// Values should no longer be updated
		selectedLodging.dispose();

		// selectedLodging.setValue(SampleData.CAMP_GROUND);
		// assertNotSame(SampleData.CAMP_GROUND,
		// getViewerSelection(listViewer));
		// assertNotSame(txtName.getText(), SampleData.CAMP_GROUND.getName());
		// enterText(txtName, "foobar");
		// assertNotSame("foobar", SampleData.CAMP_GROUND.getName());
		// listViewer.setSelection(new StructuredSelection(
		// SampleData.FIVE_STAR_HOTEL));
		// assertNotSame(SampleData.FIVE_STAR_HOTEL,
		// selectedLodging.getValue());
		// assertNotSame(SampleData.FIVE_STAR_HOTEL.getName(),
		// txtName.getText());
		// SampleData.FIVE_STAR_HOTEL.setName("barfoo");
		// assertNotSame("barfoo", txtName.getText());
	}

	@Test
	public void testScenario02() {
		// Selecting from the list of lodgings for an adventure and editing the
		// properties of the selected lodging in text widgets. If no lodging is
		// selected the input controls for name and adventure are disabled.
		// There are two buttons "Add" and "Remove"; clicking on "Add" creates a
		// new lodging and selects it so it can be edited, clicking on "Remove"
		// removes the currently selected lodging from the list.
		ListViewer listViewer = new ListViewer(getComposite(), SWT.BORDER);
		listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		Catalog catalog = SampleData.CATALOG_2005;

		IObservableList<Lodging> lodgings = BeanProperties.list("lodgings", Lodging.class).observe(realm, catalog);
		ViewerSupport.bind(listViewer, lodgings, BeanProperties.value(Lodging.class, "name"));

		assertArrayEquals(catalog.getLodgings(), getViewerContent(listViewer).toArray());

		IObservableValue<Lodging> selectedLodgingObservable = ViewerProperties.singleSelection(Lodging.class)
				.observe(listViewer);

		selectedLodgingObservable.setValue(null);
		assertTrue(listViewer.getStructuredSelection().isEmpty());

		IObservableValue<Boolean> selectionExistsObservable = ComputedValue
				.create(() -> selectedLodgingObservable.getValue() != null);

		assertFalse(selectionExistsObservable.getValue());

		Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(WidgetProperties.enabled().observe(txtName), selectionExistsObservable);
		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(txtName),
				BeanProperties.value(Lodging.class, "name").observeDetail(selectedLodgingObservable));

		assertEquals(txtName.getText(), "");
		assertFalse(txtName.getEnabled());

		Text txtDescription = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(WidgetProperties.enabled().observe(txtDescription), selectionExistsObservable);
		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(txtDescription),
				BeanProperties.value("description", String.class).observeDetail(selectedLodgingObservable));

		assertEquals(txtDescription.getText(), "");
		assertFalse(txtDescription.getEnabled());

		Button addButton = new Button(getComposite(), SWT.PUSH);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Lodging selectedLodging = selectedLodgingObservable.getValue();
				int insertionIndex = 0;
				if (selectedLodging != null) {
					insertionIndex = Arrays.asList(catalog.getLodgings()).indexOf(selectedLodging);
					assertTrue(insertionIndex >= 0);
				}
				Lodging newLodging = SampleData.FACTORY.createLodging();
				int itemCount = listViewer.getList().getItemCount();
				newLodging.setName("new lodging name " + itemCount);
				newLodging.setDescription("new lodging description " + itemCount);
				catalog.addLodging(newLodging);
				assertEquals(itemCount + 1, listViewer.getList().getItemCount());
				listViewer.setSelection(new StructuredSelection(newLodging));
				assertSame(newLodging, selectedLodgingObservable.getValue());
				assertTrue(txtName.getEnabled());
				assertTrue(txtDescription.getEnabled());
				assertEquals(newLodging.getName(), txtName.getText());
				assertEquals(newLodging.getDescription(), txtDescription.getText());
			}

		});

		Button removeButton = new Button(getComposite(), SWT.PUSH);
		removeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Lodging selectedLodging = selectedLodgingObservable.getValue();
				assertNotNull(selectedLodging);
				int deletionIndex = Arrays.asList(catalog.getLodgings()).indexOf(selectedLodging);
				assertTrue(deletionIndex >= 0);
				int itemCount = listViewer.getList().getItemCount();
				catalog.removeLodging(selectedLodging);
				assertEquals(itemCount - 1, listViewer.getList().getItemCount());
				assertNull(selectedLodgingObservable.getValue());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		pushButtonWithEvents(addButton);
		pushButtonWithEvents(removeButton);
		pushButtonWithEvents(addButton);
		pushButtonWithEvents(addButton);
		pushButtonWithEvents(removeButton);
	}

	@Test
	public void testScenario03() {
		// List adventures and for the selected adventure allow its default
		// lodging's name and description to be changed in text controls. If
		// there is no selected adventure or the default lodging is null the
		// text controls are disabled. This is a nested property. The default
		// lodging can be changed elsewhere, and the list
		Catalog catalog = SampleData.CATALOG_2005;

		ListViewer categoryListViewer = new ListViewer(getComposite(), SWT.BORDER);
		categoryListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		IObservableList<Category> categories = BeanProperties.list("categories", Category.class).observe(realm,
				catalog);
		ViewerSupport.bind(categoryListViewer, categories, BeanProperties.value(Category.class, "name"));

		assertArrayEquals(catalog.getCategories(), getViewerContent(categoryListViewer).toArray());

		IObservableValue<Category> selectedCategoryObservable = ViewerProperties.singleSelection(Category.class)
				.observe(categoryListViewer);

		ListViewer adventureListViewer = new ListViewer(getComposite(), SWT.BORDER);
		adventureListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		IObservableList<Adventure> adventures = BeanProperties.list("adventures", Adventure.class)
				.observeDetail(selectedCategoryObservable);
		ViewerSupport.bind(adventureListViewer, adventures, BeanProperties.value(Adventure.class, "name"));

		IObservableValue<Boolean> categorySelectionExistsObservable = ComputedValue
				.create(() -> selectedCategoryObservable.getValue() != null);

		getDbc().bindValue(WidgetProperties.enabled().observe(adventureListViewer.getList()),
				categorySelectionExistsObservable);

		IObservableValue<Adventure> selectedAdventureObservable = ViewerProperties.singleSelection(Adventure.class)
				.observe(adventureListViewer);

		IObservableValue<Boolean> adventureSelectionExistsObservable = ComputedValue
				.create(() -> selectedAdventureObservable.getValue() != null);

		Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(WidgetProperties.enabled().observe(txtName), adventureSelectionExistsObservable);
		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(txtName),
				BeanProperties.value("name", String.class).observeDetail(selectedAdventureObservable));

		assertEquals(txtName.getText(), "");
		assertFalse(txtName.getEnabled());

		Text txtDescription = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(WidgetProperties.enabled().observe(txtDescription), adventureSelectionExistsObservable);
		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(txtDescription),
				BeanProperties.value("description", String.class).observeDetail(selectedAdventureObservable));

		assertFalse(adventureListViewer.getList().isEnabled());
		categoryListViewer.setSelection(new StructuredSelection(SampleData.SUMMER_CATEGORY));
		assertTrue(adventureListViewer.getList().isEnabled());
		assertFalse(txtName.getEnabled());
		adventureListViewer.setSelection(new StructuredSelection(SampleData.RAFTING_HOLIDAY));
		assertTrue(adventureSelectionExistsObservable.getValue());
		assertTrue(txtName.getEnabled());
		assertEquals(SampleData.RAFTING_HOLIDAY.getName(), txtName.getText());
		categoryListViewer.setSelection(new StructuredSelection(SampleData.WINTER_CATEGORY));
		assertTrue(adventureListViewer.getList().isEnabled());
		assertFalse(txtName.getEnabled());
	}
}
