/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.databinding.observable.value.ComputedValue;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.Catalog;
import org.eclipse.jface.examples.databinding.model.Category;
import org.eclipse.jface.examples.databinding.model.Lodging;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.jface.internal.databinding.provisional.description.ListModelDescription;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.description.TableModelDescription;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTProperties;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersProperties;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
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

	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
	}

	protected void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
	}

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
		listViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, false));
		Catalog catalog = SampleData.CATALOG_2005;

		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((Lodging) element).getName();
			}
		});
		getDbc().bind(
				listViewer,
				new TableModelDescription(new Property(catalog, "lodgings"),
						new String[] { "name" }), null);

		assertArrayEquals(catalog.getLodgings(), getViewerContent(listViewer)
				.toArray());

		IObservableValue selectedLodging = (IObservableValue) getDbc()
				.createObservable(
						new Property(listViewer,
								ViewersProperties.SINGLE_SELECTION));

		selectedLodging.setValue(SampleData.CAMP_GROUND);

		assertEquals(SampleData.CAMP_GROUND, getViewerSelection(listViewer));
		Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bind(
				txtName,
				new Property(selectedLodging, "name", String.class,
						Boolean.FALSE), null);

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

		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((Lodging) element).getName();
			}
		});
		getDbc().bind(
				listViewer,
				new TableModelDescription(new Property(catalog, "lodgings"),
						new String[] { "name" }), null);

		assertArrayEquals(catalog.getLodgings(), getViewerContent(listViewer)
				.toArray());

		final IObservableValue selectedLodgingObservable = (IObservableValue) getDbc()
				.createObservable(
						new Property(listViewer,
								ViewersProperties.SINGLE_SELECTION));

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

		getDbc().bind(new Property(txtName, SWTProperties.ENABLED),
				selectionExistsObservable, null);
		getDbc().bind(
				new Property(txtName, SWTProperties.TEXT),
				new Property(selectedLodgingObservable, "name", String.class,
						Boolean.FALSE), null);

		assertEquals(txtName.getText(), "");
		assertFalse(txtName.getEnabled());

		final Text txtDescription = new Text(getComposite(), SWT.BORDER);

		getDbc().bind(new Property(txtDescription, SWTProperties.ENABLED),
				selectionExistsObservable, null);
		getDbc().bind(
				new Property(txtDescription, SWTProperties.TEXT),
				new Property(selectedLodgingObservable, "description",
						String.class, Boolean.FALSE), null);

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
		categoryListViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((Category) element).getName();
			}
		});
		getDbc().bind(
				categoryListViewer,
				new ListModelDescription(new Property(catalog, "categories"),
						"name"), null);

		assertArrayEquals(catalog.getCategories(), getViewerContent(
				categoryListViewer).toArray());

		final IObservableValue selectedCategoryObservable = (IObservableValue) getDbc()
				.createObservable(
						new Property(categoryListViewer,
								ViewersProperties.SINGLE_SELECTION));

		final ListViewer adventureListViewer = new ListViewer(getComposite(),
				SWT.BORDER);
		adventureListViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, false));
		adventureListViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((Adventure) element).getName();
			}
		});

		getDbc().bind(
				adventureListViewer,
				new ListModelDescription(new Property(
						selectedCategoryObservable, "adventures",
						Adventure.class, Boolean.TRUE), "name"), null);

		ComputedValue categorySelectionExistsObservable = new ComputedValue() {
			protected Object calculate() {
				return new Boolean(
						selectedCategoryObservable.getValue() != null);
			}
		};

		getDbc().bind(
				new Property(adventureListViewer.getList(),
						SWTProperties.ENABLED),
				categorySelectionExistsObservable, null);

		final IObservableValue selectedAdventureObservable = (IObservableValue) getDbc()
				.createObservable(
						new Property(adventureListViewer,
								ViewersProperties.SINGLE_SELECTION));

		ComputedValue adventureSelectionExistsObservable = new ComputedValue() {
			protected Object calculate() {
				return new Boolean(
						selectedAdventureObservable.getValue() != null);
			}
		};

		final Text txtName = new Text(getComposite(), SWT.BORDER);

		getDbc().bind(new Property(txtName, SWTProperties.ENABLED),
				adventureSelectionExistsObservable, null);
		getDbc().bind(
				new Property(txtName, SWTProperties.TEXT),
				new Property(selectedAdventureObservable, "name", String.class,
						Boolean.FALSE), null);

		assertEquals(txtName.getText(), "");
		assertFalse(txtName.getEnabled());

		final Text txtDescription = new Text(getComposite(), SWT.BORDER);

		getDbc().bind(new Property(txtDescription, SWTProperties.ENABLED),
				adventureSelectionExistsObservable, null);
		getDbc().bind(
				new Property(txtDescription, SWTProperties.TEXT),
				new Property(selectedAdventureObservable, "description",
						String.class, Boolean.FALSE), null);

		assertFalse(adventureListViewer.getList().isEnabled());
		categoryListViewer.setSelection(new StructuredSelection(
				SampleData.SUMMER_CATEGORY));
		assertTrue(adventureListViewer.getList().isEnabled());
		assertFalse(txtName.getEnabled());
		adventureListViewer.setSelection(new StructuredSelection(
				SampleData.RAFTING_HOLIDAY));
		assertTrue(txtName.getEnabled());
		assertEquals(SampleData.RAFTING_HOLIDAY.getName(), txtName.getText());
		categoryListViewer.setSelection(new StructuredSelection(
				SampleData.WINTER_CATEGORY));
		assertTrue(adventureListViewer.getList().isEnabled());
		assertFalse(txtName.getEnabled());
	}
}
