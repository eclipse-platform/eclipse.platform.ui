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

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BooleanFieldEditorTest extends TestCase {

	private Shell shell;
	private BooleanFieldEditor bfEditorWithSameLabel;
	private BooleanFieldEditor bfEditorWithSeparateLabel;
	
	private boolean otherThreadEventOccurred = false;
	private final Object lock = new Object();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		shell = new Shell();
		
		bfEditorWithSameLabel = new BooleanFieldEditor("name", "label", shell);
		bfEditorWithSeparateLabel = new BooleanFieldEditor("name2", "label", BooleanFieldEditor.SEPARATE_LABEL, shell);
	}
	
	public void testSetLabelText() {
		bfEditorWithSameLabel.setLabelText("label text");
		assertEquals("label text", bfEditorWithSameLabel.getLabelText());
		
		bfEditorWithSeparateLabel.setLabelText("label text");
		assertEquals("label text", bfEditorWithSameLabel.getLabelText());
	}
	
	public void testLoad() {	
		PreferenceStore myPreferenceStore = new PreferenceStore();
		bfEditorWithSameLabel.setPreferenceName("name");
		bfEditorWithSameLabel.setPreferenceStore(myPreferenceStore);	

		myPreferenceStore.setDefault("name", true);	//Make sure this doesn't interfere
		myPreferenceStore.setValue("name", false);
		bfEditorWithSameLabel.load();
		assertFalse(bfEditorWithSameLabel.getBooleanValue());
		
		myPreferenceStore.setDefault("name", false); //Make sure this doesn't interfere
		myPreferenceStore.setValue("name", true);
		bfEditorWithSameLabel.load();
		assertTrue(bfEditorWithSameLabel.getBooleanValue());
	}
	
	public void testLoadDefault() {	
		PreferenceStore myPreferenceStore = new PreferenceStore();
		bfEditorWithSameLabel.setPreferenceName("name");
		bfEditorWithSameLabel.setPreferenceStore(myPreferenceStore);	

		myPreferenceStore.setDefault("name", false);
		myPreferenceStore.setValue("name", true);	//Make sure this doesn't interfere
		bfEditorWithSameLabel.loadDefault();
		assertFalse(bfEditorWithSameLabel.getBooleanValue());
		
		myPreferenceStore.setDefault("name", true);
		myPreferenceStore.setValue("name", false);	//Make sure this doesn't interfere
		bfEditorWithSameLabel.loadDefault();
		assertTrue(bfEditorWithSameLabel.getBooleanValue());
	}
	
	public void testGetBooleanValue() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		bfEditorWithSameLabel.setPreferenceName("name");
		bfEditorWithSameLabel.setPreferenceStore(myPreferenceStore);	

		myPreferenceStore.setValue("name", true);
		bfEditorWithSameLabel.load();
		assertTrue(bfEditorWithSameLabel.getBooleanValue());
		
		Button button = getButton(bfEditorWithSameLabel);
		button.setSelection(false);
		assertFalse(bfEditorWithSameLabel.getBooleanValue());
		
		button.setSelection(true);
		assertTrue(bfEditorWithSameLabel.getBooleanValue());
	}
	
	public void testStore() {
		PreferenceStore myPreferenceStore = new PreferenceStore();
		bfEditorWithSameLabel.setPreferenceName("name");
		bfEditorWithSameLabel.setPreferenceStore(myPreferenceStore);	

		myPreferenceStore.setValue("name", true);
		bfEditorWithSameLabel.load();
		assertTrue(bfEditorWithSameLabel.getBooleanValue());
		
		Button button = getButton(bfEditorWithSameLabel);
		button.setSelection(false);
		assertTrue(myPreferenceStore.getBoolean("name"));
		bfEditorWithSameLabel.store();
		assertFalse(myPreferenceStore.getBoolean("name"));
		
		button.setSelection(true);
		assertTrue(bfEditorWithSameLabel.getBooleanValue());
		assertFalse(myPreferenceStore.getBoolean("name"));
		bfEditorWithSameLabel.store();
		assertTrue(myPreferenceStore.getBoolean("name"));
	}
	
	public void testValueChanged() {
		bfEditorWithSameLabel.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				otherThreadEventOccurred();
			}
		});
		
		PreferenceStore myPreferenceStore = new PreferenceStore();
		bfEditorWithSameLabel.setPreferenceName("name");
		bfEditorWithSameLabel.setPreferenceStore(myPreferenceStore);	

		myPreferenceStore.setValue("name", false);
		bfEditorWithSameLabel.load();
		assertFalse(bfEditorWithSameLabel.getBooleanValue());
		
		Button button = getButton(bfEditorWithSameLabel);
		button.setSelection(true);
		
		assertFalse(otherThreadEventOccurred);
		button.notifyListeners(SWT.Selection, null);
		assertTrue(bfEditorWithSameLabel.getBooleanValue());
		
		waitForEventInOtherThread();
		
		assertTrue(otherThreadEventOccurred);
	}

	
	public void testSetFocus() {
		bfEditorWithSameLabel = new BooleanFieldEditor("name", "label", shell){
			@Override
			protected Button getChangeControl(Composite parent) {
				return new Button(parent,SWT.CHECK){
					@Override
					protected void checkSubclass() {}
					@Override
					public boolean setFocus() {
						otherThreadEventOccurred();
						return super.setFocus();
					}
				};
			}
		};
		assertFalse(otherThreadEventOccurred);
		bfEditorWithSameLabel.setFocus();
		waitForEventInOtherThread();
		assertTrue(otherThreadEventOccurred);
	}
	
	public void testSetEnabled() {
		Button buttonWithSameLabel = getButton(bfEditorWithSameLabel);
		
		bfEditorWithSameLabel.setEnabled(true, shell);
		assertTrue(buttonWithSameLabel.isEnabled());
		
		bfEditorWithSameLabel.setEnabled(false, shell);
		assertFalse(buttonWithSameLabel.isEnabled());
		
		bfEditorWithSameLabel.setEnabled(true, shell);
		assertTrue(buttonWithSameLabel.isEnabled());
		
		Button buttonWithSeparateLabel = getButton(bfEditorWithSeparateLabel);
		Label separateLabel = bfEditorWithSeparateLabel.getLabelControl(shell);
		
		bfEditorWithSeparateLabel.setEnabled(true, shell);
		assertTrue(buttonWithSeparateLabel.isEnabled());
		assertTrue(separateLabel.isEnabled());
		
		bfEditorWithSeparateLabel.setEnabled(false, shell);
		assertFalse(buttonWithSeparateLabel.isEnabled());
		assertFalse(separateLabel.isEnabled());
		
		bfEditorWithSeparateLabel.setEnabled(true, shell);
		assertTrue(buttonWithSeparateLabel.isEnabled());
		assertTrue(separateLabel.isEnabled());
	}
	
	public void testAdjustForNumColumns() {
		final BooleanFieldEditor[] editors = new BooleanFieldEditor[2];
		
		PreferencePage page = new FieldEditorPreferencePage(FieldEditorPreferencePage.GRID) {
			@Override
			protected void createFieldEditors() {
				Composite parent = getFieldEditorParent();
				BooleanFieldEditor bfEditorWithSameLabel = new BooleanFieldEditor("name", "label", parent);
				BooleanFieldEditor bfEditorWithSeparateLabel = new BooleanFieldEditor("name2", "label", BooleanFieldEditor.SEPARATE_LABEL, parent);

				editors[0] = bfEditorWithSameLabel;
				editors[1] = bfEditorWithSeparateLabel;
				
				addField(bfEditorWithSameLabel);
				addField(bfEditorWithSeparateLabel);
			}			
		};
		
		page.createControl(shell);
		
		BooleanFieldEditor bfEditorWithSameLabel = editors[0];
		BooleanFieldEditor bfEditorWithSeparateLabel = editors[1];
		
		Button buttonWithSameLabel = getButton(bfEditorWithSameLabel);
		Button buttonWithSeparateLabel = getButton(bfEditorWithSeparateLabel);
		
		int withLabelSpan = ((GridData)buttonWithSameLabel.getLayoutData()).horizontalSpan;
		int separateLabelSpan = ((GridData)buttonWithSeparateLabel.getLayoutData()).horizontalSpan;
		
		assertEquals(withLabelSpan - 1, separateLabelSpan);
	}
	
	/**
	 * Reads the button control from the BooleanFieldEditor
	 */
	private Button getButton(BooleanFieldEditor booleanFieldEditor) {
		try {
			Field f = BooleanFieldEditor.class.getDeclaredField("checkBox");
			f.setAccessible(true);
			return (Button) f.get(booleanFieldEditor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Invoke to signal a single expected event from another thread.
	 */
	private void otherThreadEventOccurred() {
		synchronized (lock) {
			otherThreadEventOccurred = true;
			lock.notify();	
		}
	}
	
	/**
	 * Invoke to wait for a single expected event from another thread.
	 * Times out after one second.
	 */
	private void waitForEventInOtherThread() {
		synchronized (lock) {
			if(!otherThreadEventOccurred) {
				try {
					lock.wait(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

