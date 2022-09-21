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
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 260329
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.PriceModelObject;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class CustomConverterScenarios extends ScenariosTestCase {

	private Adventure skiTrip;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		skiTrip = SampleData.WINTER_HOLIDAY;
	}

	@After
	@Override
	public void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
	}

	@Test
	public void testScenario01() {
		DataBindingContext dbc = getDbc();
		Spinner spinner_dollars = new Spinner(getComposite(), SWT.NONE);
		spinner_dollars.setMaximum(10000);
		Spinner spinner_cents = new Spinner(getComposite(), SWT.NONE);

		// The price object is a double which contains both the dollars and
		// cents
		// To allow this to be worked on with two separate spinner controls
		// (which get and set int values)
		// an intermediate object is used
		PriceModelObject priceModel = new PriceModelObject();


		dbc.bindValue(BeanProperties.value("price").observe(priceModel),
				BeanProperties.value("price").observe(skiTrip));

		dbc.bindValue(WidgetProperties.spinnerSelection().observe(spinner_dollars),
				BeanProperties.value("dollars").observe(priceModel));

		dbc.bindValue(WidgetProperties.spinnerSelection().observe(spinner_cents),
				BeanProperties.value("cents").observe(priceModel));
		// spinEventLoop(1);
		// Make sure that the selection on the spinner_dollars matches the
		// dollars of the price
		assertEquals(spinner_dollars.getSelection(), Double.valueOf(skiTrip.getPrice()).intValue());
		// Make sure that the selection on the spinner_cents matches the dollars
		// of the price
		Double doublePrice = Double.valueOf(skiTrip.getPrice());
		double cents = 100 * doublePrice.doubleValue() - 100 * doublePrice.intValue();
		assertEquals(spinner_cents.getSelection(), (int) cents);

		// Change the selection on the spinner_dollars to be $50 and make sure
		// the model is updated with the cents included
		spinner_dollars.setSelection(50);
		double expectedPrice = 50 + cents / 100;
		assertEquals(expectedPrice, skiTrip.getPrice(), 0.01);

		// Change the selection on the spinner_cents to be 27 and make sure the
		// model is updated with the dollars included
		spinner_cents.setSelection(27);
		assertEquals(50.27, skiTrip.getPrice(), 0.01);

		// Change the model to be $60.99 dollars and make sure the
		// spinner_dollars is 60 and spinner_cents is 99
		skiTrip.setPrice(60.99);
		assertEquals(60, spinner_dollars.getSelection());
		assertEquals(99, spinner_cents.getSelection());

	}

}
