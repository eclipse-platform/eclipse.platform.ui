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
package org.eclipse.jface.internal.databinding.provisional.swt;

import org.eclipse.jface.internal.databinding.internal.swt.ButtonObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ControlObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.LabelObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SpinnerObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TableObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * A factory that supports binding to SWT controls. This factory supports the
 * following description objects:
 * <ul>
 * <li>org.eclipse.swt.widgets.Text - denotes the text's text property</li>
 * <li>org.eclipse.swt.widgets.Button - denotes the button's selection property</li>
 * <li>org.eclipse.swt.widgets.Combo - denotes the combo's items collection</li>
 * <li>org.eclipse.swt.widgets.CCombo - denotes the ccombo's items collection</li>
 * <li>org.eclipse.swt.widgets.List - denotes the list's items collection</li>
 * <li>org.eclipse.jface.databinding.PropertyDescription - depending on the
 * property description's object and property ID:
 * <ul>
 * <li>object instanceof Widget, property ID is SWT_ENABLED - denoting the
 * widget's enabled property</li>
 * <li>object instanceof Spinner, property ID is SWT_SELECTION - denoting the
 * spinner's selection property</li>
 * <li>object instanceof Spinner, property ID is SWT_MINIMUM - denoting the
 * spinner's minimum property</li>
 * <li>object instanceof Spinner, property ID is SWT_MAXIMUM - denoting the
 * spinner's maximum property</li>
 * </ul>
 * </li>
 * </ul>
 * TODO complete the list
 * 
 * @since 1.0
 * 
 */
final public class SWTObservableFactory implements IObservableFactory {

	private int updateTime = DataBindingContext.TIME_LATE;

	/**
	 * Create a factory that can create observables for SWT controls
	 */
	public SWTObservableFactory() {
	}

	/**
	 * @param updateTime
	 *            The update policy of IDataBineingContext.TIME_LATE or
	 *            DataBindingContext.TIME_EARLY is a hint that some editable
	 *            controls may implement (such as Text) to determine when to
	 *            fire updates
	 */
	public SWTObservableFactory(int updateTime) {
		this.updateTime = updateTime;
	}

	public IObservable createObservable(Object description) {
		if (description instanceof Property) {
			Object object = ((Property) description).getObject();
			Object attribute = ((Property) description).getPropertyID();
			if (object instanceof Control
					&& SWTProperties.ENABLED.equals(attribute)) {
				return new ControlObservableValue((Control) object,
						(String) attribute);
			}
			if (object instanceof Control
					&& SWTProperties.VISIBLE.equals(attribute)) {
				return new ControlObservableValue((Control) object,
						(String) attribute);
			}
			if (object instanceof Spinner
					&& (SWTProperties.SELECTION.equals(attribute)
							|| SWTProperties.MIN.equals(attribute) || SWTProperties.MAX
							.equals(attribute))) {
				return new SpinnerObservableValue((Spinner) object,
						(String) attribute);
			}
			if (object instanceof Text && SWTProperties.TEXT.equals(attribute)) {
				return new TextObservableValue((Text) object, SWT.Modify);
			}
			if (object instanceof Label && SWTProperties.TEXT.equals(attribute)) {
				return new LabelObservableValue((Label) object);
			}
			if (object instanceof Button
					&& SWTProperties.SELECTION.equals(attribute)) {
				return new ButtonObservableValue((Button) object);
			}
			if (object instanceof Combo
					&& (SWTProperties.TEXT.equals(attribute) || SWTProperties.SELECTION
							.equals(attribute))) {
				return new ComboObservableValue((Combo) object,
						(String) attribute);
			} else if (object instanceof Combo
					&& SWTProperties.ITEMS.equals(attribute)) {
				return new ComboObservableList((Combo) object);
			}
			if (object instanceof CCombo
					&& (SWTProperties.TEXT.equals(attribute) || SWTProperties.SELECTION
							.equals(attribute))) {
				return new CComboObservableValue((CCombo) object,
						(String) attribute);
			} else if (object instanceof CCombo
					&& SWTProperties.ITEMS.equals(attribute)) {
				return new CComboObservableList((CCombo) object);
			}
			if (object instanceof List
					&& SWTProperties.SELECTION.equals(attribute)) {
				// SWT.SINGLE selection only
				return new ListObservableValue((List) object);
			} else if (object instanceof List
					&& SWTProperties.ITEMS.equals(attribute)) {
				return new ListObservableList((List) object);
			}
			if (object instanceof Table) {
				return new TableObservableValue((Table) object,
						(String) attribute);
			}
		}
		if (description instanceof Text) {
			int updatePolicy = new int[] { SWT.Modify, SWT.FocusOut, SWT.None }[updateTime];
			return new TextObservableValue((Text) description, updatePolicy);
		} else if (description instanceof Button) {
			int updatePolicy = new int[] { SWT.Modify, SWT.FocusOut, SWT.None }[updateTime];
			return new ButtonObservableValue((Button) description);
		} else if (description instanceof Label) {
			return new LabelObservableValue((Label) description);
		} else if (description instanceof Combo) {
			return new ComboObservableList((Combo) description);
		} else if (description instanceof Spinner) {
			return new SpinnerObservableValue((Spinner) description,
					SWTProperties.SELECTION);
		} else if (description instanceof CCombo) {
			return new CComboObservableList((CCombo) description);
		} else if (description instanceof List) {
			return new ListObservableList((List) description);
		}
		return null;
	}

	/**
	 * @param time
	 *            Values are TIME_EARLY or TIME_LATE and specify when update
	 *            occurs For example TIME_EARLY on Text control and update
	 *            occurs per keystroke, TIME_LATE and validation occurs when the
	 *            field loses focus
	 */
	public void setUpdateTime(int time) {
		updateTime = time;
	}
}
