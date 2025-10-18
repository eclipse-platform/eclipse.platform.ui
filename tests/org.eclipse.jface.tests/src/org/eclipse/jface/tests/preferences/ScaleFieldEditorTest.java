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

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScaleFieldEditorTest {

	private static final String PREFKEY = "scaleValue";

	private Shell shell;
	private ScaleFieldEditor editor;

	@BeforeEach
	public void setUp() {
		shell = new Shell(SWT.NONE);
		editor = new ScaleFieldEditor(PREFKEY, "Test Scale", shell);
	}

	@AfterEach
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
			final var scale = new Scale(shell, 0);
			defaultMax = scale.getMaximum();
			scale.dispose();
		}

		// choose a new min that is higher than the default max to test proper bounds
		// handling when setting min to a value that is larger than the OS default max
		final var newMin = defaultMax + 1;
		final var newMax = defaultMax + 100;

		editor.dispose();
		editor = new ScaleFieldEditor(PREFKEY, "Test Scale", shell, newMin, newMax, 3, 9);

		assertEquals(newMin, editor.getMinimum());
		assertEquals(newMin, editor.getScaleControl().getMinimum());
		assertEquals(newMax, editor.getMaximum());
		assertEquals(newMax, editor.getScaleControl().getMaximum());

		assertEquals(3, editor.getIncrement());
		assertEquals(3, editor.getScaleControl().getIncrement());

		assertEquals(9, editor.getScaleControl().getPageIncrement());
		assertEquals(9, editor.getPageIncrement());
	}

	@Test
	public void testGetterAndSetter() {
		final Scale widget = editor.getScaleControl();

		assertNotNull(widget);

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

		assertTrue(editor.getLabelControl(shell).isEnabled());
		assertTrue(widget.isEnabled());
		editor.setEnabled(false, shell);
		assertFalse(editor.getLabelControl(shell).isEnabled());
		assertFalse(widget.isEnabled());
	}

	@Test
	public void testLoad() {
		final Scale widget = editor.getScaleControl();
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
		final Scale widget = editor.getScaleControl();
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
		final Scale widget = editor.getScaleControl();
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
