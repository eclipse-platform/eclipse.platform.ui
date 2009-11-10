/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Chris Aniszczyk <zx@code9.com> - bug 131435
 *     Matthew Hall - bugs 248621, 213893, 262320, 169876
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.swt;

import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.internal.databinding.swt.ButtonImageProperty;
import org.eclipse.jface.internal.databinding.swt.ButtonTextProperty;
import org.eclipse.jface.internal.databinding.swt.CComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.CComboTextProperty;
import org.eclipse.jface.internal.databinding.swt.CLabelImageProperty;
import org.eclipse.jface.internal.databinding.swt.CLabelTextProperty;
import org.eclipse.jface.internal.databinding.swt.CTabItemTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.ComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ComboTextProperty;
import org.eclipse.jface.internal.databinding.swt.ControlTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.DateTimeSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ItemImageProperty;
import org.eclipse.jface.internal.databinding.swt.ItemTextProperty;
import org.eclipse.jface.internal.databinding.swt.LabelImageProperty;
import org.eclipse.jface.internal.databinding.swt.LabelTextProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleMaximumProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleMinimumProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerMaximumProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerMinimumProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.StyledTextTextProperty;
import org.eclipse.jface.internal.databinding.swt.TableSingleSelectionIndexProperty;
import org.eclipse.jface.internal.databinding.swt.TextEditableProperty;
import org.eclipse.jface.internal.databinding.swt.TextTextProperty;
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
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tree;

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
		assertTrue(value.getWidget() == spinner);

		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof SpinnerSelectionProperty);
	}

	public void testObserveSelectionOfButton() throws Exception {
		Button button = new Button(shell, SWT.PUSH);
		ISWTObservableValue value = SWTObservables.observeSelection(button);
		assertNotNull(value);
		assertTrue(value.getWidget() == button);
	}

	public void testObserveSelectionOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertNotNull(value);
		assertTrue(value.getWidget() == combo);

		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ComboSelectionProperty);
	}

	public void testObserveSelectionOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(combo);
		assertNotNull(value);
		assertTrue(value.getWidget() == combo);

		IPropertyObservable property = getPropertyObservable(value);
		assertTrue(property.getProperty() instanceof CComboSelectionProperty);
	}

	public void testObserveSelectionOfDateTime_Date() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.DATE);
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertNotNull(value);
		assertTrue(value.getWidget() == dateTime);
		IPropertyObservable property = getPropertyObservable(value);
		assertTrue(property.getProperty() instanceof DateTimeSelectionProperty);
	}

	public void testObserveSelectionOfDateTime_Calendar() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.CALENDAR);
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertNotNull(value);
		assertTrue(value.getWidget() == dateTime);
		IPropertyObservable property = getPropertyObservable(value);
		assertTrue(property.getProperty() instanceof DateTimeSelectionProperty);
	}

	public void testObserveSelectionOfDateTime_Time() throws Exception {
		DateTime dateTime = new DateTime(shell, SWT.TIME);
		ISWTObservableValue value = SWTObservables.observeSelection(dateTime);
		assertNotNull(value);
		assertTrue(value.getWidget() == dateTime);
		IPropertyObservable property = getPropertyObservable(value);
		assertTrue(property.getProperty() instanceof DateTimeSelectionProperty);
	}

	public void testObserveSelectionOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(list);
		assertNotNull(value);
		assertTrue(value.getWidget() == list);
	}

	public void testObserveSelectionOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeSelection(scale);
		assertNotNull(value);
		assertTrue(value.getWidget() == scale);

		IPropertyObservable property = getPropertyObservable(value);
		assertTrue(property.getProperty() instanceof ScaleSelectionProperty);
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
		assertNotNull(value);
		assertTrue(value.getWidget() == text);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof TextTextProperty);

		assertFalse(text.isListening(SWT.FocusOut));
		ChangeEventTracker.observe(value);
		assertTrue(text.isListening(SWT.FocusOut));
	}

	public void testObserveTextOfStyledText() throws Exception {
		StyledText text = new StyledText(shell, SWT.NONE);
		assertFalse(text.isListening(SWT.FocusOut));

		ISWTObservableValue value = SWTObservables.observeText(text,
				SWT.FocusOut);
		assertNotNull(value);
		assertTrue(value.getWidget() == text);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof StyledTextTextProperty);

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
		assertNotNull(button);
		assertTrue(value.getWidget() == button);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ButtonTextProperty);
	}

	public void testObserveTextOfLabel() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertNotNull(label);
		assertTrue(value.getWidget() == label);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof LabelTextProperty);
	}

	public void testObserveTextOfCLabel() throws Exception {
		CLabel label = new CLabel(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(label);
		assertNotNull(label);
		assertTrue(value.getWidget() == label);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof CLabelTextProperty);
	}

	public void testObserveTextOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertNotNull(value);
		assertTrue(value.getWidget() == combo);

		assertTrue(getPropertyObservable(value).getProperty() instanceof ComboTextProperty);
	}

	/**
	 * @param observable
	 * @return
	 */
	private IPropertyObservable getPropertyObservable(
			ISWTObservableValue observable) {
		IDecoratingObservable decoratingObservable = (IDecoratingObservable) observable;
		IPropertyObservable propertyObservable = (IPropertyObservable) decoratingObservable
				.getDecorated();
		return propertyObservable;
	}

	public void testObserveTextOfCCombo() throws Exception {
		CCombo combo = new CCombo(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(combo);
		assertNotNull(value);
		assertTrue(value.getWidget() == combo);

		IDecoratingObservable decorating = (IDecoratingObservable) value;
		IPropertyObservable property = (IPropertyObservable) decorating
				.getDecorated();
		assertTrue(property.getProperty() instanceof CComboTextProperty);
	}

	public void testObserveTextOfText() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(text);
		assertNotNull(value);

		assertTrue(value.getWidget() == text);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof TextTextProperty);

		assertFalse(text.isListening(SWT.Modify));
		assertFalse(text.isListening(SWT.FocusOut));
	}

	public void testObserveTextOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeText(item);
		assertNotNull(value);
		assertTrue(value.getWidget() == item);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ItemTextProperty);
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
		assertNotNull(button);
		assertTrue(value.getWidget() == button);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ButtonImageProperty);
	}

	public void testObserveImageOfCLabel() throws Exception {
		CLabel cLabel = new CLabel(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeImage(cLabel);
		assertNotNull(cLabel);
		assertTrue(value.getWidget() == cLabel);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof CLabelImageProperty);
	}

	public void testObserveImageOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeImage(item);
		assertNotNull(item);
		assertTrue(value.getWidget() == item);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ItemImageProperty);
	}

	public void testObserveImageOfLabel() throws Exception {
		Label label = new Label(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeImage(label);
		assertNotNull(label);
		assertTrue(value.getWidget() == label);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof LabelImageProperty);
	}

	public void testObserveTooltipOfItem() throws Exception {
		CTabFolder ctf = new CTabFolder(shell, SWT.NONE);
		Item item = new CTabItem(ctf, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeTooltipText(item);
		assertNotNull(value);
		assertTrue(value.getWidget() == item);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof CTabItemTooltipTextProperty);
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
		assertNotNull(value);
		assertTrue(value.getWidget() == label);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ControlTooltipTextProperty);
	}

	public void testObserveItemsOfCombo() throws Exception {
		Combo combo = new Combo(shell, SWT.NONE);
		IObservableList list = SWTObservables.observeItems(combo);
		assertNotNull(list);
		assertTrue(list instanceof ISWTObservable);
		assertTrue(((ISWTObservable) list).getWidget() == combo);
	}

	public void testObserveItemsOfCCombo() throws Exception {
		CCombo ccombo = new CCombo(shell, SWT.NONE);
		IObservableList list = SWTObservables.observeItems(ccombo);
		assertNotNull(list);
		ISWTObservable swtObservable = (ISWTObservable) list;
		assertTrue(swtObservable.getWidget() == ccombo);
	}

	public void testObserveItemsOfList() throws Exception {
		List list = new List(shell, SWT.NONE);
		IObservableList observableList = SWTObservables.observeItems(list);
		assertNotNull(observableList);
		ISWTObservable swtObservable = (ISWTObservable) observableList;
		assertTrue(swtObservable.getWidget() == list);
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
		assertTrue(value.getWidget() == table);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof TableSingleSelectionIndexProperty);
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
		assertNotNull(value);
		assertTrue(value.getWidget() == spinner);

		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof SpinnerMinimumProperty);
	}

	public void testObserveMinOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMin(scale);
		assertNotNull(value);
		assertTrue(value.getWidget() == scale);

		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ScaleMinimumProperty);
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
		assertTrue(value.getWidget() == spinner);

		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof SpinnerMaximumProperty);
	}

	public void testObserveMaxOfScale() throws Exception {
		Scale scale = new Scale(shell, SWT.NONE);
		ISWTObservableValue value = SWTObservables.observeMax(scale);
		assertNotNull(value);
		assertTrue(value.getWidget() == scale);

		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof ScaleMaximumProperty);
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
		assertTrue(value.getWidget() == text);
		IPropertyObservable propertyObservable = getPropertyObservable(value);
		assertTrue(propertyObservable.getProperty() instanceof TextEditableProperty);
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
