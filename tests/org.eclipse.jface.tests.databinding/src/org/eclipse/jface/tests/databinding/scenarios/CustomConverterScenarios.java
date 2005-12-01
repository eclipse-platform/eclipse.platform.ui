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
import org.eclipse.jface.databinding.swt.SWTProperties;
import org.eclipse.jface.tests.databinding.scenarios.model.Adventure;
import org.eclipse.jface.tests.databinding.scenarios.model.PriceModelObject;
import org.eclipse.jface.tests.databinding.scenarios.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class CustomConverterScenarios extends ScenariosTestCase {

	private Adventure skiTrip;

	protected void setUp() throws Exception {
		super.setUp();
		skiTrip = SampleData.WINTER_HOLIDAY;
	}

	protected void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
	}

	public void testScenario01() {

		Spinner spinner_dollars = new Spinner(getComposite(), SWT.NONE);
		spinner_dollars.setMaximum(10000);
		Spinner spinner_cents = new Spinner(getComposite(), SWT.NONE);
		
		// The price object is a double which contains both the dollars and cents
		// To allow this to be worked on with two separate spinner controls (which get and set int values)
		// an intermediate object is used
		PriceModelObject priceModel = new PriceModelObject();
		getDbc().bind(
				new Property(priceModel,"price"),
				new Property(skiTrip,"price"),null);

		getDbc().bind(
				new Property(spinner_dollars,
						SWTProperties.SELECTION),
				new Property(priceModel, "dollars"),null);

		getDbc().bind(
				new Property(spinner_cents,
						SWTProperties.SELECTION),
				new Property(priceModel, "cents"),null);
		// spinEventLoop(1);
		// Make sure that the selection on the spinner_dollars matches the
		// dollars of the price
		assertEquals(spinner_dollars.getSelection(), new Double(skiTrip
				.getPrice()).intValue());
		// Make sure that the selection on the spinner_cents matches the dollars
		// of the price
		Double doublePrice = new Double(skiTrip.getPrice());
		double cents = 100 * (doublePrice.doubleValue() - doublePrice
				.intValue());
		assertEquals(spinner_cents.getSelection(), (int) cents);

		// Change the selection on the spinner_dollars to be $50 and make sure
		// the model is updated with the cents included
		spinner_dollars.setSelection(50);
		double expectedPrice = 50 + cents / 100;
		assertEquals(expectedPrice, skiTrip.getPrice(),0.01);

		// Change the selection on the spinner_cents to be 27 and make sure the
		// model is updated with the dollars included
		spinner_cents.setSelection(27);
		assertEquals(50.27, skiTrip.getPrice(),0.01);

		// Change the model to be $60.99 dollars and make sure the
		// spinner_dollars is 60 and spinner_cents is 99
		skiTrip.setPrice(60.99);
		assertEquals(spinner_dollars.getSelection(), 60);
		assertEquals(spinner_cents.getSelection(), 99);

	}

}
