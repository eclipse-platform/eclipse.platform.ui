/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 *******************************************************************************/
package org.eclipse.ui.tests.propertysheet;

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
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 *  The class implements a test for the workbench's default
 * property sheet page. It does this by firing a sequence of 
 * selection events. The property sheet view receives these events 
 * and displays the properties for the selected objects.
 * We are able to test the property sheet code which displays 
 * properties and handles the transition to another set of 
 * properties.
 */

public class PropertySheetAuto extends UITestCase {

    /**
     * This car serves as a simple porperty source.
     * The only interesting behavior it has is that if
     * one of its properties has a "null" value then
     * it does not include that property in its list
     * of property descriptors.
     */
    private class Car implements IPropertySource {
        private int modelYear = 0;

        private RGB color = null;

        private String manufacturer = null;

        private String model = null;

        private double engineSize = 0.0;

        // property ids
        private final static String prefix = "org.eclipse.ui.tests.standardcomponents.propertysheetauto.";

        private final static String MODEL_YEAR = prefix + "modelyear";

        private final static String COLOR = prefix + "color";

        private final static String MANUFACTURER = prefix + "manufacturer";

        private final static String MODEL = prefix + "model";

        private final static String ENGINE_SIZE = prefix + "enginesize";

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
            ArrayList list = new ArrayList(5);
            if (modelYear != 0)
                list.add(new TextPropertyDescriptor(MODEL_YEAR, "model year"));
            if (color != null)
                list.add(new ColorPropertyDescriptor(COLOR, "color"));
            if (manufacturer != null)
                list.add(new TextPropertyDescriptor(MANUFACTURER, "make"));
            if (model != null)
                list.add(new TextPropertyDescriptor(MODEL, "model"));
            if (engineSize != 0.0)
                list.add(new TextPropertyDescriptor(ENGINE_SIZE, "engine"));
            descriptors = (IPropertyDescriptor[]) list
                    .toArray(new IPropertyDescriptor[list.size()]);
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
            if (id.equals(MODEL_YEAR))
                return Integer.toString(modelYear);
            if (id.equals(COLOR))
                return color;
            if (id.equals(MANUFACTURER))
                return manufacturer;
            if (id.equals(MODEL))
                return model;
            if (id.equals(ENGINE_SIZE))
                return Double.toString(engineSize);
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
            if (id.equals(MODEL_YEAR))
                modelYear = new Integer((String) value).intValue();
            if (id.equals(COLOR))
                color = (RGB) value;
            if (id.equals(MANUFACTURER))
                manufacturer = (String) value;
            if (id.equals(MODEL))
                model = (String) value;
            if (id.equals(ENGINE_SIZE))
                engineSize = new Double((String) value).doubleValue();
        }

        @Override
		public String toString() {
            StringBuffer s = new StringBuffer();
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

    private Random random = new Random();

    private static final int NUMBER_OF_CARS = 10;

    private static final int NUMBER_OF_SELECTIONS = 100;

    private static final String[] makers = new String[] { "Ford", "GM",
            "Chrysler", "BMW", "Toyota", "Nissan", "Honda", "Volvo" };

    private static final String[] models = new String[] { "Thunderbird",
            "Deville", "Viper", "320i", "Camry", "Ultima", "Prelude", "V70" };

    public PropertySheetAuto(String name) {
        super(name);
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
        if (random.nextInt(FACTOR) < FACTOR - 1)
            modelYear = 1990 + random.nextInt(15);
        if (random.nextInt(FACTOR) < FACTOR - 1)
            color = new RGB(random.nextInt(256), random.nextInt(256), random
                    .nextInt(256));
        if (random.nextInt(FACTOR) < FACTOR - 1)
            manufacturer = makers[random.nextInt(makers.length)];
        if (random.nextInt(FACTOR) < FACTOR - 1)
            model = models[random.nextInt(models.length)];
        if (random.nextInt(FACTOR) < FACTOR - 1)
            engineSize = random.nextDouble() * 6;
        return new Car(modelYear, color, manufacturer, model, engineSize);
    }

    @Override
	protected void doSetUp() throws Exception {
        super.doSetUp();
        workbenchWindow = openTestWindow();
        activePage = workbenchWindow.getActivePage();
        while (Display.getCurrent().readAndDispatch())
            ;
    }

    protected IWorkbenchPart createTestParts(IWorkbenchPage page)
            throws Throwable {
        IViewPart view = page.showView(IPageLayout.ID_PROP_SHEET);
        selectionProviderView = (SelectionProviderView) page
                .showView(SelectionProviderView.ID);
        while (Display.getCurrent().readAndDispatch())
            ;
        return view;

    }

    /** 
     * Supply selection events with a random car selection. All of these should go to
     * the properties view because it is visible.
     */
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
            ArrayList selection = new ArrayList(numberToSelect);
            while (selection.size() < numberToSelect) {
                int j = random.nextInt(NUMBER_OF_CARS);
                if (!selection.contains(cars[j]))
                    selection.add(cars[j]);
            }
            StructuredSelection structuredSelection = new StructuredSelection(
                    selection);
            // fire the selection	
            selectionProviderView.setSelection(structuredSelection);
            while (Display.getCurrent().readAndDispatch())
                ;
            assertEquals(structuredSelection, propView.getShowInContext().getSelection());
        }
    }
    
    /** 
     * Supply selection events with a random car selection. None of these should go to
     * the properties view because it is hidden.
     * <p>
     * This test invalidated by the fix for 
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=267425
     * </p>
     */
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
            ArrayList selection = new ArrayList(numberToSelect);
            while (selection.size() < numberToSelect) {
                int j = random.nextInt(NUMBER_OF_CARS);
                if (!selection.contains(cars[j]))
                    selection.add(cars[j]);
            }
            StructuredSelection structuredSelection = new StructuredSelection(
                    selection);
            // fire the selection   
            selectionProviderView.setSelection(structuredSelection);
            while (Display.getCurrent().readAndDispatch())
                ;
            assertNull("Selection should be null in properties view", propView.getShowInContext()
                    .getSelection());            
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
            while (Display.getCurrent().readAndDispatch())
                ;
            
            // create the selection
            int numberToSelect = random.nextInt(NUMBER_OF_CARS - 2);
            ArrayList selection = new ArrayList(numberToSelect);
            while (selection.size() < numberToSelect) {
                int j = random.nextInt(NUMBER_OF_CARS);
                if (!selection.contains(cars[j]))
                    selection.add(cars[j]);
            }
            StructuredSelection structuredSelection = new StructuredSelection(
                    selection);
            // fire the selection
            selectionProviderView.setSelection(structuredSelection);
            while (Display.getCurrent().readAndDispatch())
                ;
            
            // props view hidden, but still tracks the selection from original source part            
            assertEquals(structuredSelection, propView.getShowInContext().getSelection());
            
            // unhide props view again
            activePage.showView(IPageLayout.ID_PROP_SHEET);            
            assertViewsVisibility2(propView, projectExplorer);

            // props view visible again and shows the last selection from original source part
            assertEquals(structuredSelection, propView.getShowInContext().getSelection());
        }
    }

    private void assertViewsVisibility1(PropertySheet propView, IViewPart projectExplorer) {
        while (Display.getCurrent().readAndDispatch())
            ;
        assertFalse("'Property' view should be hidden", activePage.isPartVisible(propView));
        assertTrue("'Project Explorer' view should be visible", activePage
                .isPartVisible(projectExplorer));
        assertTrue("'Selection provider' view should be visible", activePage
                .isPartVisible(selectionProviderView));
    }        
    
    private void assertViewsVisibility2(PropertySheet propView, IViewPart projectExplorer) {
        while (Display.getCurrent().readAndDispatch())
            ;
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
    public void testSaveableRetargeting() throws Throwable {
        PropertySheetPerspectiveFactory.applyPerspective(activePage); 
    	IWorkbenchPart propView = createTestParts(activePage);
    	assertNull(propView.getAdapter(ISaveablePart.class));
    	IViewPart saveableView = activePage.showView(SaveableMockViewPart.ID);
    	activePage.activate(propView);
    	assertEquals(saveableView, propView.getAdapter(ISaveablePart.class));
    }
}

