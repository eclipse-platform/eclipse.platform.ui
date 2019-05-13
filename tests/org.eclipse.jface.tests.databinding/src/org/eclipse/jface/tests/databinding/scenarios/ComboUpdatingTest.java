/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 260329
 *******************************************************************************/

package org.eclipse.jface.tests.databinding.scenarios;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @since 3.2
 */
public class ComboUpdatingTest extends ScenariosTestCase {

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
				newValue);
	}


	private Combo comboEditable;
	//private Combo comboReadOnly;

	private static final String PROP_TEXT = "text";
	private String text = "Hello, world";

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	private static final String PROP_CHOICES = "choices";
	private List<String> choices = new ArrayList<>();
	{
		choices.add("Banana");
		choices.add("Apple");
		choices.add("Mango");
	}

	public List<String> getChoices() {
		return choices;
	}

	public void setChoices(List<String> choices) {
		this.choices = choices;
		firePropertyChange(PROP_CHOICES, null, null);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		getComposite().setLayout(new FillLayout());
		comboEditable = new Combo(getComposite(), SWT.DROP_DOWN);
//		comboReadOnly = new Combo(getComposite(), SWT.DROP_DOWN | SWT.READ_ONLY);
	}

	//-------------------------------------------------------------------------

	private static final String NEXT = "Next";
	@Test
	public void testBindText() throws Exception {
		getDbc().bindValue(WidgetProperties.text().observe(comboEditable),
				BeanProperties.value(ComboUpdatingTest.class, PROP_TEXT).observe(this));
		spinEventLoop(0);
		assertEquals("Should find value of text", text, comboEditable.getText());
		comboEditable.setText(NEXT);
		spinEventLoop(0);
		assertEquals("Should find new value in text", NEXT, text);
	}

	@Test
	@Ignore
	public void testBindItems_listHasSameItems_editable() throws Exception {
		text = "Apple";

		getDbc().bindValue(WidgetProperties.text().observe(comboEditable),
				BeanProperties.value(ComboUpdatingTest.class, "text").observe(this));

		spinEventLoop(0);
		assertEquals("Should find value of text", text, comboEditable.getText());

		IObservableList<String> list = new WritableList<>(getChoices(), null);
		getDbc().bindList(WidgetProperties.items().observe(comboEditable), list);

		spinEventLoop(0);
		int position = 0;
		for (String element : choices) {
			assertEquals(element, comboEditable.getItem(position));
			++position;
		}
//		assertEquals("Should find value of text", "Apple", text);
		assertEquals("Should find value of combo.getText()", "", comboEditable.getText());
		comboEditable.setText("Banana");
		spinEventLoop(0);
		assertEquals("Should find value of text", "Banana", text);
	}

//	@Test
	// public void testBindItems_listHasSameItems_readOnly() throws Exception {
//		text = "Apple";
//		ComboObservableValue value = (ComboObservableValue) getDbc().createObservable(new Property(comboReadOnly, PROP_TEXT));
//		getDbc().bind(value.getItems(), new Property(this, PROP_CHOICES), null);
////		getDbc().bind(combo, new Property(this, PROP_CHOICES, String.class, Boolean.TRUE), null);
//		spinEventLoop(0);
//		assertEquals("Should find value of text", "Apple", text);
// 		getDbc().bind(value, new Property(this, PROP_TEXT), null);
//		spinEventLoop(0);
//		assertEquals("Should find value of text", "Apple", text);
//		assertEquals("Should find value of combo.getText()", "Apple", comboReadOnly.getText());
//		int position = 0;
//		for (Iterator choicesIter = choices.iterator(); choicesIter.hasNext();) {
//			String element = (String) choicesIter.next();
//			assertEquals(element, comboReadOnly.getItem(position));
//			++position;
//		}
//		assertEquals("Should find value of text", "Apple", text);
//		assertEquals("Should find value of combo.getText()", "Apple", comboReadOnly.getText());
//		comboReadOnly.setText("Banana");
//		spinEventLoop(0);
//		assertEquals("Should find value of text", "Banana", text);
//	}

	@Test
	@Ignore
	public void testBindItems_listHasDifferentItems_editable() throws Exception {

		getDbc().bindValue(WidgetProperties.text().observe(comboEditable),
				BeanProperties.value(ComboUpdatingTest.class, "text").observe(this));

		spinEventLoop(0);
		assertEquals("Should find value of text", text, comboEditable.getText());

		IObservableList<String> list = new WritableList<>(new ArrayList<>(), String.class);
		list.addAll(getChoices());
		getDbc().bindList(WidgetProperties.items().observe(comboEditable), list);

		spinEventLoop(0);
		int position = 0;
		for (String element : choices) {
			assertEquals(element, comboEditable.getItem(position));
			++position;
		}
//		assertEquals("Should find value of text", "Hello, world", text);
		assertEquals("Should find value of combo.getText()", "", comboEditable.getText());
		comboEditable.setText("Banana");
		spinEventLoop(0);
		assertEquals("Should find value of text", "Banana", text);
	}

//	@Test
	// public void testBindItems_listHasDifferentItems_readOnly() throws
	// Exception {
//		ComboObservableValue value = (ComboObservableValue) getDbc().createObservable(new Property(comboReadOnly, PROP_TEXT));
//		getDbc().bind(value, new Property(this, PROP_TEXT), null);
//		spinEventLoop(0);
//		getDbc().bind(value.getItems(), new Property(this, PROP_CHOICES), null);
////		getDbc().bind(combo, new Property(this, PROP_CHOICES, String.class, Boolean.TRUE), null);
//		spinEventLoop(0);
//		int position = 0;
//		for (Iterator choicesIter = choices.iterator(); choicesIter.hasNext();) {
//			String element = (String) choicesIter.next();
//			assertEquals(element, comboReadOnly.getItem(position));
//			++position;
//		}
////		assertEquals("Should find value of text", "Hello, world", text);
//		assertEquals("Should find value of combo.getText()", "", comboReadOnly.getText());
//		comboReadOnly.setText("Banana");
//		spinEventLoop(0);
//		assertEquals("Should find value of text", "Banana", text);
//	}

}
