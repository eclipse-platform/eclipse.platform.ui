/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *     Brad Reynolds - bug 116920, 159768
 *     Matthew Hall - bug 260329
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.text.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.text.StringToNumberConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.conversion.IdentityConverter;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.examples.databinding.model.Account;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.Cart;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class PropertyScenarios extends ScenariosTestCase {

	private Adventure adventure;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// do any setup work here
		adventure = SampleData.WINTER_HOLIDAY;
	}

	@Override
	@After
	public void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
	}

	@Test
	public void testEnterText() {
		// just to make sure enterText() generates a FocusOut event.
		Text text = new Text(getComposite(), SWT.BORDER);
		final boolean[] focusLostHolder = { false };
		text.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// only interested in focus lost events
			}

			@Override
			public void focusLost(FocusEvent e) {
				focusLostHolder[0] = true;
			}
		});
		enterText(text, "hallo");
		assertTrue(focusLostHolder[0]);
	}

	@Test
	public void testScenario01() {
		Text text = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				BeanProperties.value("name").observe(adventure));

		// getDbc().bind(text, new Property(adventure, "name"), null);
		// uncomment the following line to see what's happening
		// spinEventLoop(1);
		assertEquals(adventure.getName(), text.getText());
		enterText(text, "foobar");
		// uncomment the following line to see what's happening
		// spinEventLoop(1);
		assertEquals("foobar", adventure.getName());
		adventure.setName("barfoo");
		// uncomment the following line to see what's happening
		// spinEventLoop(1);
		assertEquals("barfoo", text.getText());
	}

	@Test
	public void testScenario02() {
		// Binding the name property of an Adventure object to the contents of
		// Text controls, no conversion, no validation. The Text widget editable
		// is set to false.by the developer (can not change the name)
		Text text = new Text(getComposite(), SWT.READ_ONLY);

		getDbc().bindValue(WidgetProperties.text(SWT.None).observe(text),
				BeanProperties.value("name").observe(adventure));

		assertEquals(adventure.getName(), text.getText());
	}

	@Test
	public void testScenario03() {
		// Binding of a read-only property of an Adventure object to the
		// contents of Text controls, no conversion, no validation. Text control
		// is not editable as a side effect of binding to a read-only property..
		Cart cart = SampleData.CART;
		cart.setAdventureDays(42);
		// bind to the lodgingDays feature, which is read-only and always one
		// less than the number of adventure days.
		Text text = new Text(getComposite(), SWT.BORDER);

		System.out.println("Expecting message about not being able to attach a listener");


		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				BeanProperties.value("lodgingDays").observe(cart));

		assertEquals(Integer.valueOf(cart.getLodgingDays()).toString(), text.getText());
	}

	@Test
	public void testScenario04() {
		// Binding a nested property of an Adventure object to the content of a
		// Text control, no conversion, no validation.
		Text text = new Text(getComposite(), SWT.BORDER);
		// TODO Scenario needs to be more specific - I'm binding to the default
		// lodging's description of an adventure. What do we expect to happen
		// when the default lodging changes? If we expect no change, then this
		// scenario does not introduce anything new. If we expect the binding to
		// be to the new default lodging's description, shouldn't we move this
		// scenario to the master/detail section? I'm assuming the latter for
		// now.


		IObservableValue defaultLodging = BeanProperties.value("defaultLodging.description", String.class)
				.observe(adventure);

		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(text), defaultLodging);

		// test changing the description
		assertEquals(adventure.getDefaultLodging().getDescription(), text.getText());
		enterText(text, "foobar");
		assertEquals("foobar", adventure.getDefaultLodging().getDescription());
		adventure.getDefaultLodging().setDescription("barfoo");
		assertEquals(adventure.getDefaultLodging().getDescription(), text.getText());

		// test changing the default lodging
		adventure.setDefaultLodging(SampleData.CAMP_GROUND);
		assertEquals(adventure.getDefaultLodging().getDescription(), text.getText());
		adventure.getDefaultLodging().setDescription("barfo");
		assertEquals(adventure.getDefaultLodging().getDescription(), text.getText());

		adventure.setDefaultLodging(null);
		assertEquals("", text.getText());

		adventure.setDefaultLodging(SampleData.FIVE_STAR_HOTEL);
		assertEquals(adventure.getDefaultLodging().getDescription(), text.getText());
		adventure.getDefaultLodging().setDescription("barf");
		assertEquals(adventure.getDefaultLodging().getDescription(), text.getText());

	}

	@Test
	public void testScenario05() {
		// Binding the name property of an Adventure object to the contents of
		// Text controls where conversion occurs � the model data is held all
		// in
		// uppercase and displayed in lowercase with the first letter
		// capitalized.
		Text text = new Text(getComposite(), SWT.BORDER);
		adventure.setName("UPPERCASE");

		IConverter converter1 = new IConverter() {
			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object getToType() {
				return String.class;
			}

			@Override
			public Object convert(Object toObject) {
				String modelValue = (String) toObject;
				if (modelValue == null || modelValue.isEmpty()) {
					return modelValue;
				}
				String firstChar = modelValue.substring(0, 1);
				String remainingChars = modelValue.substring(1);
				return firstChar.toUpperCase() + remainingChars.toLowerCase();
			}
		};
		IConverter converter2 = new IConverter() {
			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object getToType() {
				return String.class;
			}

			@Override
			public Object convert(Object fromObject) {
				return ((String) fromObject).toUpperCase();
			}
		};


		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				BeanProperties.value("name").observe(adventure),
				UpdateValueStrategy.create(converter2), UpdateValueStrategy.create(converter1));

		// spinEventLoop(1);
		assertEquals("Uppercase", text.getText());
		enterText(text, "lowercase");
		// spinEventLoop(1);
		// TODO If we wanted to "canonicalize" the value in the text field, how
		// could we do that?
		assertEquals("LOWERCASE", adventure.getName());
	}

	@Test
	public void testScenario06() {
		// Binding the name property of an Adventure object to the contents of
		// Text controls where validation occurs and the name cannot be longer
		// than 15 characters and cannot contain spaces
		Text text = new Text(getComposite(), SWT.BORDER);
		final String noSpacesMessage = "Name must not contain spaces.";
		final String max15CharactersMessage = "Maximum length for name is 15 characters.";
		adventure.setName("ValidValue");

		IValidator validator = value -> {
			String stringValue = (String) value;
			if (stringValue.length() > 15) {
				return ValidationStatus.error(max15CharactersMessage);
			} else if (stringValue.indexOf(' ') != -1) {
				return ValidationStatus.cancel(noSpacesMessage);
			} else {
				return Status.OK_STATUS;
			}
		};

//        BindSpec bindSpec = new DefaultBindSpec().setModelToTargetConverter(new IdentityConverter(String.class))
//                .setTargetToModelConverter(new IdentityConverter(String.class))
//                .addTargetValidator(BindingEvent.PIPELINE_VALUE_CHANGING, validator);

		Binding binding = getDbc().bindValue(
				WidgetProperties.text(SWT.Modify).observe(text),
				BeanProperties.value("name").observe(adventure),
				new UpdateValueStrategy().setConverter(new IdentityConverter(
						String.class)).setAfterGetValidator(validator),
				new UpdateValueStrategy().setConverter(new IdentityConverter(
						String.class)));

		// no validation message
		assertTrue(binding.getValidationStatus().getValue().isOK());
		enterText(text, "Invalid Value");
		assertEquals(noSpacesMessage, binding.getValidationStatus().getValue().getMessage());
		assertEquals("ValidValue", adventure.getName());
		text.setText("InvalidValueBecauseTooLong");
		assertEquals(max15CharactersMessage,
				binding.getValidationStatus().getValue().getMessage());
		assertEquals("ValidValue", adventure.getName());
		enterText(text, "anothervalid");
		assertTrue(binding.getValidationStatus().getValue().isOK());
		assertEquals("anothervalid", adventure.getName());
	}

	@Test
	public void testScenario07() {
		// Binding the price property of an Adventure to a Text control. Price
		// is a double and Text accepts String so conversion will have to occur.
		// Validation ensure that the value is positive
		Text text = new Text(getComposite(), SWT.BORDER);
		adventure.setPrice(5.0);
		final String cannotBeNegativeMessage = "Price cannot be negative.";
		final String mustBeCurrencyMessage = "Price must be a currency.";

		IValidator validator = value -> {
			String stringValue = (String) value;
			try {
				double doubleValue = Double.parseDouble(stringValue);
				if (doubleValue < 0.0) {
					return ValidationStatus.error(cannotBeNegativeMessage);
				}
				return Status.OK_STATUS;
			} catch (NumberFormatException ex) {
				return ValidationStatus.error(mustBeCurrencyMessage);
			}
		};

		//Create a number formatter that will display one decimal position.
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMinimumFractionDigits(1);

		IConverter targetToModelConverter = StringToNumberConverter.toDouble(
				numberFormat, true);
		IConverter modelToTargetConverter = NumberToStringConverter.fromDouble(
				numberFormat, true);

		getDbc().bindValue(
				WidgetProperties.text(SWT.Modify).observe(text), BeanProperties.value("price").observe(adventure),
				new UpdateValueStrategy().setAfterGetValidator(validator)
						.setConverter(targetToModelConverter),
				new UpdateValueStrategy().setConverter(modelToTargetConverter));

		String expected = numberFormat.format(adventure.getPrice());
		assertEquals(expected, text.getText());
		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());

		String toEnter = numberFormat.format(0.65);
		enterText(text, toEnter);
		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());
		assertEquals(0.65, adventure.getPrice(), 0.0001);

		adventure.setPrice(42.24);
		expected = numberFormat.format(adventure.getPrice());
		assertEquals(expected, text.getText());
		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());

		enterText(text, "jygt");
		assertEquals(mustBeCurrencyMessage, AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).getMessage());

		toEnter = numberFormat.format(-23.9);
		enterText(text, toEnter);
		assertEquals(cannotBeNegativeMessage, AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).getMessage());
		assertEquals(42.24, adventure.getPrice(), 0.0001);

		adventure.setPrice(0.0);
		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());
	}

	@Test
	public void testScenario08() {
		// Binding the price property of an Adventure to a Text control but with
		// custom conversion � the double will be validated to only have two
		// decimal places and displayed with a leading currency symbol, and can
		// be entered with or without the currency symbol.
		Text text = new Text(getComposite(), SWT.BORDER);
		adventure.setPrice(5.0);
		final String cannotBeNegativeMessage = "Price cannot be negative.";
		final String mustBeCurrencyMessage = "Price must be a currency.";
		final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);

		IConverter toCurrency = new Converter(double.class, String.class) {
			@Override
			public Object convert(Object toObject) {
				return currencyFormat.format(((Double) toObject).doubleValue());
			}
		};

		IConverter toDouble = new Converter(String.class, double.class) {
			@Override
			public Object convert(Object fromObject) {
				try {
					return Double.valueOf(currencyFormat.parse((String) fromObject).doubleValue());
				} catch (ParseException e) {
					// TODO throw something like
					// IllegalConversionException?
					return Double.valueOf(0);
				}
			}
		};

		IValidator validator = value -> {
			String stringValue = (String) value;
			try {
				double doubleValue = currencyFormat.parse(stringValue).doubleValue();
				if (doubleValue < 0.0) {
					return ValidationStatus.error(cannotBeNegativeMessage);
				}
				return Status.OK_STATUS;
			} catch (ParseException e) {
				return ValidationStatus.error(mustBeCurrencyMessage);
			}
		};


		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				BeanProperties.value("price").observe(adventure),
new UpdateValueStrategy().setConverter(toDouble).setAfterGetValidator(validator),new UpdateValueStrategy().setConverter(toCurrency));

		String expected = currencyFormat.format(5);
		assertEquals(expected, text.getText());
		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());

		String toEnter = currencyFormat.format(0.65);
		enterText(text, toEnter);
		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());
		assertEquals(0.65, adventure.getPrice(), 0.0001);

		adventure.setPrice(42.24);
		expected = currencyFormat.format(adventure.getPrice());
		assertEquals(expected, text.getText());

		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());
		enterText(text, "jygt");
		assertEquals(mustBeCurrencyMessage, AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).getMessage());

		toEnter = currencyFormat.format(-23.9);
		enterText(text, toEnter);

		assertEquals(cannotBeNegativeMessage, AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).getMessage());
		assertEquals(42.24, adventure.getPrice(), 0.0001);
		adventure.setPrice(0.0);
		assertTrue(AggregateValidationStatus.getStatusMaxSeverity(getDbc().getBindings()).isOK());
	}

	@Test
	public void testScenario09() {
		// Binding a boolean property to a CheckBox. Adventure will have a
		// Boolean property �petsAllowed�
		Button checkbox = new Button(getComposite(), SWT.CHECK);
		// checkbox.setText("Pets allowed");
		// checkbox.setLayoutData(new GridData(SWT.LEFT,SWT.TOP, false,false));
		adventure.setPetsAllowed(true);


		getDbc().bindValue(WidgetProperties.buttonSelection().observe(checkbox),
				BeanProperties.value("petsAllowed").observe(adventure));

		assertEquals(true, checkbox.getSelection());
		setButtonSelectionWithEvents(checkbox, false);
		assertEquals(false, adventure.isPetsAllowed());
		adventure.setPetsAllowed(true);
		assertEquals(true, checkbox.getSelection());
	}

	@Test
	public void testScenario10() {
		// Binding a Transportation departure time to a Text control that
		// formats and validates the time to and from a String. There are
		// property bindings that bind elements of the GUI to elements to GUI
		// and also elements of the domain to elements of the domain
		// TODO fail("not implemented");
	}

	@Test
	public void testScenario11() {
		// Binding the max value of a spinner to another spinner.
		Spinner spinner1 = new Spinner(getComposite(), SWT.NONE);
		spinner1.setSelection(10);
		spinner1.setMinimum(1);
		spinner1.setMaximum(100);
		Spinner spinner2 = new Spinner(getComposite(), SWT.NONE);
		spinner2.setMaximum(1);

		getDbc().bindValue(WidgetProperties.spinnerSelection().observe(spinner1),
				WidgetProperties.maximum().observe(spinner2));

		assertEquals(1, spinner1.getSelection());
		spinner1.setSelection(10);
		spinner1.notifyListeners(SWT.Modify, new Event());
		assertEquals(10, spinner2.getMaximum());
	}

	@Test
	public void testScenario12() {
		// Binding the enabled state of several Text controls to a check box.
		// There will be two check boxes, so as each is enabled/disabled the
		// other one follows as do the states of the Text controls.
		Button checkbox1 = new Button(getComposite(), SWT.CHECK);
		checkbox1.setSelection(false);
		Button checkbox2 = new Button(getComposite(), SWT.CHECK);
		checkbox2.setSelection(false);
		Text text1 = new Text(getComposite(), SWT.NONE);
		Text text2 = new Text(getComposite(), SWT.NONE);

		IObservableValue checkbox1Selected = WidgetProperties.buttonSelection().observe(checkbox1);
		IObservableValue checkbox2Selected = WidgetProperties.buttonSelection().observe(checkbox2);

		// bind the two checkboxes so that if one is checked, the other is not
		// and vice versa.
		Converter negatingConverter = new Converter(boolean.class, boolean.class) {
			private Boolean negated(Boolean booleanObject) {
				return Boolean.valueOf(!booleanObject.booleanValue());
			}

			@Override
			public Object convert(Object targetObject) {
				return negated((Boolean) targetObject);
			}
		};

		getDbc().bindValue(checkbox1Selected,
				checkbox2Selected,new UpdateValueStrategy().setConverter(negatingConverter),
				new UpdateValueStrategy().setConverter(negatingConverter));

		// bind the enabled state of the two text widgets to one of the
		// checkboxes each.
		getDbc().bindValue(WidgetProperties.enabled().observe(text1), checkbox1Selected);
		getDbc().bindValue(WidgetProperties.enabled().observe(text2), checkbox2Selected);

		assertEquals(true, text1.getEnabled());
		assertEquals(false, text2.getEnabled());
		assertEquals(true, checkbox1.getSelection());
		setButtonSelectionWithEvents(checkbox1, false);
		assertEquals(false, text1.getEnabled());
		assertEquals(true, text2.getEnabled());
		assertEquals(true, checkbox2.getSelection());
		setButtonSelectionWithEvents(checkbox2, false);
		assertEquals(true, text1.getEnabled());
		assertEquals(false, text2.getEnabled());
		assertEquals(true, checkbox1.getSelection());
	}

	@Test
	public void testScenario13() {
		Text text = new Text(getComposite(), SWT.BORDER);
		getDbc().bindValue(WidgetProperties.text(SWT.FocusOut).observe(text), BeanProperties.value("name").observe(adventure));

		// uncomment the following line to see what's happening
		// happening
		// spinEventLoop(1);
		String adventureName = adventure.getName();
		assertEquals(adventureName, text.getText());
		enterText(text, "foobar");
		// uncomment the following line to see what's happening
		// spinEventLoop(1);
		assertEquals("foobar", adventure.getName());
		adventure.setName("barfoo");
		// uncomment the following line to see what's happening
		// spinEventLoop(1);
		assertEquals("barfoo", text.getText());
	}

	@Test
	public void testScenario14() {
		Text t1 = new Text(getComposite(), SWT.BORDER);
		Text t2 = new Text(getComposite(), SWT.BORDER);

		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(t1),
				BeanProperties.value("name").observe(adventure));
		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(t2),
				BeanProperties.value("name").observe(adventure));

		final int[] counter = { 0 };

		IObservableValue uv = BeanProperties.value("name").observe(adventure);

		uv.addChangeListener(event -> counter[0]++);

		String name = adventure.getName() + "Foo";
		enterText(t1, name);
		assertEquals(name, adventure.getName());
		assertEquals(name, t2.getText());
		assertTrue(counter[0] == 1);

		name = name + "Bar";
		uv.setValue(name);
		assertEquals(t1.getText(), adventure.getName());
		assertEquals(2, counter[0]);

	}

	@Test
	public void testScenario15() {
		Text text = new Text(getComposite(), SWT.NONE);
		Account account = new Account();
		account.setExpiryDate(new Date());

		Binding b = getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				BeanProperties.value("expiryDate").observe(account));
		Text errorText = new Text(getComposite(), SWT.NONE);

		getDbc().bindValue(WidgetProperties.text(SWT.Modify).observe(errorText), b.getValidationStatus(),
				UpdateValueStrategy.never(), null);
		assertTrue(b.getValidationStatus().getValue().isOK());
		enterText(text, "foo");
		assertFalse(b.getValidationStatus().getValue().isOK());
	}
}
