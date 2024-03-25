/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 *     Matthew Bisson - <mrbisson@ca.ibm.com> Initial test implementation
 ******************************************************************************/

package org.eclipse.jface.tests.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Before;
import org.junit.Test;

public class IntegerFieldEditorTest {

	private Shell shell;
	private IntegerFieldEditor integerFieldEditor;

	@Before
	public void setUp() throws Exception {
		shell = new Shell();

		integerFieldEditor = new IntegerFieldEditor("name", "label", shell);
		integerFieldEditor.setValidRange(0, 500);
		integerFieldEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
	}

	@Test
	public void testLoad() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		integerFieldEditor.setPreferenceName("name");
		integerFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setDefault("name", 5);
		integerFieldEditor.load();
		assertEquals(integerFieldEditor.getIntValue(), 5);

		myPreferenceStore.setDefault("name", 5);
		myPreferenceStore.setValue("name", 6);
		integerFieldEditor.load();
		assertEquals(integerFieldEditor.getIntValue(), 6);
	}

	@Test
	public void testLoadDefault() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		integerFieldEditor.setPreferenceName("name");
		integerFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setDefault("name", 5);
		myPreferenceStore.setValue("name", 6);
		integerFieldEditor.loadDefault();
		assertEquals(integerFieldEditor.getIntValue(), 5);
	}

	@Test
	public void testSetValueInWidget() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		integerFieldEditor.setPreferenceName("name");
		integerFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setValue("name", 5);
		integerFieldEditor.load();
		assertEquals(integerFieldEditor.getIntValue(), 5);

		Text text = integerFieldEditor.getTextControl(shell);
		text.setText("6");
		assertEquals(integerFieldEditor.getIntValue(), 6);
	}

	@Test
	public void testSetValueInEditor() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		integerFieldEditor.setPreferenceName("name");
		integerFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setValue("name", 5);
		integerFieldEditor.load();
		assertEquals(integerFieldEditor.getIntValue(), 5);

		integerFieldEditor.setStringValue("6");
		Text text = integerFieldEditor.getTextControl(shell);
		assertEquals(text.getText(), "6");
		assertEquals(integerFieldEditor.getIntValue(), 6);
	}

	@Test
	public void testValidate() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		integerFieldEditor.setPreferenceName("name");
		integerFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setValue("name", 5000);
		integerFieldEditor.load();
		assertFalse(integerFieldEditor.isValid());
	}

}
