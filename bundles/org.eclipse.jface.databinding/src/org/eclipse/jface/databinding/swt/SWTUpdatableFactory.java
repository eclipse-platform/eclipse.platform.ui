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
package org.eclipse.jface.databinding.swt;

import java.util.Map;

import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.viewers.ViewersProperties;
import org.eclipse.jface.internal.databinding.swt.ButtonUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.CComboUpdatableCollection;
import org.eclipse.jface.internal.databinding.swt.CComboUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.ComboUpdatableCollection;
import org.eclipse.jface.internal.databinding.swt.ComboUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.ControlUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.LabelUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.ListUpdatableCollection;
import org.eclipse.jface.internal.databinding.swt.ListUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.SpinnerUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.TableUpdatableValue;
import org.eclipse.jface.internal.databinding.swt.TextUpdatableValue;
import org.eclipse.jface.internal.databinding.viewers.StructuredViewerUpdatableValue;
import org.eclipse.jface.internal.databinding.viewers.TableViewerUpdatableCollection;
import org.eclipse.jface.internal.databinding.viewers.UpdatableCollectionViewer;
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

	private int updateTime = IDataBindingContext.TIME_LATE;
	
	/**
	 * Create a factory that can create updatables for SWT controls
	 */
	public SWTUpdatableFactory(){
	}
	
	/**
	 * @param updateTime	The update policy of IDataBineingContext.TIME_LATE or IDataBindingContext.TIME_EARLY is a hint
	 * 						that some editable controls may implement (such as Text) to determine when to fire updates
	 */
	public SWTUpdatableFactory(int updateTime){
		this.updateTime = updateTime;
	}
	
	public IUpdatable createUpdatable(Map properties,
			Object description, IDataBindingContext bindingContext) {
		if (description instanceof Property) {
			Object object = ((Property) description)
					.getObject();
			Object attribute = ((Property) description)
					.getPropertyID();
			if (object instanceof Control
					&& SWTProperties.ENABLED.equals(attribute)) {
				return new ControlUpdatableValue((Control) object,
						(String) attribute);
			}
			if (object instanceof Control
					&& SWTProperties.VISIBLE.equals(attribute)) {
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
				return new TextUpdatableValue((Text) object,SWT.Modify);
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
			int updatePolicy = new int[]{SWT.Modify,SWT.FocusOut,SWT.None}[updateTime];
			return new TextUpdatableValue((Text) description,updatePolicy);
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
		}
		return null;
	}

	/**
	 * @param time.  Values are TIME_EARLY or TIME_LATE and specify when update occurs
	 * 				 For example TIME_EARLY on Text control and update occurs per keystroke,
	 * 				 TIME_LATE and validation occurs when the field loses focus
	 */
	public void setUpdateTime(int time) {
		updateTime = time;
	}
}