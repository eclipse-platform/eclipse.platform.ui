/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 260329
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.examples.databinding.model.Account;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.jface.examples.databinding.model.Transportation;
import org.eclipse.jface.tests.databinding.BindingTestSuite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.NumberFormat;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class TextControlScenario extends ScenariosTestCase {

    private Text text;

    private Adventure adventure;

    private Transportation transportation;

    Account account;

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        // do any setup work here
        adventure = SampleData.WINTER_HOLIDAY;
        transportation = SampleData.EXECUTIVE_JET;
        account = SampleData.PRESIDENT;
        text = new Text(getComposite(), SWT.BORDER);
    }

    @Override
	protected void tearDown() throws Exception {
        text.dispose();
        text = null;
        super.tearDown();
    }

    public void testScenario01() {
        // Bind the adventure "name" property to a text field
        // Change the UI and verify the model changes
        // Change the model and verify the UI changes
        getDbc().bindValue(SWTObservables.observeText(text, SWT.Modify),
                BeansObservables.observeValue(adventure, "name"));

        assertEquals(adventure.getName(), text.getText());
        text.setText("England");
        text.notifyListeners(SWT.FocusOut, null);
        assertEquals("England", adventure.getName());
        adventure.setName("France");
        assertEquals("France", text.getText());
        adventure.setName("Germany");
        spinEventLoop(0);
        assertEquals("Germany", text.getText());
    }

    public void testScenario02() {

        // Bind the transportation "price" property to a text field
        // This is a Double.TYPE so we check that conversion and validation
        // occurs
        // Change the UI and verify the model changes
        // Change the model and verify the UI changes
        getDbc().bindValue(SWTObservables.observeText(text, SWT.Modify),
                BeansObservables.observeValue(transportation, "price"));

        NumberFormat numberFormat = NumberFormat.getInstance();

        assertEquals(numberFormat.format(transportation.getPrice()), text.getText());
        text.setText("9876.54");
        text.notifyListeners(SWT.FocusOut, null);
        assertEquals(9876.54, transportation.getPrice(), 0);

        transportation.setPrice(1234.56);
        assertEquals(numberFormat.format(transportation.getPrice()), text.getText());
    }

//    public void testScenario03() {
//        // Show that the Escape key can be pressed in the middle of editing and
//        // the value will revert
//        // the updatePolicy for this test is TIME_LATE so it occurs when focus
//        // is lost from the Text control
//        getDbc().bindValue(SWTObservables.observeText(text, SWT.FocusOut),
//                BeansObservables.observeValue(adventure, "name"),
//                null, null);
//
//        String currentText = text.getText();
//        text.setText("Switzerland");
//        // We do not notify FocusOut
//        // Verify that the model hasn't changed
//        assertEquals(currentText, adventure.getName());
//        Event event = new Event();
//        event.character = SWT.ESC;
//        event.keyCode = 27;
//        text.notifyListeners(SWT.KeyDown, event);
//        // Verify that the text has reverted
//        assertEquals(currentText, text.getText());
//        // And that the model didn't change
//        assertEquals(adventure.getName(), currentText);
//        // Now change the GUI and commit this change
//        currentText = "Austria";
//        text.setText(currentText);
//        text.notifyListeners(SWT.FocusOut, null);
//        assertEquals(text.getText(), adventure.getName());
//        // Now change the text again and press escape a second time
//        text.setText("Turkey");
//        // Send escape
//        text.notifyListeners(SWT.KeyDown, event);
//        // Verify it has reverted to "Austria" and not any other value, i.e. the
//        // last value it displayed
//        assertEquals(currentText, text.getText());
//
//    }

//    public void testScenario04() {
//        // Show that the Escape key can be pressed in the middle of editing and
//        // the value will revert
//        // the updatePolicy for this test is TIME_EARLY so it occurs when each
//        // keystroke occurs
//        getDbc().bindValue(SWTObservables.observeText(text, SWT.Modify),
//                BeansObservables.observeValue(adventure, "name"),
//                null, null);
//
//        String originalName = adventure.getName();
//        // Change the text field character by character and ensure that the
//        // model changes
//        String newName = "Switzerland";
//        for (int i = 0; i < newName.length(); i++) {
//            text.setText(newName.substring(0, i + 1));
//            // Verify the model has changed
//            assertEquals(newName.substring(0, i + 1), adventure.getName());
//        }
//
//        // Now send an escape key and verify that the model reverts
//        Event event = new Event();
//        event.character = SWT.ESC;
//        event.keyCode = 27;
//        text.notifyListeners(SWT.KeyDown, event);
//        assertEquals(adventure.getName(), originalName);
//
//        // Now send "Austria" key by key
//        newName = "Austria";
//        for (int i = 0; i < newName.length(); i++) {
//            text.setText(newName.substring(0, i + 1));
//            // Verify the model has changed
//            assertEquals(newName.substring(0, i + 1), adventure.getName());
//        }
//        // Send a focus lost event to commit the change
//        text.notifyListeners(SWT.FocusOut, null);
//        // Send an escape key
//        text.notifyListeners(SWT.KeyDown, event);
//        // Verify that the model has changed and has not reverted
//        assertEquals(newName, adventure.getName());
//    }

    /**
     * public void testScenario05(){ // Show that nesting of properties works.
     * Adventure has defaultLodging and Lodging has name getDbc().bind(text,new
     * Property(adventure,"defaultLodging.name"),null); // Verify the GUI is
     * showing the model value
     * assertEquals(text.getText(),adventure.getDefaultLodging().getName()); }
     */
    public void testScenario06() {
        // // Show that partial validation works for TIME_EARLY
        // // We are using TIME_EARLY to verify that invalid states are not sent
        // to the model
        // getSWTObservableFactory().setUpdateTime(DataBindingContext.TIME_EARLY);
        // getDbc().bind(text, new Property(account, "phone"), new BindSpec(new
        // PhoneConverter(),new PhoneValidator()));
        // // Verify we have no error message for partial validation or full
        // validation yet
        // assertTrue(((String)getDbc().getPartialValidationMessage().getValue()).length()
        // == 0);
        // assertTrue(((String)getDbc().getValidationMessage().getValue()).length()
        // == 0);
        // // Update some of the phone number
        // String originalPhoneNumber = account.getPhone();
        // text.setText("999");
        // // Verify that the phone number is partially invalid and there is no
        // validation message
        // assertTrue(((String)getDbc().getPartialValidationMessage().getValue()).length()
        // > 0);
        // assertTrue(((String)getDbc().getValidationMessage().getValue()).length()
        // == 0);
        // // And that the model has not changed
        // assertEquals(account.getPhone(),originalPhoneNumber);
        // // Verify that fixing the phone removes the error and the model is
        // updated too
        // text.setText("999-888-7777");
        // assertTrue(((String)getDbc().getPartialValidationMessage().getValue()).length()
        // == 0);
        // assertEquals(account.getPhone(),"9998887777");
    }

    public void testScenario07() {
        // // Show that partial validation works for TIME_LATE
        // getSWTObservableFactory().setUpdateTime(DataBindingContext.TIME_LATE);
        // getDbc().bind(text, new Property(account, "phone"), new BindSpec(new
        // PhoneConverter(),new PhoneValidator()));
        // // Update some of the phone number
        // String originalPhoneNumber = account.getPhone();
        // text.setText("222");
        // // Verify that we have no completion validation message and a partial
        // one
        // assertTrue(((String)getDbc().getPartialValidationMessage().getValue()).length()
        // > 0);
        // assertTrue(((String)getDbc().getValidationMessage().getValue()).length()
        // == 0);
        // // Fix the error
        // text.setText("222-333-4444");
        // // Verify that the errors are both fixed
        // assertTrue(((String)getDbc().getPartialValidationMessage().getValue()).length()
        // == 0);
        // assertTrue(((String)getDbc().getValidationMessage().getValue()).length()
        // == 0);
        // // The model should not be changed
        // assertEquals(originalPhoneNumber,account.getPhone());
        // // Lose focus and verify that the complete validation message is
        // fixed
        // text.notifyListeners(SWT.FocusOut,null);
        // assertTrue(((String)getDbc().getValidationMessage().getValue()).length()
        // == 0);
        // // The model should be changed
        // assertEquals("2223334444",account.getPhone());
    }

    public void testScenario08() {

        if (BindingTestSuite.failingTestsDisabled(this)) {
            return;
        }

        // Show that the CustomBeanBindSupportFactory will automatically pick up
        // the
        // validator on the MaxNumberOfPeople property

        DataBindingContext dbc = getDbc();

        dbc.bindValue(SWTObservables.observeText(text, SWT.Modify),
				BeansObservables.observeValue(adventure, "maxNumberOfPeople"),
				new CustomBeanUpdateValueStrategy(), null);

        // make sure we can set a value inside the validator's range
        text.setText("4");
        assertEquals(4, adventure.getMaxNumberOfPeople());
        // Now try to set a value outside the validator's range
        text.setText("999");
        assertEquals(4, adventure.getMaxNumberOfPeople());
        dbc.dispose();
    }

    public void testScenario09() {
        // Verify direct binding between a Text and Label following bugzilla
        // 118696
        Label label = new Label(getComposite(), SWT.NONE);
        getDbc().bindValue(SWTObservables.observeText(text, SWT.FocusOut), SWTObservables.observeText(label));

        // Change the text
        text.setText("Frog");
        // Verify the label does not change
        assertTrue(label.getText().length() == 0);
        // Lose focus from the text field
        text.notifyListeners(SWT.FocusOut, null);
        assertEquals(label.getText(), "Frog");

    }

    public void testScenario10() {
        // Verify direct binding between a Text and Label following bugzilla
        // 118696 with TIME_EARLY
        Label label = new Label(getComposite(), SWT.NONE);
        getDbc().bindValue(SWTObservables.observeText(text, SWT.Modify), SWTObservables.observeText(label));

        // Change the text
        String newTextValue = "Frog";
        for (int i = 0; i < newTextValue.length(); i++) {
            text.setText(newTextValue.substring(0, i + 1));
            // Verify the label has changed key by key
            assertEquals(text.getText(), label.getText());
        }
        // Lose focus
        text.notifyListeners(SWT.FocusOut, null);
        // Verify the text and label are the same following a lose focus
        assertEquals(text.getText(), label.getText());
    }

}
