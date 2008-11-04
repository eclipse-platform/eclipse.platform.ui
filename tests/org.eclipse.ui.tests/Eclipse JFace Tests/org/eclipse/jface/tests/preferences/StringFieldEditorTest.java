/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Bisson - <mrbisson@ca.ibm.com> Initial test implementation
 ******************************************************************************/

package org.eclipse.jface.tests.preferences;


import junit.framework.TestCase;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class StringFieldEditorTest extends TestCase {

	private Shell shell;
	private StringFieldEditor stringFieldEditor;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		shell = new Shell();
		
		stringFieldEditor = new StringFieldEditor("name", "label", shell);
	}
	
	public void testSetLabelText() {
		stringFieldEditor.setLabelText("label text");
		assertEquals("label text", stringFieldEditor.getLabelText());
		
		stringFieldEditor.setLabelText("label text");
		assertEquals("label text", stringFieldEditor.getLabelText());
	}
	
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
	
	public void testLoadDefault() {	
		PreferenceStore myPreferenceStore = new PreferenceStore();
		stringFieldEditor.setPreferenceName("name");
		stringFieldEditor.setPreferenceStore(myPreferenceStore);	

		myPreferenceStore.setDefault("name", "foo");
		myPreferenceStore.setValue("name", "bar");	
		stringFieldEditor.loadDefault();
		assertEquals(stringFieldEditor.getStringValue(), "foo");
	}
	
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
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

