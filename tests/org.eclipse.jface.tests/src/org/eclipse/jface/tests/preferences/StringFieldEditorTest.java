/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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
 *     Pawel Pogorzelski - <Pawel.Pogorzelski@pl.ibm.com> - test for bug 289599
 ******************************************************************************/

package org.eclipse.jface.tests.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Before;
import org.junit.Test;

public class StringFieldEditorTest {

	private Shell shell;
	private StringFieldEditor stringFieldEditor;

	@Before
	public void setUp() throws Exception {
		shell = new Shell();

		stringFieldEditor = new StringFieldEditor("name", "label", shell);
	}

	@Test
	public void testSetLabelText() {
		stringFieldEditor.setLabelText("label text");
		assertEquals("label text", stringFieldEditor.getLabelText());

		stringFieldEditor.setLabelText("label text");
		assertEquals("label text", stringFieldEditor.getLabelText());
	}

	@Test
	public void testLoad() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		stringFieldEditor.setPreferenceName("name");
		stringFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setDefault("name", "foo");
		stringFieldEditor.load();
		assertEquals(stringFieldEditor.getStringValue(), "foo");

		myPreferenceStore.setDefault("name", "foo");
		myPreferenceStore.setValue("name", "bar");
		stringFieldEditor.load();
		assertEquals(stringFieldEditor.getStringValue(), "bar");
	}

	@Test
	public void testLoadDefault() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		stringFieldEditor.setPreferenceName("name");
		stringFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setDefault("name", "foo");
		myPreferenceStore.setValue("name", "bar");
		stringFieldEditor.loadDefault();
		assertEquals(stringFieldEditor.getStringValue(), "foo");
	}

	@Test
	public void testSetValueInWidget() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		stringFieldEditor.setPreferenceName("name");
		stringFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setValue("name", "foo");
		stringFieldEditor.load();
		assertEquals(stringFieldEditor.getStringValue(), "foo");

		Text text = stringFieldEditor.getTextControl(shell);
		text.setText("bar");
		assertEquals(stringFieldEditor.getStringValue(), "bar");
	}

	@Test
	public void testSetValueInEditor() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		stringFieldEditor.setPreferenceName("name");
		stringFieldEditor.setPreferenceStore(myPreferenceStore);

		myPreferenceStore.setValue("name", "foo");
		stringFieldEditor.load();
		assertEquals(stringFieldEditor.getStringValue(), "foo");

		stringFieldEditor.setStringValue("bar");
		Text text = stringFieldEditor.getTextControl(shell);
		assertEquals(text.getText(), "bar");
		assertEquals(stringFieldEditor.getStringValue(), "bar");
	}

	@Test
	public void testBug289599() {
		PreferenceStore store = new PreferenceStore();
		store.setDefault("foo", "bar");
		assertEquals("bar", store.getString("foo"));
		store.setValue("foo", "???");
		assertEquals("???", store.getString("foo"));
		IPropertyChangeListener listener = event -> {
			assertEquals("foo", event.getProperty());
			assertEquals("???", event.getOldValue());
			assertEquals("bar", event.getNewValue());
		};
		store.addPropertyChangeListener(listener);
		store.setToDefault("foo");
		store.removePropertyChangeListener(listener);
		assertEquals("bar", store.getString("foo"));
		IPropertyChangeListener failingListener = event -> fail("1.0");
		store.addPropertyChangeListener(failingListener);
		// We already called setToDefault, nothing should happen this time
		store.setToDefault("foo");
		store.removePropertyChangeListener(failingListener);
		assertEquals("bar", store.getString("foo"));
	}

}
