/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.swt;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.swt.ButtonObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.CLabelObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.LabelObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.jface.internal.databinding.internal.swt.ScaleObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SpinnerObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TableObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TextEditableObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.jface.tests.databinding.RealmTester;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.1
 */
public class SWTObservablesTest extends AbstractSWTTestCase {
	private Shell shell;

	protected void setUp() throws Exception {
		super.setUp();
		
		shell = getShell();
		RealmTester.setDefault(SWTObservables.getRealm(shell.getDisplay()));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		RealmTester.setDefault(null);
	}

	public void testObserveForeground() throws Exception {
		ISWTObservableValue value = SWTObservables.observeForeground(shell);
		assertNotNull(value);
		assertEquals(Color.class, value.getValueType());
	}

	public void testObserveBackground() throws Exception {
		ISWTObservableValue value = SWTObservables.observeBackground(shell);
		assertNotNull(value);
		assertEquals(Color.class, value.getValueType());
	}

	public void testObserveFont() throws Exception {
		ISWTObservableValue value = SWTObservables.observeFont(shell);
		assertNotNull(value);
		assertEquals(Font.class, value.getValueType());
	}

	public void testObserveSelectionOfSpinner() throws Exception {
		Spinner spinner = new Spinner(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(spinner);
		assertNotNull(value);
		assertTrue(value instanceof SpinnerObservableValue);

		SpinnerObservableValue spinnerObservable = (SpinnerObservableValue) value;
		assertEquals(SWTProperties.SELECTION, spinnerObservable.getAttribute());
	}

	public void testObserveSelectionOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeSelection(button);
		assertNotNull(value);
		assertTrue(value instanceof ButtonObservableValue);
	}

	public void testObserveSelectionOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertNotNull(value);
		assertTrue(value instanceof ComboObservableValue);

		ComboObservableValue comboObservable = (ComboObservableValue) value;
		assertEquals(SWTProperties.SELECTION, comboObservable.getAttribute());
	}

	public void testObserveSelectionOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertNotNull(value);
		assertTrue(value instanceof CComboObservableValue);

		CComboObservableValue ccomboObservable = (CComboObservableValue) value;
		assertEquals(SWTProperties.SELECTION, ccomboObservable.getAttribute());
	}

	public void testObserveSelectionOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(list);
		assertNotNull(value);
		assertTrue(value instanceof ListObservableValue);
	}
	
	public void testObserveSelectionOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(scale);
		assertNotNull(value);
		assertTrue(value instanceof ScaleObservableValue);
		
		ScaleObservableValue scaleObservable = (ScaleObservableValue) value;
		assertEquals(SWTProperties.SELECTION, scaleObservable.getAttribute());
	}

	public void testObserveSelectionOfUnsupportedControl() throws Exception {
		try {
			Text text = new Text(shell, SWT.NONE);
			SWTObservables.observeSelection(text);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testObserveTextOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		assertFalse(text.isListening(SWT.FocusOut));

		ISWTObservableValue value = SWTObservables.observeText(text,
				SWT.FocusOut);
		assertNotNull(value);
		assertTrue(value instanceof TextObservableValue);
		assertTrue(text.isListening(SWT.FocusOut));
	}

	public void testObserveTextWithEventOfUnsupportedControl() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		try {
			SWTObservables.observeText(label, SWT.FocusOut);
			fail("Exception should have been thrown");
		} catch (Exception e) {
		}
	}

	public void testObserveTextOfLabel() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertNotNull(label);
		assertTrue(value instanceof LabelObservableValue);
	}

	public void testObserveTextOfCLabel() throws Exception {
		CLabel label = new CLabel(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertNotNull(label);
		assertTrue(value instanceof CLabelObservableValue);
	}

	public void testObserveTextOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertNotNull(value);
		assertTrue(value instanceof ComboObservableValue);

		ComboObservableValue comboObservable = (ComboObservableValue) value;
		assertEquals(SWTProperties.TEXT, comboObservable.getAttribute());
	}

	public void testObserveTextOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertNotNull(value);
		assertTrue(value instanceof CComboObservableValue);

		CComboObservableValue ccomboObservable = (CComboObservableValue) value;
		assertEquals(SWTProperties.TEXT, ccomboObservable.getAttribute());
	}

	public void testObserveTextOfUnsupportedControl() throws Exception {
		Table table = new Table(shell, SWT.NONE);
		try {
			SWTObservables.observeText(table);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testObserveItemsOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		IObservableList list = SWTObservables.observeItems(combo);
		assertNotNull(list);
		assertTrue(list instanceof ComboObservableList);
	}

	public void testObserveItemsOfCCombo() throws Exception {
		CCombo ccombo = new CCombo(shell, SWT.NONE);
		IObservableList list = SWTObservables.observeItems(ccombo);
		assertNotNull(list);
		assertTrue(list instanceof CComboObservableList);
	}

	public void testObserveItemsOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		IObservableList observableList = SWTObservables.observeItems(list);
		assertNotNull(observableList);
		assertTrue(observableList instanceof ListObservableList);
	}

	public void testObserveItemsOfUnsupportedControl() throws Exception {
		Table table = new Table(shell, SWT.NONE);
		try {
			SWTObservables.observeItems(table);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testObserveSingleSelectionIndexOfTable() throws Exception {
		Table table = new Table(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables
				.observeSingleSelectionIndex(table);
		assertNotNull(value);
		assertTrue(value instanceof TableObservableValue);

		TableObservableValue tableObservable = (TableObservableValue) value;
		assertEquals(SWTProperties.SELECTION, tableObservable.getAttribute());
	}

	public void testObserveSingleSelectionIndexOfUnsupportedControl()
			throws Exception {
		List list = new List(shell, SWT.NONE);
		try {
			SWTObservables.observeSingleSelectionIndex(list);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {

		}
	}
	
	public void testObserveMinOfSpinner() throws Exception {
		Spinner spinner = new Spinner(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMin(spinner);
		assertNotNull(value);
		assertTrue(value instanceof SpinnerObservableValue);
		
		SpinnerObservableValue spinnerObservable = (SpinnerObservableValue) value;
		assertEquals(SWTProperties.MIN, spinnerObservable.getAttribute());
	}
	
	public void testObserveMinOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMin(scale);
		assertNotNull(value);
		assertTrue(value instanceof ScaleObservableValue);
		
		ScaleObservableValue scaleObservable = (ScaleObservableValue) value;
		assertEquals(SWTProperties.MIN, scaleObservable.getAttribute());
	}

	public void testObserveMinOfUnsupportedControl() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		try {
			SWTObservables.observeMin(text);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {	
		}
	}
	
	public void testObserveMaxOfSpinner() throws Exception {
		Spinner spinner = new Spinner(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMax(spinner);
		assertNotNull(value);
		assertTrue(value instanceof SpinnerObservableValue);
		
		SpinnerObservableValue spinnerObservable = (SpinnerObservableValue) value;
		assertEquals(SWTProperties.MAX, spinnerObservable.getAttribute());
	}
	
	public void testObserveMaxOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMax(scale);
		assertNotNull(value);
		assertTrue(value instanceof ScaleObservableValue);
		
		ScaleObservableValue scaleObservable = (ScaleObservableValue) value;
		assertEquals(SWTProperties.MAX, scaleObservable.getAttribute());
	}
	
	public void testObserveMaxOfUnsupportedControl() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		try {
			SWTObservables.observeMax(text);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testObserveEditableOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeEditable(text);
		assertNotNull(value);
		assertTrue(value instanceof TextEditableObservableValue);
	}
	
	public void testObserveEditableOfUnsupportedControl() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		try {
			SWTObservables.observeEditable(label);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
