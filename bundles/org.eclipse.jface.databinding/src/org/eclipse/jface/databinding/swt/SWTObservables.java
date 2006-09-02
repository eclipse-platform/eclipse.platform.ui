/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.swt;

import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ButtonObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.CLabelObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ControlObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.LabelObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SpinnerObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TableObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTProperties;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * A factory for creating observables for SWT widgets
 * 
 * @since 1.1
 * 
 */
public class SWTObservables {

	/**
	 * @param control
	 * @return
	 */
	public static ISWTObservableValue getEnabled(Control control) {
		return new ControlObservableValue(control, SWTProperties.ENABLED);
	}

	/**
	 * @param control
	 * @return
	 */
	public static ISWTObservableValue getVisisble(Control control) {
		return new ControlObservableValue(control, SWTProperties.VISIBLE);
	}

	/**
	 * @param spinner
	 * @return
	 */
	public static ISWTObservableValue getSelection(Spinner spinner) {
		return new SpinnerObservableValue(spinner, SWTProperties.SELECTION);
	}

	/**
	 * @param spinner
	 * @return
	 */
	public static ISWTObservableValue getMin(Spinner spinner) {
		return new SpinnerObservableValue(spinner, SWTProperties.MIN);
	}

	/**
	 * @param spinner
	 * @return
	 */
	public static ISWTObservableValue getMax(Spinner spinner) {
		return new SpinnerObservableValue(spinner, SWTProperties.MAX);
	}

	/**
	 * @param text
	 * @param event
	 * @return
	 */
	public static ISWTObservableValue getText(Text text, int event) {
		return new TextObservableValue(text, event);
	}

	/**
	 * @param label
	 * @return
	 */
	public static ISWTObservableValue getText(Label label) {
		return new LabelObservableValue(label);
	}

	/**
	 * @param cLabel
	 * @return
	 */
	public static ISWTObservableValue getText(CLabel cLabel) {
		return new CLabelObservableValue(cLabel);
	}

	/**
	 * @param button
	 * @return
	 */
	public static ISWTObservableValue getSelection(Button button) {
		return new ButtonObservableValue(button);
	}

	/**
	 * @param combo
	 * @param event
	 * @return
	 */
	public static ISWTObservableValue getText(Combo combo) {
		return new ComboObservableValue(combo, SWTProperties.TEXT);
	}

	/**
	 * @param combo
	 * @return
	 */
	public static ISWTObservableValue getSelection(Combo combo) {
		return new ComboObservableValue(combo, SWTProperties.SELECTION);
	}

	/**
	 * @param combo
	 * @return
	 */
	public static IObservableList getItems(Combo combo) {
		return new ComboObservableList(combo);
	}

	/**
	 * @param combo
	 * @param event
	 * @return
	 */
	public static ISWTObservableValue getText(CCombo combo) {
		return new CComboObservableValue(combo, SWTProperties.TEXT);
	}

	/**
	 * @param combo
	 * @return
	 */
	public static ISWTObservableValue getSelection(CCombo combo) {
		return new CComboObservableValue(combo, SWTProperties.SELECTION);
	}

	/**
	 * @param combo
	 * @return
	 */
	public static IObservableList getItems(CCombo combo) {
		return new CComboObservableList(combo);
	}

	/**
	 * @param list
	 * @return
	 */
	public static ISWTObservableValue getSelection(List list) {
		return new ListObservableValue(list);
	}

	/**
	 * @param list
	 * @return
	 */
	public static IObservableList getItems(List list) {
		return new ListObservableList(list);
	}

	/**
	 * @param table
	 * @return
	 */
	public static ISWTObservableValue getSingleSelectionIndex(Table table) {
		return new TableObservableValue(table, SWTProperties.SELECTION);
	}

}