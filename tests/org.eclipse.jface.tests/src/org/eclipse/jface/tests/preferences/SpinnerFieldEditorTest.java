/*******************************************************************************
 * Copyright (c) 2024 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sebastian Thomschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.preferences;

import static org.junit.Assert.*;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.SpinnerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpinnerFieldEditorTest {

	private static final String PREFKEY = "spinnerValue";

	private Shell shell;
	private SpinnerFieldEditor editor;

	@Before
	public void setUp() {
		shell = new Shell(SWT.NONE);
		editor = new SpinnerFieldEditor(PREFKEY, "Test Spinner", shell);
	}

	@After
	public void tearDown() {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
	}

	@Test
	public void testConstructorArgs() {
		// determine default OS-specific max (on Windows = 100)
		final int defaultMax;
		{
			final var scale = new Spinner(shell, 0);
			defaultMax = scale.getMaximum();
			scale.dispose();
		}

		// choose a new min that is higher than the default max to test proper bounds
		// handling when setting min to a value that is larger than the OS default max
		final var newMin = defaultMax + 1;
		final var newMax = defaultMax + 100;

		editor.dispose();
		editor = new SpinnerFieldEditor(PREFKEY, "Test Scale", shell, newMin, newMax);

		assertEquals(newMin, editor.getMinimum());
		assertEquals(newMin, editor.getSpinnerControl().getMinimum());
		assertEquals(newMax, editor.getMaximum());
		assertEquals(newMax, editor.getSpinnerControl().getMaximum());
	}

	@Test
	public void testGetterAndSetter() {
		final Spinner widget = editor.getSpinnerControl();

		assertNotNull(widget);

		assertEquals(0, editor.getDigits());
		assertEquals(0, widget.getDigits());
		editor.setDigits(2);
		assertEquals(2, editor.getDigits());
		assertEquals(2, widget.getDigits());

		assertEquals(10, editor.getMaximum());
		assertEquals(10, widget.getMaximum());
		editor.setMaximum(999);
		assertEquals(999, editor.getMaximum());
		assertEquals(999, widget.getMaximum());

		assertEquals(0, editor.getMinimum());
		assertEquals(0, widget.getMinimum());
		editor.setMinimum(99);
		assertEquals(99, editor.getMinimum());
		assertEquals(99, widget.getMinimum());

		assertEquals(1, editor.getIncrement());
		assertEquals(1, widget.getIncrement());
		editor.setIncrement(9);
		assertEquals(9, editor.getIncrement());
		assertEquals(9, widget.getIncrement());

		assertEquals(1, editor.getPageIncrement());
		assertEquals(1, widget.getPageIncrement());
		editor.setPageIncrement(99);
		assertEquals(99, editor.getPageIncrement());
		assertEquals(99, widget.getPageIncrement());

		assertEquals(Spinner.LIMIT, editor.getTextLimit());
		/*
		 * the following assertion commented out because setting text limit to
		 * Spinner#Limit on Windows results in an effective limit of (Spinner#Limit - 1)
		 * and probably into different values on other platforms
		 */
		// assertEquals(Spinner.LIMIT, spinner.getTextLimit());
		editor.setTextLimit(99);
		assertEquals(99, editor.getTextLimit());
		assertEquals(99, widget.getTextLimit());

		assertTrue(editor.getLabelControl(shell).isEnabled());
		assertTrue(widget.isEnabled());
		editor.setEnabled(false, shell);
		assertFalse(editor.getLabelControl(shell).isEnabled());
		assertFalse(widget.isEnabled());
	}

	@Test
	public void testLoad() {
		final Spinner widget = editor.getSpinnerControl();
		assertEquals(0, widget.getSelection());

		final var prefStoreWithDefault = new PreferenceStore();
		prefStoreWithDefault.setDefault(PREFKEY, 2);

		editor.setPreferenceStore(prefStoreWithDefault);

		editor.load();
		assertEquals(2, widget.getSelection());

		final var prefStoreWithDefaultAndValue = new PreferenceStore();
		prefStoreWithDefaultAndValue.setDefault(PREFKEY, 2);
		prefStoreWithDefaultAndValue.setValue(PREFKEY, 4);

		editor.setPreferenceStore(prefStoreWithDefaultAndValue);

		editor.load();
		assertEquals(4, widget.getSelection());
	}

	@Test
	public void testLoadDefault() {
		final Spinner widget = editor.getSpinnerControl();
		assertEquals(0, widget.getSelection());

		final var prefStoreWithDefaultAndValue = new PreferenceStore();
		prefStoreWithDefaultAndValue.setDefault(PREFKEY, 2);
		prefStoreWithDefaultAndValue.setValue(PREFKEY, 4);

		editor.setPreferenceStore(prefStoreWithDefaultAndValue);

		editor.load();
		assertEquals(4, widget.getSelection());

		editor.loadDefault();
		assertEquals(2, widget.getSelection());
	}

	@Test
	public void testSetValueInWidget() {
		final Spinner widget = editor.getSpinnerControl();
		assertEquals(0, widget.getSelection());

		final int min = 100;
		final int max = 103;

		editor.setMinimum(min);
		editor.setMaximum(max);

		// by setting the minimum bound the selection changes accordingly
		assertEquals(min, widget.getSelection());

		widget.setSelection(min - 1); // outside lower bound
		assertEquals(min, widget.getSelection());

		widget.setSelection(min);
		assertEquals(min, widget.getSelection());

		widget.setSelection(min + 1);
		assertEquals(min + 1, widget.getSelection());

		widget.setSelection(max);
		assertEquals(max, widget.getSelection());

		widget.setSelection(max + 1); // outside upper bound
		assertEquals(max, widget.getSelection());
	}
}
