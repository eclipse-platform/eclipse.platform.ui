/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 160000
 *     Matthew Hall - bugs 260329, 260337
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class MasterDetailScenarios extends ScenariosTestCase {

	protected Object getViewerSelection(ContentViewer contentViewer) {
		return ((IStructuredSelection) contentViewer.getSelection())
				.getFirstElement();
	}

	/**
	 * @return the ComboViewer's domain object list
	 */
	protected List getViewerContent(ContentViewer contentViewer) {
		Object[] elements = ((IStructuredContentProvider) contentViewer
				.getContentProvider()).getElements(null);
		if (elements != null)
			return Arrays.asList(elements);
		return null;
	}

	public void testScenario01() {
		// Displaying the catalog's list of Lodging objects in a list viewer,
		// using their names. The name of the currently selected Lodging can
		// be edited in a text widget. There is always a selected Lodging
		// object.
		ListViewer listViewer = new ListViewer(getComposite(), SWT.BORDER);
		Realm realm = SWTObservables.getRealm(listViewer.getControl()
				.getDisplay());
		listViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, false));
		Catalog catalog = SampleData.CATALOG_2005;

		IObservableList lodgings = BeansObservables.observeList(realm, catalog,
				"lodgings");
		ViewerSupport.bind(listViewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		assertArrayEquals(catalog.getLodgings(), getViewerContent(listViewer)
				.toArray());

		IObservableValue selectedLodging = ViewersObservables
				.observeSingleSelection(listViewer);

		selectedLodging.setValue(SampleData.CAMP_GROUND);

		assertEquals(SampleData.CAMP_GROUND, getViewerSelection(listViewer));
		Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(
				SWTObservables.observeText(txtName, SWT.Modify),
				BeansObservables.observeDetailValue(selectedLodging, "name",
						String.class));

		assertEquals(txtName.getText(), SampleData.CAMP_GROUND.getName());
		enterText(txtName, "foobar");
		assertEquals("foobar", SampleData.CAMP_GROUND.getName());
		listViewer.setSelection(new StructuredSelection(
				SampleData.FIVE_STAR_HOTEL));
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

	public void testScenario02() {
		// Selecting from the list of lodgings for an adventure and editing the
		// properties of the selected lodging in text widgets. If no lodging is
		// selected the input controls for name and adventure are disabled.
		// There are two buttons "Add" and "Remove"; clicking on "Add" creates a
		// new lodging and selects it so it can be edited, clicking on "Remove"
		// removes the currently selected lodging from the list.
		final ListViewer listViewer = new ListViewer(getComposite(), SWT.BORDER);
		listViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, false));
		final Catalog catalog = SampleData.CATALOG_2005;

		IObservableList lodgings = BeansObservables.observeList(realm, catalog,
				"lodgings");
		ViewerSupport.bind(listViewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		assertArrayEquals(catalog.getLodgings(), getViewerContent(listViewer)
				.toArray());

		final IObservableValue selectedLodgingObservable = ViewersObservables
				.observeSingleSelection(listViewer);

		selectedLodgingObservable.setValue(null);
		assertTrue(listViewer.getSelection().isEmpty());

		ComputedValue selectionExistsObservable = new ComputedValue(
				boolean.class) {
			protected Object calculate() {
				return new Boolean(selectedLodgingObservable.getValue() != null);
			}
		};

		assertFalse(((Boolean) selectionExistsObservable.getValue())
				.booleanValue());

		final Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(SWTObservables.observeEnabled(txtName),
				selectionExistsObservable);
		getDbc().bindValue(
				SWTObservables.observeText(txtName, SWT.Modify),
				BeansObservables.observeDetailValue(selectedLodgingObservable,
						"name", String.class));

		assertEquals(txtName.getText(), "");
		assertFalse(txtName.getEnabled());

		final Text txtDescription = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(SWTObservables.observeEnabled(txtDescription),
				selectionExistsObservable);
		getDbc().bindValue(
				SWTObservables.observeText(txtDescription, SWT.Modify),
				MasterDetailObservables.detailValue(selectedLodgingObservable,
						BeansObservables.valueFactory(realm, "description"),
						String.class));

		assertEquals(txtDescription.getText(), "");
		assertFalse(txtDescription.getEnabled());

		Button addButton = new Button(getComposite(), SWT.PUSH);
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				Lodging selectedLodging = (Lodging) selectedLodgingObservable
						.getValue();
				int insertionIndex = 0;
				if (selectedLodging != null) {
					insertionIndex = Arrays.asList(catalog.getLodgings())
							.indexOf(selectedLodging);
					assertTrue(insertionIndex >= 0);
				}
				Lodging newLodging = SampleData.FACTORY.createLodging();
				int itemCount = listViewer.getList().getItemCount();
				newLodging.setName("new lodging name " + itemCount);
				newLodging.setDescription("new lodging description "
						+ itemCount);
				catalog.addLodging(newLodging);
				assertEquals(itemCount + 1, listViewer.getList().getItemCount());
				listViewer.setSelection(new StructuredSelection(newLodging));
				assertSame(newLodging, selectedLodgingObservable.getValue());
				assertTrue(txtName.getEnabled());
				assertTrue(txtDescription.getEnabled());
				assertEquals(newLodging.getName(), txtName.getText());
				assertEquals(newLodging.getDescription(), txtDescription
						.getText());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Button removeButton = new Button(getComposite(), SWT.PUSH);
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				Lodging selectedLodging = (Lodging) selectedLodgingObservable
						.getValue();
				assertNotNull(selectedLodging);
				int deletionIndex = Arrays.asList(catalog.getLodgings())
						.indexOf(selectedLodging);
				assertTrue(deletionIndex >= 0);
				int itemCount = listViewer.getList().getItemCount();
				catalog.removeLodging(selectedLodging);
				assertEquals(itemCount - 1, listViewer.getList().getItemCount());
				assertNull(selectedLodgingObservable.getValue());
			}

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

	public void testScenario03() {
		// List adventures and for the selected adventure allow its default
		// lodging�s name and description to be changed in text controls. If
		// there is no selected adventure or the default lodging is null the
		// text controls are disabled. This is a nested property. The default
		// lodging can be changed elsewhere, and the list
		final Catalog catalog = SampleData.CATALOG_2005;

		final ListViewer categoryListViewer = new ListViewer(getComposite(),
				SWT.BORDER);
		categoryListViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, false));

		IObservableList categories = BeansObservables.observeList(realm,
				catalog, "categories");
		ViewerSupport.bind(categoryListViewer, categories, BeanProperties
				.value(Category.class, "name"));

		assertArrayEquals(catalog.getCategories(), getViewerContent(
				categoryListViewer).toArray());

		final IObservableValue selectedCategoryObservable = ViewersObservables
				.observeSingleSelection(categoryListViewer);

		final ListViewer adventureListViewer = new ListViewer(getComposite(),
				SWT.BORDER);
		adventureListViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, false));

		IObservableList adventures = BeansObservables.observeDetailList(
				selectedCategoryObservable, "adventures", Adventure.class);
		ViewerSupport.bind(adventureListViewer, adventures, BeanProperties
				.value(Adventure.class, "name"));

		ComputedValue categorySelectionExistsObservable = new ComputedValue() {
			protected Object calculate() {
				return new Boolean(
						selectedCategoryObservable.getValue() != null);
			}
		};

		getDbc().bindValue(
				SWTObservables.observeEnabled(adventureListViewer.getList()),
				categorySelectionExistsObservable);

		final IObservableValue selectedAdventureObservable = ViewersObservables
				.observeSingleSelection(adventureListViewer);

		ComputedValue adventureSelectionExistsObservable = new ComputedValue() {
			protected Object calculate() {
				return new Boolean(
						selectedAdventureObservable.getValue() != null);
			}
		};

		final Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(SWTObservables.observeEnabled(txtName),
				adventureSelectionExistsObservable);
		getDbc().bindValue(
				SWTObservables.observeText(txtName, SWT.Modify),
				BeansObservables.observeDetailValue(
						selectedAdventureObservable, "name", String.class));

		assertEquals(txtName.getText(), "");
		assertFalse(txtName.getEnabled());

		final Text txtDescription = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(SWTObservables.observeEnabled(txtDescription),
				adventureSelectionExistsObservable);
		getDbc().bindValue(
				SWTObservables.observeText(txtDescription, SWT.Modify),
				BeansObservables.observeDetailValue(
						selectedAdventureObservable, "description",
						String.class));

		assertFalse(adventureListViewer.getList().isEnabled());
		categoryListViewer.setSelection(new StructuredSelection(
				SampleData.SUMMER_CATEGORY));
		assertTrue(adventureListViewer.getList().isEnabled());
		assertFalse(txtName.getEnabled());
		adventureListViewer.setSelection(new StructuredSelection(
				SampleData.RAFTING_HOLIDAY));
		assertEquals(Boolean.TRUE, adventureSelectionExistsObservable
				.getValue());
		assertTrue(txtName.getEnabled());
		assertEquals(SampleData.RAFTING_HOLIDAY.getName(), txtName.getText());
		categoryListViewer.setSelection(new StructuredSelection(
				SampleData.WINTER_CATEGORY));
		assertTrue(adventureListViewer.getList().isEnabled());
		assertFalse(txtName.getEnabled());
	}
}
