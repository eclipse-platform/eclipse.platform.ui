/*******************************************************************************
 * Copyright (c) 2007, 2009, 2011 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Chris Aniszczyk <zx@code9.com> - bug 131435
 *     Matthew Hall - bugs 248621, 213893, 262320, 169876, 306203
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.swt;

import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.swt.ISWTObservableList;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
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

/**
 * @since 1.1
 */
public class SWTObservablesTest extends AbstractSWTTestCase {
	private Shell shell;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		shell = getShell();
		RealmTester.setDefault(SWTObservables.getRealm(shell.getDisplay()));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		RealmTester.setDefault(null);
	}

	@Override
	protected Shell getShell() {
		if (shell == null) {
			shell = new Shell(SWT.V_SCROLL);
		}
		return shell;
	}

	public void testObserveForeground() throws Exception {
		ISWTObservableValue value = SWTObservables.observeForeground(shell);
		assertWidgetObservable(value, shell, ControlForegroundProperty.class);
		assertEquals(Color.class, value.getValueType());
	}

	public void testObserveBackground() throws Exception {
		ISWTObservableValue value = SWTObservables.observeBackground(shell);
		assertWidgetObservable(value, shell, ControlBackgroundProperty.class);
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
		assertWidgetObservable(value, spinner, SpinnerSelectionProperty.class);
	}

	public void testObserveSelectionOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeSelection(button);
		assertWidgetObservable(value, button, ButtonSelectionProperty.class);
	}

	public void testObserveSelectionOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertWidgetObservable(value, combo, ComboSelectionProperty.class);
	}

	public void testObserveSelectionOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertWidgetObservable(value, combo, CComboSelectionProperty.class);
	}

	public void testObserveSelectionOfDateTime_Date() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.DATE);
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertWidgetObservable(value, dateTime, DateTimeSelectionProperty.class);
	}

	public void testObserveSelectionOfDateTime_Calendar() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.CALENDAR);
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertWidgetObservable(value, dateTime, DateTimeSelectionProperty.class);
	}

	public void testObserveSelectionOfDateTime_Time() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.TIME);
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertWidgetObservable(value, dateTime, DateTimeSelectionProperty.class);
	}

	public void testObserveSelectionOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(list);
		assertWidgetObservable(value, list, ListSelectionProperty.class);
	}

	public void testObserveSelectionOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(scale);
		assertWidgetObservable(value, scale, ScaleSelectionProperty.class);
	}

	public void testObserveSelectionOfUnsupportedControl() throws Exception {
		try {
			Text text = new Text(shell, SWT.NONE);
			SWTObservables.observeSelection(text);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testObserveTextWithEventOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		assertFalse(text.isListening(SWT.FocusOut));

		ISWTObservableValue value = SWTObservables.observeText(text,
				SWT.FocusOut);
		assertWidgetObservable(value, text, TextTextProperty.class);

		assertFalse(text.isListening(SWT.FocusOut));
		ChangeEventTracker.observe(value);
		assertTrue(text.isListening(SWT.FocusOut));
	}

	public void testObserveTextOfStyledText() throws Exception {
		StyledText text = new StyledText(shell, SWT.NONE);
		assertFalse(text.isListening(SWT.FocusOut));

		ISWTObservableValue value = SWTObservables.observeText(text,
				SWT.FocusOut);
		assertWidgetObservable(value, text, StyledTextTextProperty.class);

		assertFalse(text.isListening(SWT.FocusOut));
		ChangeEventTracker.observe(value);
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

	public void testObserveTextOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeText(button);
		assertWidgetObservable(value, button, ButtonTextProperty.class);
	}

	public void testObserveTextOfLabel() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertWidgetObservable(value, label, LabelTextProperty.class);
	}

	public void testObserveTextOfCLabel() throws Exception {
		CLabel label = new CLabel(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertWidgetObservable(value, label, CLabelTextProperty.class);
	}

	public void testObserveTextOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertWidgetObservable(value, combo, ComboTextProperty.class);
	}

	/**
	 * @param observable
	 * @return
	 */
	private IPropertyObservable getPropertyObservable(ISWTObservable observable) {
		IDecoratingObservable decoratingObservable = (IDecoratingObservable) observable;
		IPropertyObservable propertyObservable = (IPropertyObservable) decoratingObservable
				.getDecorated();
		return propertyObservable;
	}

	public void testObserveTextOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertWidgetObservable(value, combo, CComboTextProperty.class);
	}

	public void testObserveTextOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(text);

		assertWidgetObservable(value, text, TextTextProperty.class);

		assertFalse(text.isListening(SWT.Modify));
		assertFalse(text.isListening(SWT.FocusOut));
	}

	public void testObserveTextOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(item);
		assertWidgetObservable(value, item, ItemTextProperty.class);
	}

	public void testObserveTextOfUnsupportedControl() throws Exception {
		Table table = new Table(shell, SWT.NONE);
		try {
			SWTObservables.observeText(table);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testObserveImageOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeImage(button);
		assertWidgetObservable(value, button, ButtonImageProperty.class);
	}

	public void testObserveImageOfCLabel() throws Exception {
		CLabel cLabel = new CLabel(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeImage(cLabel);
		assertWidgetObservable(value, cLabel, CLabelImageProperty.class);
	}

	public void testObserveImageOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeImage(item);
		assertWidgetObservable(value, item, ItemImageProperty.class);
	}

	public void testObserveImageOfLabel() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeImage(label);
		assertWidgetObservable(value, label, LabelImageProperty.class);
	}

	public void testObserveTooltipOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeTooltipText(item);
		assertWidgetObservable(value, item, CTabItemTooltipTextProperty.class);
	}

	public void testObserveTooltipOfUnsupportedControl() throws Exception {
		ToolTip ttip = new ToolTip(shell, SWT.NONE);
		try {
			SWTObservables.observeTooltipText(ttip);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testObserveTooltipOfControl() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeTooltipText(label);
		assertWidgetObservable(value, label, ControlTooltipTextProperty.class);
	}

	public void testObserveItemsOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		ISWTObservableList list = (ISWTObservableList) SWTObservables
				.observeItems(combo);
		assertWidgetObservable(list, combo, ComboItemsProperty.class);
	}

	public void testObserveItemsOfCCombo() throws Exception {
		CCombo ccombo = new CCombo(shell, SWT.NONE);
		ISWTObservableList list = (ISWTObservableList) SWTObservables
				.observeItems(ccombo);
		assertWidgetObservable(list, ccombo, CComboItemsProperty.class);
	}

	public void testObserveItemsOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		ISWTObservableList observableList = (ISWTObservableList) SWTObservables
				.observeItems(list);
		assertWidgetObservable(observableList, list, ListItemsProperty.class);
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
		assertWidgetObservable(value, table,
				TableSingleSelectionIndexProperty.class);
	}

	public void testObserveSingleSelectionIndexOfCCombo_DeselectAll()
			throws Exception {
		CCombo cCombo = new CCombo(shell, SWT.NONE);
		cCombo.add("item");
		cCombo.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(cCombo);
		assertEquals(0, cCombo.getSelectionIndex());
		value.setValue(new Integer(-1));
		assertEquals(-1, cCombo.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfCCombo_SetValueNull()
			throws Exception {
		CCombo cCombo = new CCombo(shell, SWT.NONE);
		cCombo.add("item");
		cCombo.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(cCombo);
		assertEquals(0, cCombo.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, cCombo.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfCombo_DeselectAll()
			throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		combo.add("item");
		combo.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(combo);
		assertEquals(0, combo.getSelectionIndex());
		value.setValue(new Integer(-1));
		assertEquals(-1, combo.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfCombo_SetValueNull()
			throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		combo.add("item");
		combo.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(combo);
		assertEquals(0, combo.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, combo.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfList_DeselectAll()
			throws Exception {
		List list = new List(shell, SWT.NONE);
		list.add("item");
		list.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(list);
		assertEquals(0, list.getSelectionIndex());
		value.setValue(new Integer(-1));
		assertEquals(-1, list.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfList_SetValueNull()
			throws Exception {
		List list = new List(shell, SWT.NONE);
		list.add("item");
		list.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(list);
		assertEquals(0, list.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, list.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfTable_DeselectAll()
			throws Exception {
		Table table = new Table(shell, SWT.NONE);
		new TableItem(table, SWT.NONE);
		table.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(table);
		assertEquals(0, table.getSelectionIndex());
		value.setValue(new Integer(-1));
		assertEquals(-1, table.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfTable_SetValueNull()
			throws Exception {
		Table table = new Table(shell, SWT.NONE);
		new TableItem(table, SWT.NONE);
		table.select(0);

		ISWTObservableValue value = WidgetProperties.singleSelectionIndex()
				.observe(table);
		assertEquals(0, table.getSelectionIndex());
		value.setValue(null);
		assertEquals(-1, table.getSelectionIndex());
	}

	public void testObserveSingleSelectionIndexOfUnsupportedControl()
			throws Exception {
		Tree tree = new Tree(shell, SWT.NONE);
		try {
			SWTObservables.observeSingleSelectionIndex(tree);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {

		}
	}

	public void testObserveMinOfSpinner() throws Exception {
		Spinner spinner = new Spinner(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMin(spinner);
		assertWidgetObservable(value, spinner, SpinnerMinimumProperty.class);
	}

	public void testObserveMinOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMin(scale);
		assertWidgetObservable(value, scale, ScaleMinimumProperty.class);
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
		assertWidgetObservable(value, spinner, SpinnerMaximumProperty.class);
	}

	public void testObserveMaxOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMax(scale);
		assertWidgetObservable(value, scale, ScaleMaximumProperty.class);
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
		assertWidgetObservable(value, text, TextEditableProperty.class);
	}

	public void testObserveEditableOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeEditable(combo);
		assertWidgetObservable(value, combo, CComboEditableProperty.class);
	}

	public void testObserveEditableOfStyledText() throws Exception {
		StyledText styledText = new StyledText(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeEditable(styledText);
		assertWidgetObservable(value, styledText,
				StyledTextEditableProperty.class);
	}

	public void testObserveEnabledOfMenu() throws Exception {
		Menu menu = new Menu(shell, SWT.BAR);
		ISWTObservableValue value = SWTObservables.observeEnabled(menu);
		assertWidgetObservable(value, menu, MenuEnabledProperty.class);
	}

	public void testObserveEnabledOfMenuItem() throws Exception {
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeEnabled(item);
		assertWidgetObservable(value, item, MenuItemEnabledProperty.class);
	}

	public void testObserveSelectionOfMenuItem() throws Exception {
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeSelection(item);
		assertWidgetObservable(value, item, MenuItemSelectionProperty.class);
	}

	public void testObserveEnabledOfScrollBar() throws Exception {
		ScrollBar scrollBar = shell.getVerticalBar();
		ISWTObservableValue value = SWTObservables.observeEnabled(scrollBar);
		assertWidgetObservable(value, scrollBar, ScrollBarEnabledProperty.class);
	}

	public void testObserveEnabledOfToolItem() throws Exception {
		ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL);
		ToolItem item = new ToolItem(bar, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeEnabled(item);
		assertWidgetObservable(value, item, ToolItemEnabledProperty.class);
	}

	private void assertWidgetObservable(ISWTObservable observable,
			Widget widget, Class propertyClass) {
		assertNotNull(observable);
		assertTrue(observable.getWidget() == widget);
		IPropertyObservable propertyObservable = getPropertyObservable(observable);
		assertTrue(propertyClass.isInstance(propertyObservable.getProperty()));
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
