/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 *******************************************************************************/
package org.eclipse.ui.tests.propertysheet;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.SelectionProviderView;
import org.eclipse.ui.tests.api.SaveableMockViewPart;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *  The class implements a test for the workbench's default
 * property sheet page. It does this by firing a sequence of
 * selection events. The property sheet view receives these events
 * and displays the properties for the selected objects.
 * We are able to test the property sheet code which displays
 * properties and handles the transition to another set of
 * properties.
 */

@RunWith(JUnit4.class)
public class PropertySheetAuto extends UITestCase {

	/**
	 * This car serves as a simple porperty source.
	 * The only interesting behavior it has is that if
	 * one of its properties has a "null" value then
	 * it does not include that property in its list
	 * of property descriptors.
	 */
	private static class Car implements IPropertySource {
		private int modelYear = 0;

		private RGB color = null;

		private String manufacturer = null;

		private String model = null;

		private double engineSize = 0.0;

		// property ids
		private static final String prefix = "org.eclipse.ui.tests.standardcomponents.propertysheetauto.";

		private static final String MODEL_YEAR = prefix + "modelyear";

		private static final String COLOR = prefix + "color";

		private static final String MANUFACTURER = prefix + "manufacturer";

		private static final String MODEL = prefix + "model";

		private static final String ENGINE_SIZE = prefix + "enginesize";

		private IPropertyDescriptor[] descriptors;

		public Car(int carModelYear, RGB carColor, String carManufacturer,
				String carModel, double carEngineSize) {
			modelYear = carModelYear;
			color = carColor;
			manufacturer = carManufacturer;
			model = carModel;
			engineSize = carEngineSize;

			createDescriptors();
		}

		/**
		 * Creates the property descriptors.
		 * If one of the properties has a "null" value then
		 * that property is not included in the list of
		 * property descriptors.
		 */
		private void createDescriptors() {
			ArrayList<IPropertyDescriptor> list = new ArrayList<>(5);
			if (modelYear != 0) {
				list.add(new TextPropertyDescriptor(MODEL_YEAR, "model year"));
			}
			if (color != null) {
				list.add(new ColorPropertyDescriptor(COLOR, "color"));
			}
			if (manufacturer != null) {
				list.add(new TextPropertyDescriptor(MANUFACTURER, "make"));
			}
			if (model != null) {
				list.add(new TextPropertyDescriptor(MODEL, "model"));
			}
			if (engineSize != 0.0) {
				list.add(new TextPropertyDescriptor(ENGINE_SIZE, "engine"));
			}
			descriptors = list.toArray(new IPropertyDescriptor[list.size()]);
		}

		@Override
		public Object getEditableValue() {
			return this;
		}

		@Override
		public IPropertyDescriptor[] getPropertyDescriptors() {
			return descriptors;
		}

		@Override
		public Object getPropertyValue(Object id) {
			if (id.equals(MODEL_YEAR)) {
				return Integer.toString(modelYear);
			}
			if (id.equals(COLOR)) {
				return color;
			}
			if (id.equals(MANUFACTURER)) {
				return manufacturer;
			}
			if (id.equals(MODEL)) {
				return model;
			}
			if (id.equals(ENGINE_SIZE)) {
				return Double.toString(engineSize);
			}
			return null;
		}

		@Override
		public boolean isPropertySet(Object id) {
			return false;
		}

		@Override
		public void resetPropertyValue(Object id) {
			return;
		}

		@Override
		public void setPropertyValue(Object id, Object value) {
			if (id.equals(MODEL_YEAR)) {
				modelYear = Integer.parseInt((String) value);
			}
			if (id.equals(COLOR)) {
				color = (RGB) value;
			}
			if (id.equals(MANUFACTURER)) {
				manufacturer = (String) value;
			}
			if (id.equals(MODEL)) {
				model = (String) value;
			}
			if (id.equals(ENGINE_SIZE)) {
				engineSize = Double.parseDouble((String) value);
			}
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("<");
			if (modelYear != 0) {
				s.append(modelYear);
				s.append(" ");
			}
			if (color != null) {
				s.append(color);
				s.append(" ");
			}
			if (manufacturer != null) {
				s.append(manufacturer);
				s.append(" ");
			}
			if (model != null) {
				s.append(model);
				s.append(" ");
			}
			if (engineSize != 0.0) {
				s.append(engineSize);
				s.append(" ");
			}
			s.append(">");
			return s.toString();
		}
	}

	private IWorkbenchPage activePage;

	private IWorkbenchWindow workbenchWindow;

	private SelectionProviderView selectionProviderView;

	private Car[] cars;

	private final Random random = new Random();

	private static final int NUMBER_OF_CARS = 10;

	private static final int NUMBER_OF_SELECTIONS = 100;

	private static final String[] makers = new String[] { "Ford", "GM",
			"Chrysler", "BMW", "Toyota", "Nissan", "Honda", "Volvo" };

	private static final String[] models = new String[] { "Thunderbird",
			"Deville", "Viper", "320i", "Camry", "Ultima", "Prelude", "V70" };

	public PropertySheetAuto() {
		super(PropertySheetAuto.class.getSimpleName());
	}

	/**
	 * Creates a array of car objects
	 */
	private void createCars() {
		cars = new Car[NUMBER_OF_CARS];
		for (int i = 0; i < cars.length; i++) {
			cars[i] = createCar();
		}
	}

	/**
	 * Creates a car initialized with random values
	 */
	private Car createCar() {
		int modelYear = 0;
		RGB color = null;
		String manufacturer = null;
		String model = null;
		double engineSize = 0.0;
		// only set 25% of the properties
		int FACTOR = 4;
		if (random.nextInt(FACTOR) < FACTOR - 1) {
			modelYear = 1990 + random.nextInt(15);
		}
		if (random.nextInt(FACTOR) < FACTOR - 1) {
			color = new RGB(random.nextInt(256), random.nextInt(256), random
					.nextInt(256));
		}
		if (random.nextInt(FACTOR) < FACTOR - 1) {
			manufacturer = makers[random.nextInt(makers.length)];
		}
		if (random.nextInt(FACTOR) < FACTOR - 1) {
			model = models[random.nextInt(models.length)];
		}
		if (random.nextInt(FACTOR) < FACTOR - 1) {
			engineSize = random.nextDouble() * 6;
		}
		return new Car(modelYear, color, manufacturer, model, engineSize);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		workbenchWindow = openTestWindow();
		activePage = workbenchWindow.getActivePage();
		processUiEvents();
	}

	protected IWorkbenchPart createTestParts(IWorkbenchPage page)
			throws Throwable {
		IViewPart view = page.showView(IPageLayout.ID_PROP_SHEET);
		selectionProviderView = (SelectionProviderView) page
				.showView(SelectionProviderView.ID);
		processUiEvents();
		return view;

	}

	/**
	 * Supply selection events with a random car selection. All of these should go to
	 * the properties view because it is visible.
	 */
	@Test
	public void testInput() throws Throwable {
		PropertySheetPerspectiveFactory.applyPerspective(activePage);
		PropertySheet propView = (PropertySheet) createTestParts(activePage);
		createCars();

		assertTrue("'Property' view should be visible", activePage.isPartVisible(propView));
		assertTrue("'Selection provider' view should be visible", activePage
				.isPartVisible(selectionProviderView));

		for (int i = 0; i < NUMBER_OF_SELECTIONS; i++) {
			// create the selection
			int numberToSelect = random.nextInt(NUMBER_OF_CARS - 2);
			ArrayList<Car> selection = new ArrayList<>(numberToSelect);
			while (selection.size() < numberToSelect) {
				int j = random.nextInt(NUMBER_OF_CARS);
				if (!selection.contains(cars[j])) {
					selection.add(cars[j]);
				}
			}
			StructuredSelection structuredSelection = new StructuredSelection(
					selection);
			// fire the selection
			selectionProviderView.setSelection(structuredSelection);
			processUiEvents();
			assertEquals(structuredSelection, propView.getShowInContext().getSelection());
		}

		// After the 'selection provider' view is closed the properties view selection
		// should be cleared
		activePage.hideView(selectionProviderView);
		processUiEvents();
		assertNull(propView.getShowInContext().getSelection());
	}

	/**
	 * Supply selection events with a random car selection. None of these should go to
	 * the properties view because it is hidden.
	 * <p>
	 * This test invalidated by the fix for
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=267425
	 * </p>
	 */
	@Test
	@Ignore
	public void XtestInputIfHiddenBug69953() throws Throwable {
		PropertySheetPerspectiveFactory2.applyPerspective(activePage);
		PropertySheet propView = (PropertySheet) createTestParts(activePage);
		createCars();

		assertFalse("'Property' view should be hidden", activePage.isPartVisible(propView));
		assertTrue("'Selection provider' view should be visible", activePage
				.isPartVisible(selectionProviderView));

		for (int i = 0; i < NUMBER_OF_SELECTIONS; i++) {
			// create the selection
			int numberToSelect = random.nextInt(NUMBER_OF_CARS - 2);
			ArrayList<Car> selection = new ArrayList<>(numberToSelect);
			while (selection.size() < numberToSelect) {
				int j = random.nextInt(NUMBER_OF_CARS);
				if (!selection.contains(cars[j])) {
					selection.add(cars[j]);
				}
			}
			StructuredSelection structuredSelection = new StructuredSelection(
					selection);
			// fire the selection
			selectionProviderView.setSelection(structuredSelection);
			processUiEvents();
			assertNull("Selection should be null in properties view", propView.getShowInContext()
					.getSelection());
		}
	}

	/**
	 * Supply selection events with a random car selection after properties view
	 * is hidden by maximizing source view. All of these selections should go to
	 * the properties view even if it is hidden. After properties view became
	 * visible again, it should show car selection from the (restored) original
	 * source view.
	 */
	@Test
	public void testInputIfHiddenByMaximizeBug509405() throws Throwable {
		PropertySheetPerspectiveFactory3.applyPerspective(activePage);
		IViewPart projectExplorer = activePage.showView(IPageLayout.ID_PROJECT_EXPLORER);
		PropertySheet propView = (PropertySheet) createTestParts(activePage);
		// project explorer hides property view, because it is in the same stack
		createCars();
		for (int i = 0; i < 10; i++) {
			// bring project explorer view to front (hides property view from
			// same stack)
			assertViewsVisibility2(propView, projectExplorer);

			// activate now selectionProviderView (to became site selection
			// provider again)
			activePage.activate(selectionProviderView);
			processUiEvents();
			activePage.toggleZoom(activePage.getReference(selectionProviderView));
			processUiEvents();

			// create the selection
			int numberToSelect = random.nextInt(NUMBER_OF_CARS - 2);
			ArrayList<Car> selection = new ArrayList<>(numberToSelect);
			while (selection.size() < numberToSelect) {
				int j = random.nextInt(NUMBER_OF_CARS);
				if (!selection.contains(cars[j])) {
					selection.add(cars[j]);
				}
			}
			StructuredSelection structuredSelection = new StructuredSelection(selection);
			// fire the selection
			selectionProviderView.setSelection(structuredSelection);
			processUiEvents();

			// props view hidden, but still tracks the selection from original
			// source part
			assertEquals(structuredSelection, propView.getShowInContext().getSelection());

			// unhide props view again
			activePage.toggleZoom(activePage.getReference(selectionProviderView));
			processUiEvents();
			assertViewsVisibility2(propView, projectExplorer);

			// props view visible again and shows the last selection from
			// original source part
			assertEquals(structuredSelection, propView.getShowInContext().getSelection());

			PropertySheetPage currentPage = (PropertySheetPage) propView.getCurrentPage();
			PropertySheetEntry propEntry = (PropertySheetEntry) currentPage.getControl().getData();
			Object[] values = propEntry.getValues();
			if (values.length == 1 && !(values[0] instanceof PropertySheetAuto.Car)) {
				// When the selection is empty, the part is set as selection on the
				// propertiesview
				values = new Object[0];
			}
			assertArrayEquals(structuredSelection.toArray(), values);
		}
	}

	/**
	 * Supply selection events with a random car selection after properties view
	 * is hidden by the another view in the same stack but the original
	 * selection source view is still visible. All of these selections should go
	 * to the properties view even if it is hidden. After properties view became
	 * visible again, it should show car selection from the (still visible)
	 * original source view.
	 */
	@Test
	public void testInputIfHidden2Bug69953() throws Throwable {
		PropertySheetPerspectiveFactory3.applyPerspective(activePage);
		PropertySheet propView = (PropertySheet) createTestParts(activePage);
		// project explorer hides property view, because it is in the same stack
		createCars();
		for (int i = 0; i < 10; i++) {
			// bring project explorer view to front (hides property view from same stack)
			IViewPart projectExplorer = activePage.showView(IPageLayout.ID_PROJECT_EXPLORER);
			assertViewsVisibility1(propView, projectExplorer);

			// activate now selectionProviderView (to became site selection provider again)
			activePage.activate(selectionProviderView);
			processUiEvents();

			// create the selection
			int numberToSelect = random.nextInt(NUMBER_OF_CARS - 2);
			ArrayList<Car> selection = new ArrayList<>(numberToSelect);
			while (selection.size() < numberToSelect) {
				int j = random.nextInt(NUMBER_OF_CARS);
				if (!selection.contains(cars[j])) {
					selection.add(cars[j]);
				}
			}
			StructuredSelection structuredSelection = new StructuredSelection(
					selection);
			// fire the selection
			selectionProviderView.setSelection(structuredSelection);
			processUiEvents();

			// props view hidden, but still tracks the selection from original source part
			assertEquals(structuredSelection, propView.getShowInContext().getSelection());

			// unhide props view again
			activePage.showView(IPageLayout.ID_PROP_SHEET);
			assertViewsVisibility2(propView, projectExplorer);

			// props view visible again and shows the last selection from original source part
			assertEquals(structuredSelection, propView.getShowInContext().getSelection());

			PropertySheetPage currentPage = (PropertySheetPage) propView.getCurrentPage();
			PropertySheetEntry propEntry = (PropertySheetEntry) currentPage.getControl().getData();
			Object[] values = propEntry.getValues();
			if (values.length == 1 && !(values[0] instanceof PropertySheetAuto.Car)) {
				// When the selection is empty, the part is set as selection on the
				// propertiesview
				values = new Object[0];
			}
			assertArrayEquals(structuredSelection.toArray(), values);
		}
	}

	/**
	 * Supply selection events with a random car selection before properties
	 * view is hidden by the another view in the same stack which can also
	 * provide selection. Switch to the another view in the same stack - now the
	 * selection from this view should NOT go to the properties view, because
	 * only one of those views can be shown at same time. After properties view
	 * became visible again, it should show car selection from the (still
	 * visible) original source view.
	 */
	@Test
	public void testInputIfHiddenAndSelectionNotChangesBug485154() throws Throwable {
		PropertySheetPerspectiveFactory3.applyPerspective(activePage);
		PropertySheet propView = (PropertySheet) createTestParts(activePage);
		processUiEvents();

		// bring project explorer view to front (hides property view from same
		// stack)
		IViewPart projectExplorer = activePage.showView(IPageLayout.ID_PROJECT_EXPLORER);
		processUiEvents();

		// bring properties view to front (hides project explorer view from same
		// stack)
		activePage.showView(IPageLayout.ID_PROP_SHEET);
		processUiEvents();

		assertViewsVisibility2(propView, projectExplorer);
		assertEquals(new StructuredSelection(), propView.getShowInContext().getSelection());

		// make sure selection view is active
		activePage.activate(selectionProviderView);
		processUiEvents();

		createCars();

		// create the selection
		int numberToSelect = random.nextInt(NUMBER_OF_CARS - 2);
		ArrayList<Car> selection = new ArrayList<>(numberToSelect);
		while (selection.size() < numberToSelect) {
			int j = random.nextInt(NUMBER_OF_CARS);
			if (!selection.contains(cars[j])) {
				selection.add(cars[j]);
			}
		}
		StructuredSelection structuredSelection = new StructuredSelection(selection);
		// fire the selection
		selectionProviderView.setSelection(structuredSelection);
		processUiEvents();

		assertEquals(structuredSelection, propView.getShowInContext().getSelection());

		for (int i = 0; i < 10; i++) {
			// activate project explorer (should hide properties view)
			activePage.activate(projectExplorer);
			processUiEvents();

			assertViewsVisibility1(propView, projectExplorer);

			// props view hidden, but still tracks the selection from original
			// source part
			assertEquals(structuredSelection, propView.getShowInContext().getSelection());

			// unhide props view again
			activePage.showView(IPageLayout.ID_PROP_SHEET);
			processUiEvents();

			assertViewsVisibility2(propView, projectExplorer);

			// props view visible again and shows the last selection from
			// original source part
			assertEquals(structuredSelection, propView.getShowInContext().getSelection());
			PropertySheetPage currentPage = (PropertySheetPage) propView.getCurrentPage();
			PropertySheetEntry propEntry = (PropertySheetEntry) currentPage.getControl().getData();
			Object[] values = propEntry.getValues();
			if (values.length == 1 && !(values[0] instanceof PropertySheetAuto.Car)) {
				// When the selection is empty, the part is set as selection on the
				// propertiesview
				values = new Object[0];
			}
			assertArrayEquals(structuredSelection.toArray(), values);
		}
	}

	private void processUiEvents() {
		while (Display.getCurrent().readAndDispatch()) {
		}
	}

	private void assertViewsVisibility1(PropertySheet propView, IViewPart projectExplorer) {
		processUiEvents();
		assertFalse("'Property' view should be hidden", activePage.isPartVisible(propView));
		assertTrue("'Project Explorer' view should be visible", activePage
				.isPartVisible(projectExplorer));
		assertTrue("'Selection provider' view should be visible", activePage
				.isPartVisible(selectionProviderView));
	}

	private void assertViewsVisibility2(PropertySheet propView, IViewPart projectExplorer) {
		processUiEvents();
		assertTrue("'Property' view should be visible", activePage.isPartVisible(propView));
		assertFalse("'Project Explorer' view should be hidden", activePage
				.isPartVisible(projectExplorer));
		assertTrue("'Selection provider' view should be visible", activePage
				.isPartVisible(selectionProviderView));
	}

	/**
	 * Tests that the Properties view provides the source part for getAdapter(ISaveablePart.class)
	 * if it's saveable.
	 * See  Bug 125386 [PropertiesView] Properties view should delegate Save back to source part
	 */
	@Test
	public void testSaveableRetargeting() throws Throwable {
		PropertySheetPerspectiveFactory.applyPerspective(activePage);
		IWorkbenchPart propView = createTestParts(activePage);
		assertNull(propView.getAdapter(ISaveablePart.class));
		IViewPart saveableView = activePage.showView(SaveableMockViewPart.ID);
		activePage.activate(propView);
		assertEquals(saveableView, propView.getAdapter(ISaveablePart.class));
	}
}

