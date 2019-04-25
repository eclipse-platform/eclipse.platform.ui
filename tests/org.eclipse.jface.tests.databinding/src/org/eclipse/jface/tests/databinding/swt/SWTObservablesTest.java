/*******************************************************************************
 * Copyright (c) 2007, 2009, 2011 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Chris Aniszczyk <zx@code9.com> - bug 131435
 *     Matthew Hall - bugs 248621, 213893, 262320, 169876, 306203
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.swt.ISWTObservableList;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.internal.databinding.swt.ButtonImageProperty;
import org.eclipse.jface.internal.databinding.swt.ButtonSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ButtonTextProperty;
import org.eclipse.jface.internal.databinding.swt.CComboEditableProperty;
import org.eclipse.jface.internal.databinding.swt.CComboItemsProperty;
import org.eclipse.jface.internal.databinding.swt.CComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.CComboTextProperty;
import org.eclipse.jface.internal.databinding.swt.CLabelImageProperty;
import org.eclipse.jface.internal.databinding.swt.CLabelTextProperty;
import org.eclipse.jface.internal.databinding.swt.CTabItemTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.ComboItemsProperty;
import org.eclipse.jface.internal.databinding.swt.ComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ComboTextProperty;
import org.eclipse.jface.internal.databinding.swt.ControlBackgroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlForegroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.DateTimeSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ItemImageProperty;
import org.eclipse.jface.internal.databinding.swt.ItemTextProperty;
import org.eclipse.jface.internal.databinding.swt.LabelImageProperty;
import org.eclipse.jface.internal.databinding.swt.LabelTextProperty;
import org.eclipse.jface.internal.databinding.swt.ListItemsProperty;
import org.eclipse.jface.internal.databinding.swt.ListSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.MenuEnabledProperty;
import org.eclipse.jface.internal.databinding.swt.MenuItemEnabledProperty;
import org.eclipse.jface.internal.databinding.swt.MenuItemSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleMaximumProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleMinimumProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ScrollBarEnabledProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerMaximumProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerMinimumProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.StyledTextEditableProperty;
import org.eclipse.jface.internal.databinding.swt.StyledTextTextProperty;
import org.eclipse.jface.internal.databinding.swt.TableSingleSelectionIndexProperty;
import org.eclipse.jface.internal.databinding.swt.TextEditableProperty;
import org.eclipse.jface.internal.databinding.swt.TextTextProperty;
import org.eclipse.jface.internal.databinding.swt.ToolItemEnabledProperty;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class SWTObservablesTest extends AbstractSWTTestCase {
	private Shell shell;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		shell = getShell();
		RealmTester.setDefault(DisplayRealm.getRealm(shell.getDisplay()));
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();

		RealmTester.setDefault(null);
	}

	@Override
	protected Shell createShell() {
		return new Shell(SWT.V_SCROLL);
	}

	@Test
	public void testObserveForeground() throws Exception {
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeForeground(shell);
		assertWidgetObservable(value, shell, ControlForegroundProperty.class);
		assertEquals(Color.class, value.getValueType());
	}

	@Test
	public void testObserveBackground() throws Exception {
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeBackground(shell);
		assertWidgetObservable(value, shell, ControlBackgroundProperty.class);
		assertEquals(Color.class, value.getValueType());
	}

	@Test
	public void testObserveFont() throws Exception {
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeFont(shell);
		assertNotNull(value);
		assertEquals(Font.class, value.getValueType());
	}

	@Test
	public void testObserveSelectionOfSpinner() throws Exception {
		Spinner spinner = new Spinner(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(spinner);
		assertWidgetObservable(value, spinner, SpinnerSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(button);
		assertWidgetObservable(value, button, ButtonSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertWidgetObservable(value, combo, ComboSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertWidgetObservable(value, combo, CComboSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfDateTime_Date() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.DATE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertWidgetObservable(value, dateTime, DateTimeSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfDateTime_Calendar() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.CALENDAR);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertWidgetObservable(value, dateTime, DateTimeSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfDateTime_Time() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.TIME);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertWidgetObservable(value, dateTime, DateTimeSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(list);
		assertWidgetObservable(value, list, ListSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(scale);
		assertWidgetObservable(value, scale, ScaleSelectionProperty.class);
	}

	@Test
	public void testObserveSelectionOfUnsupportedControl() throws Exception {
		try {
			Text text = new Text(shell, SWT.NONE);
			SWTObservables.observeSelection(text);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testObserveTextWithEventOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		assertFalse(text.isListening(SWT.FocusOut));

		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(text,
				SWT.FocusOut);
		assertWidgetObservable(value, text, TextTextProperty.class);

		assertFalse(text.isListening(SWT.FocusOut));
		ChangeEventTracker.observe(value);
		assertTrue(text.isListening(SWT.FocusOut));
	}

	@Test
	public void testObserveTextOfStyledText() throws Exception {
		StyledText text = new StyledText(shell, SWT.NONE);
		assertFalse(text.isListening(SWT.FocusOut));

		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(text,
				SWT.FocusOut);
		assertWidgetObservable(value, text, StyledTextTextProperty.class);

		assertFalse(text.isListening(SWT.FocusOut));
		ChangeEventTracker.observe(value);
		assertTrue(text.isListening(SWT.FocusOut));
	}

	@Test
	public void testObserveTextWithEventOfUnsupportedControl() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		try {
			SWTObservables.observeText(label, SWT.FocusOut);
			fail("Exception should have been thrown");
		} catch (Exception e) {
		}
	}

	@Test
	public void testObserveTextOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(button);
		assertWidgetObservable(value, button, ButtonTextProperty.class);
	}

	@Test
	public void testObserveTextOfLabel() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertWidgetObservable(value, label, LabelTextProperty.class);
	}

	@Test
	public void testObserveTextOfCLabel() throws Exception {
		CLabel label = new CLabel(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertWidgetObservable(value, label, CLabelTextProperty.class);
	}

	@Test
	public void testObserveTextOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertWidgetObservable(value, combo, ComboTextProperty.class);
	}

	/**
	 * @param observable
	 * @return
	 */
	private IPropertyObservable<?> getPropertyObservable(ISWTObservable observable) {
		IDecoratingObservable decoratingObservable = (IDecoratingObservable) observable;
		IPropertyObservable<?> propertyObservable = (IPropertyObservable<?>) decoratingObservable
				.getDecorated();
		return propertyObservable;
	}

	@Test
	public void testObserveTextOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertWidgetObservable(value, combo, CComboTextProperty.class);
	}

	@Test
	public void testObserveTextOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(text);

		assertWidgetObservable(value, text, TextTextProperty.class);

		assertFalse(text.isListening(SWT.Modify));
		assertFalse(text.isListening(SWT.FocusOut));
	}

	@Test
	public void testObserveTextOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeText(item);
		assertWidgetObservable(value, item, ItemTextProperty.class);
	}

	@Test
	public void testObserveTextOfUnsupportedControl() throws Exception {
		Table table = new Table(shell, SWT.NONE);
		try {
			SWTObservables.observeText(table);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testObserveImageOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeImage(button);
		assertWidgetObservable(value, button, ButtonImageProperty.class);
	}

	@Test
	public void testObserveImageOfCLabel() throws Exception {
		CLabel cLabel = new CLabel(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeImage(cLabel);
		assertWidgetObservable(value, cLabel, CLabelImageProperty.class);
	}

	@Test
	public void testObserveImageOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeImage(item);
		assertWidgetObservable(value, item, ItemImageProperty.class);
	}

	@Test
	public void testObserveImageOfLabel() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeImage(label);
		assertWidgetObservable(value, label, LabelImageProperty.class);
	}

	@Test
	public void testObserveTooltipOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeTooltipText(item);
		assertWidgetObservable(value, item, CTabItemTooltipTextProperty.class);
	}

	@Test
	public void testObserveTooltipOfUnsupportedControl() throws Exception {
		ToolTip ttip = new ToolTip(shell, SWT.NONE);
		try {
			SWTObservables.observeTooltipText(ttip);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testObserveTooltipOfControl() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeTooltipText(label);
		assertWidgetObservable(value, label, ControlTooltipTextProperty.class);
	}

	@Test
	public void testObserveItemsOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableList list = (ISWTObservableList) SWTObservables
				.observeItems(combo);
		assertWidgetObservable(list, combo, ComboItemsProperty.class);
	}

	@Test
	public void testObserveItemsOfCCombo() throws Exception {
		CCombo ccombo = new CCombo(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableList list = (ISWTObservableList) SWTObservables
				.observeItems(ccombo);
		assertWidgetObservable(list, ccombo, CComboItemsProperty.class);
	}

	@Test
	public void testObserveItemsOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableList observableList = (ISWTObservableList) SWTObservables
				.observeItems(list);
		assertWidgetObservable(observableList, list, ListItemsProperty.class);
	}

	@Test
	public void testObserveItemsOfUnsupportedControl() throws Exception {
		Table table = new Table(shell, SWT.NONE);
		try {
			SWTObservables.observeItems(table);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testObserveSingleSelectionIndexOfTable() throws Exception {
		Table table = new Table(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSingleSelectionIndex(table);
		assertWidgetObservable(value, table,
				TableSingleSelectionIndexProperty.class);
	}

	@Test
	public void testObserveSingleSelectionIndexOfCCombo_DeselectAll()
			throws Exception {
		CCombo cCombo = new CCombo(shell, SWT.NONE);
		cCombo.add("item");
		cCombo.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(cCombo);
		assertEquals(0, cCombo.getSelectionIndex());
		value.setValue(-1);
		assertEquals(-1, cCombo.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfCCombo_SetValueNull()
			throws Exception {
		CCombo cCombo = new CCombo(shell, SWT.NONE);
		cCombo.add("item");
		cCombo.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(cCombo);
		assertEquals(0, cCombo.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, cCombo.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfCombo_DeselectAll()
			throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		combo.add("item");
		combo.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(combo);
		assertEquals(0, combo.getSelectionIndex());
		value.setValue(-1);
		assertEquals(-1, combo.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfCombo_SetValueNull()
			throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		combo.add("item");
		combo.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(combo);
		assertEquals(0, combo.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, combo.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfList_DeselectAll()
			throws Exception {
		List list = new List(shell, SWT.NONE);
		list.add("item");
		list.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(list);
		assertEquals(0, list.getSelectionIndex());
		value.setValue(Integer.valueOf(-1));
		assertEquals(-1, list.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfList_SetValueNull()
			throws Exception {
		List list = new List(shell, SWT.NONE);
		list.add("item");
		list.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(list);
		assertEquals(0, list.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, list.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfTable_DeselectAll()
			throws Exception {
		Table table = new Table(shell, SWT.NONE);
		new TableItem(table, SWT.NONE);
		table.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(table);
		assertEquals(0, table.getSelectionIndex());
		value.setValue(Integer.valueOf(-1));
		assertEquals(-1, table.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfTable_SetValueNull()
			throws Exception {
		Table table = new Table(shell, SWT.NONE);
		new TableItem(table, SWT.NONE);
		table.select(0);

		IObservableValue<Integer> value = WidgetProperties.singleSelectionIndex().observe(table);
		assertEquals(0, table.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, table.getSelectionIndex());
	}

	@Test
	public void testObserveSingleSelectionIndexOfUnsupportedControl()
			throws Exception {
		Tree tree = new Tree(shell, SWT.NONE);
		try {
			SWTObservables.observeSingleSelectionIndex(tree);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void testObserveMinOfSpinner() throws Exception {
		Spinner spinner = new Spinner(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeMin(spinner);
		assertWidgetObservable(value, spinner, SpinnerMinimumProperty.class);
	}

	@Test
	public void testObserveMinOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeMin(scale);
		assertWidgetObservable(value, scale, ScaleMinimumProperty.class);
	}

	@Test
	public void testObserveMinOfUnsupportedControl() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		try {
			SWTObservables.observeMin(text);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testObserveMaxOfSpinner() throws Exception {
		Spinner spinner = new Spinner(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeMax(spinner);
		assertWidgetObservable(value, spinner, SpinnerMaximumProperty.class);
	}

	@Test
	public void testObserveMaxOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeMax(scale);
		assertWidgetObservable(value, scale, ScaleMaximumProperty.class);
	}

	@Test
	public void testObserveMaxOfUnsupportedControl() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		try {
			SWTObservables.observeMax(text);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testObserveEditableOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeEditable(text);
		assertWidgetObservable(value, text, TextEditableProperty.class);
	}

	@Test
	public void testObserveEditableOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeEditable(combo);
		assertWidgetObservable(value, combo, CComboEditableProperty.class);
	}

	@Test
	public void testObserveEditableOfStyledText() throws Exception {
		StyledText styledText = new StyledText(shell, SWT.NONE);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeEditable(styledText);
		assertWidgetObservable(value, styledText,
				StyledTextEditableProperty.class);
	}

	@Test
	public void testObserveEnabledOfMenu() throws Exception {
		Menu menu = new Menu(shell, SWT.BAR);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeEnabled(menu);
		assertWidgetObservable(value, menu, MenuEnabledProperty.class);
	}

	@Test
	public void testObserveEnabledOfMenuItem() throws Exception {
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeEnabled(item);
		assertWidgetObservable(value, item, MenuItemEnabledProperty.class);
	}

	@Test
	public void testObserveSelectionOfMenuItem() throws Exception {
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeSelection(item);
		assertWidgetObservable(value, item, MenuItemSelectionProperty.class);
	}

	@Test
	public void testObserveEnabledOfScrollBar() throws Exception {
		ScrollBar scrollBar = shell.getVerticalBar();
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeEnabled(scrollBar);
		assertWidgetObservable(value, scrollBar, ScrollBarEnabledProperty.class);
	}

	@Test
	public void testObserveEnabledOfToolItem() throws Exception {
		ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL);
		ToolItem item = new ToolItem(bar, SWT.PUSH);
		@SuppressWarnings("rawtypes")
		ISWTObservableValue value = SWTObservables.observeEnabled(item);
		assertWidgetObservable(value, item, ToolItemEnabledProperty.class);
	}

	private void assertWidgetObservable(ISWTObservable observable, Widget widget, Class<?> propertyClass) {
		assertNotNull(observable);
		assertTrue(observable.getWidget() == widget);
		IPropertyObservable<?> propertyObservable = getPropertyObservable(observable);
		assertTrue(propertyClass.isInstance(propertyObservable.getProperty()));
	}

	@Test
	public void testObserveEditableOfUnsupportedControl() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		try {
			SWTObservables.observeEditable(label);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
