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
package org.eclipse.jface.tests.binding.scenarios;

import org.eclipse.jface.databinding.BindSpec;
import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.PropertyDescription;
import org.eclipse.jface.databinding.swt.SWTBindingConstants;
import org.eclipse.jface.tests.binding.scenarios.model.Adventure;
import org.eclipse.jface.tests.binding.scenarios.model.PriceCentsConverter;
import org.eclipse.jface.tests.binding.scenarios.model.PriceDollarsConverter;
import org.eclipse.jface.tests.binding.scenarios.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class CustomConverterScenarios extends ScenariosTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		// do any setup work here
	}

	protected void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
	}

	public void testScenario01() throws BindingException {

		Adventure skiTrip = SampleData.WINTER_HOLIDAY;
		Spinner spinner_dollars = new Spinner(getComposite(), SWT.NONE);
		spinner_dollars.setMaximum(10000);
		Spinner spinner_cents = new Spinner(getComposite(), SWT.NONE);

		getDbc().bind2(
				new PropertyDescription(spinner_dollars,
						SWTBindingConstants.SELECTION),
				new PropertyDescription(skiTrip, "price"),
				new BindSpec(new PriceDollarsConverter(), null));

		getDbc().bind2(
				new PropertyDescription(spinner_cents,
						SWTBindingConstants.SELECTION),
				new PropertyDescription(skiTrip, "price"),
				new BindSpec(new PriceCentsConverter(), null));
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
		assertEquals(new Double(expectedPrice), new Double(skiTrip.getPrice()));

		// Change the selection on the spinner_cents to be 27 and make sure the
		// model is updated with the dollars included
		spinner_cents.setSelection(27);
		assertEquals(new Double(50.27), new Double(skiTrip.getPrice()));

		// Change the model to be $60.99 dollars and make sure the
		// spinner_dollars is 60 and spinner_cents is 99
		skiTrip.setPrice(60.99);
		assertEquals(spinner_dollars.getSelection(), 60);
		assertEquals(spinner_cents.getSelection(), 99);

	}
}
