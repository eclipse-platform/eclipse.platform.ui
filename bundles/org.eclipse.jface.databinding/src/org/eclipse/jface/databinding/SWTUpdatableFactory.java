/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.databinding;

import java.util.Map;

import org.eclipse.jface.databinding.internal.swt.ButtonUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.CComboUpdatableCollection;
import org.eclipse.jface.databinding.internal.swt.CComboUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.ComboUpdatableCollection;
import org.eclipse.jface.databinding.internal.swt.ComboUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.ControlUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.LabelUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.ListUpdatableCollection;
import org.eclipse.jface.databinding.internal.swt.ListUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.SpinnerUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.TableUpdatableValue;
import org.eclipse.jface.databinding.internal.swt.TextUpdatableValue;
import org.eclipse.jface.databinding.internal.viewers.StructuredViewerUpdatableValue;
import org.eclipse.jface.databinding.internal.viewers.TableViewerUpdatableCollection;
import org.eclipse.jface.databinding.internal.viewers.TableViewerUpdatableCollectionExtended;
import org.eclipse.jface.databinding.internal.viewers.UpdatableCollectionViewer;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
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
 * A factory that supports binding to SWT controls. This
 * factory supports the following description objects:
 * <ul>
 * <li>org.eclipse.swt.widgets.Text - denotes the text's text property</li>
 * <li>org.eclipse.swt.widgets.Button - denotes the button's selection
 * property</li>
 * <li>org.eclipse.swt.widgets.Combo - denotes the combo's items collection</li>
 * <li>org.eclipse.swt.widgets.CCombo - denotes the ccombo's items
 * collection</li>
 * <li>org.eclipse.swt.widgets.List - denotes the list's items collection</li>
 * <li>org.eclipse.jface.databinding.PropertyDescription - depending on the
 * property description's object and property ID:
 * <ul>
 * <li>object instanceof Widget, property ID is SWT_ENABLED - denoting the
 * widget's enabled property</li>
 * <li>object instanceof Spinner, property ID is SWT_SELECTION - denoting
 * the spinner's selection property</li>
 * <li>object instanceof Spinner, property ID is SWT_MINIMUM - denoting the
 * spinner's minimum property</li>
 * <li>object instanceof Spinner, property ID is SWT_MAXIMUM - denoting the
 * spinner's maximum property</li>
 * </ul>
 * </li>
 * </ul>
 * TODO complete the list
 * @since 3.2
 *
 */
final public class SWTUpdatableFactory implements IUpdatableFactory {

	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should be triggered early, typically on each keystroke.
	 */
	public static final int TIME_EARLY = 0;
	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should be triggered late, typically on focus lost.
	 */
	public static final int TIME_LATE = 1;
	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should never be triggered. Note that this means that the updatable will
	 * not track the underlying widget's changes.
	 */
	public static final int TIME_NEVER = 2;
	
	private int updateTime = TIME_LATE;
	
	private int validateTime = TIME_EARLY;
	
	public IUpdatable createUpdatable(Map properties,
			Object description, IDataBindingContext bindingContext) {
		if (description instanceof PropertyDescription) {
			Object object = ((PropertyDescription) description)
					.getObject();
			Object attribute = ((PropertyDescription) description)
					.getPropertyID();
			if (object instanceof Control
					&& SWTProperties.ENABLED.equals(attribute)) {
				return new ControlUpdatableValue((Control) object,
						(String) attribute);
			}
			if (object instanceof Spinner
					&& (SWTProperties.SELECTION.equals(attribute)
					|| SWTProperties.MIN.equals(attribute)
					|| SWTProperties.MAX.equals(attribute))) {
				return new SpinnerUpdatableValue((Spinner) object,
						(String) attribute);
			}
			if (object instanceof Text
					&& SWTProperties.TEXT.equals(attribute)) {
				return new TextUpdatableValue((Text) object,
						SWT.Modify, SWT.Modify);
			}
			if (object instanceof Label
					&& SWTProperties.TEXT.equals(attribute)) {
				return new LabelUpdatableValue((Label) object);
			}
			if (object instanceof Button
					&& SWTProperties.SELECTION.equals(attribute)) {
				return new ButtonUpdatableValue((Button) object,
						SWT.Selection);
			}
			if (object instanceof Combo
					&& (SWTProperties.TEXT.equals(attribute) || SWTProperties.SELECTION
							.equals(attribute))) {
				return new ComboUpdatableValue((Combo) object,
						(String) attribute);
			} else if (object instanceof Combo
					&& SWTProperties.ITEMS.equals(attribute)) {
				return new ComboUpdatableCollection((Combo) object,
						(String) attribute);
			}
			if (object instanceof CCombo
					&& (SWTProperties.TEXT.equals(attribute) || SWTProperties.SELECTION
							.equals(attribute))) {
				return new CComboUpdatableValue((CCombo) object,
						(String) attribute);
			} else if (object instanceof CCombo
					&& SWTProperties.ITEMS.equals(attribute)) {
				return new CComboUpdatableCollection((CCombo) object,
						(String) attribute);
			}
			if (object instanceof List
					&& SWTProperties.SELECTION.equals(attribute)) {
				// SWT.SINGLE selection only
				return new ListUpdatableValue((List) object,
						(String) attribute);
			} else if (object instanceof List
					&& SWTProperties.ITEMS.equals(attribute)) {
				return new ListUpdatableCollection((List) object,
						(String) attribute);
			}
			if (object instanceof StructuredViewer
					&& SWTProperties.SELECTION.equals(attribute)) {
				return new StructuredViewerUpdatableValue(
						(StructuredViewer) object, (String) attribute);
			}
			if (object instanceof AbstractListViewer
					&& SWTProperties.SELECTION.equals(attribute))
				return new StructuredViewerUpdatableValue(
						(AbstractListViewer) object, (String) attribute);
			else if (object instanceof AbstractListViewer
					&& ViewersProperties.CONTENT.equals(attribute))
				return new UpdatableCollectionViewer(
						(AbstractListViewer) object);
			if (object instanceof TableViewer
					&& ViewersProperties.CONTENT.equals(attribute)) {
				return new TableViewerUpdatableCollection(
						(TableViewer) object);
			}
			if (object instanceof Table) {
				return new TableUpdatableValue((Table) object,
						(String) attribute);
			}
		}
		if (description instanceof AbstractListViewer) {
			// binding to a Viewer directly implies binding to its
			// content
			return new UpdatableCollectionViewer(
					(AbstractListViewer) description);
		} else if (description instanceof Text) {
			int validatePolicy = new int[]{SWT.Modify,SWT.FocusOut,SWT.None}[validateTime];
			int updatePolicy = new int[]{SWT.Modify,SWT.FocusOut,SWT.None}[updateTime];
			return new TextUpdatableValue((Text) description,
					validatePolicy, updatePolicy);
		} else if (description instanceof Button) {
			int updatePolicy = new int[]{SWT.Modify,SWT.FocusOut,SWT.None}[updateTime];
			return new ButtonUpdatableValue((Button) description,
					updatePolicy);
		} else if (description instanceof Label) {
			return new LabelUpdatableValue((Label) description);
		} else if (description instanceof Combo) {
			return new ComboUpdatableCollection((Combo) description,
					ViewersProperties.CONTENT);
		} else if (description instanceof Spinner) {
			return new SpinnerUpdatableValue((Spinner) description,
					SWTProperties.SELECTION);
		} else if (description instanceof CCombo) {
			return new CComboUpdatableCollection((CCombo) description,
					ViewersProperties.CONTENT);
		} else if (description instanceof List) {
			return new ListUpdatableCollection((List) description,
					ViewersProperties.CONTENT);
		} else if (description instanceof TableViewerDescription) {
			return new TableViewerUpdatableCollectionExtended(
					(TableViewerDescription) description,
					bindingContext);
		}
		return null;
	}

	public void setUpdateTime(int time) {
		updateTime = time;
	}

	public void setValidationTime(int time) {
		validateTime = time;
	}
}