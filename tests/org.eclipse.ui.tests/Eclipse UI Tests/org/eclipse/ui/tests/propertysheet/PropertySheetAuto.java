/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.propertysheet;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
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

        public Object getEditableValue() {
            return this;
        }

        public IPropertyDescriptor[] getPropertyDescriptors() {
            return descriptors;
        }

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

        public boolean isPropertySet(Object id) {
            return false;
        }

        public void resetPropertyValue(Object id) {
            return;
        }

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

    protected void doSetUp() throws Exception {
        super.doSetUp();
        workbenchWindow = openTestWindow();
        activePage = workbenchWindow.getActivePage();
    }

    protected IWorkbenchPart createTestPart(IWorkbenchPage page)
            throws Throwable {
        IViewPart view = page.showView("org.eclipse.ui.views.PropertySheet");
        selectionProviderView = (SelectionProviderView) page
                .showView(SelectionProviderView.ID);
        return view;

    }

    /** 
     * Supply selection events with a random car selection
     */
    public void testInput() throws Throwable {
        createTestPart(activePage);
        createCars();
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
        }
    }
    
    /**
     * Tests that the Properties view provides the source part for getAdapter(ISaveablePart.class)
     * if it's saveable.  
     * See  Bug 125386 [PropertiesView] Properties view should delegate Save back to source part
     */
    public void testSaveableRetargeting() throws Throwable {
    	IWorkbenchPart propView = createTestPart(activePage);
    	assertNull(propView.getAdapter(ISaveablePart.class));
    	IViewPart saveableView = activePage.showView(SaveableMockViewPart.ID);
    	activePage.activate(propView);
    	assertEquals(saveableView, propView.getAdapter(ISaveablePart.class));
    }
}

