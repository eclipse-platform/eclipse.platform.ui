/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.swt;

import java.util.Map;

import org.eclipse.jface.databinding.DatabindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IValidationContext;
import org.eclipse.jface.databinding.PropertyDescription;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class SWTDatabindingContext extends DatabindingContext {

	/**
	 * 
	 */
	public static String JFACE_VIEWER_CONTENT = "content"; //$NON-NLS-1$

	/**
	 * 
	 */
	public static String JFACE_VIEWER_SELECTION = "selection"; //$NON-NLS-1$

	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should be triggered early, typically on each keystroke.
	 */
	public static final int TIME_EARLY = 1;

	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should be triggered late, typically on focus lost.
	 */
	public static final int TIME_LATE = 2;

	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should never be triggered. Note that this means that the updatable will
	 * not track the underlying widget's changes.
	 */
	public static final int TIME_NEVER = 3;

	/**
	 * Key for the validation time property in the properties map (see
	 * collectProperties) that is passed to IUpdatableFactory
	 */
	public static final String VALIDATION_TIME = "org.eclipse.jface.databinding.swt.SWTDatabindingContext.validationTime"; //$NON-NLS-1$

	/**
	 * Key for the update time property in the properties map (see
	 * collectProperties) that is passed to IUpdatableFactory
	 */
	public static final String UPDATE_TIME = "org.eclipse.jface.databinding.swt.SWTDatabindingContext.updateTime"; //$NON-NLS-1$

	private int validationTime;

	private int updateTime;

	/**
	 * @param parent
	 * @param control
	 * @param validationTime
	 * @param updateTime
	 */
	public SWTDatabindingContext(DatabindingContext parent, Control control,
			int validationTime, int updateTime) {
		super(parent);
		this.validationTime = validationTime;
		this.updateTime = updateTime;
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}

	/**
	 * Creates a data binding context with validationTime==TIME_EARLY and
	 * updateTime=TIME_LATE.
	 * 
	 * @param control
	 */
	public SWTDatabindingContext(DatabindingContext parent, Control control) {
		this(parent, control, TIME_EARLY, TIME_LATE);
	}

	protected void registerFactories() {
		super.registerFactories();

		addUpdatableFactory(new IUpdatableFactory() {
			public IUpdatable createUpdatable(Map properties,
					Object description, IValidationContext validationContext) {
				if (description instanceof PropertyDescription) {
					Object object = ((PropertyDescription) description)
							.getObject();
					Object attribute = ((PropertyDescription) description)
							.getPropertyID();
					if (object instanceof Control
							&& SWTBindingConstants.ENABLED.equals(attribute)) {
						return new ControlUpdatableValue((Control) object,
								(String) attribute);
					}
					if (object instanceof Spinner
							&& SWTBindingConstants.SELECTION.equals(attribute)
							|| SWTBindingConstants.MIN.equals(attribute)
							|| SWTBindingConstants.MAX.equals(attribute)) {
						return new SpinnerUpdatableValue((Spinner) object,
								(String) attribute);
					}
					if (object instanceof Text
							&& SWTBindingConstants.TEXT.equals(attribute)) {
						return new TextUpdatableValue((Text) object,
								SWT.Modify, SWT.Modify);
					}
					if (object instanceof Label
							&& SWTBindingConstants.TEXT.equals(attribute)) {
						return new LabelUpdatableValue((Label) object);
					}
					if (object instanceof Button
							&& SWTBindingConstants.SELECTION.equals(attribute)) {
						return new ButtonUpdatableValue((Button) object,
								SWT.Selection);
					}
					if (object instanceof Combo
							&& (SWTBindingConstants.TEXT.equals(attribute) || SWTBindingConstants.SELECTION
									.equals(attribute))) {
						return new ComboUpdatableValue((Combo) object,
								(String) attribute);
					} else if (object instanceof Combo
							&& SWTBindingConstants.ITEMS.equals(attribute)) {
						return new ComboUpdatableCollection((Combo) object,
								(String) attribute);
					}
					if (object instanceof CCombo
							&& (SWTBindingConstants.TEXT.equals(attribute) || SWTBindingConstants.SELECTION
									.equals(attribute))) {
						return new CComboUpdatableValue((CCombo) object,
								(String) attribute);
					} else if (object instanceof CCombo
							&& SWTBindingConstants.ITEMS.equals(attribute)) {
						return new CComboUpdatableCollection((CCombo) object,
								(String) attribute);
					}
					if (object instanceof List
							&& SWTBindingConstants.SELECTION.equals(attribute)) {
						// SWT.SINGLE selection only
						return new ListUpdatableValue((List) object,
								(String) attribute);
					} else if (object instanceof List
							&& SWTBindingConstants.ITEMS.equals(attribute)) {
						return new ListUpdatableCollection((List) object,
								(String) attribute);
					}
					if (object instanceof StructuredViewer
							&& SWTBindingConstants.SELECTION.equals(attribute)) {
						return new StructuredViewerUpdatableValue(
								(StructuredViewer) object, (String) attribute);
					}
					if (object instanceof AbstractListViewer
							&& SWTBindingConstants.SELECTION.equals(attribute))
						return new StructuredViewerUpdatableValue(
								(AbstractListViewer) object, (String) attribute);
					else if (object instanceof AbstractListViewer
							&& SWTBindingConstants.CONTENT.equals(attribute))
						return new UpdatableCollectionViewer(
								(AbstractListViewer) object);
					if (object instanceof TableViewer
							&& SWTBindingConstants.CONTENT.equals(attribute)) {
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
					int validatePolicy = getPolicy(properties,
							SWTDatabindingContext.VALIDATION_TIME, SWT.Modify);
					int updatePolicy = getPolicy(properties,
							SWTDatabindingContext.UPDATE_TIME, SWT.FocusOut);
					return new TextUpdatableValue((Text) description,
							validatePolicy, updatePolicy);
				} else if (description instanceof Button) {
					int updatePolicy = getPolicy(properties,
							SWTDatabindingContext.UPDATE_TIME, SWT.FocusOut);
					return new ButtonUpdatableValue((Button) description,
							updatePolicy);
				} else if (description instanceof Combo) {
					return new ComboUpdatableCollection((Combo) description,
							SWTBindingConstants.CONTENT);
				} else if (description instanceof CCombo) {
					return new CComboUpdatableCollection((CCombo) description,
							SWTBindingConstants.CONTENT);
				} else if (description instanceof List) {
					return new ListUpdatableCollection((List) description,
							SWTBindingConstants.CONTENT);
				} else if (description instanceof TableViewerDescription) {
					return new TableViewerUpdatableCollectionExtended(
							(TableViewerDescription) description,
							validationContext);
				}
				return null;
			}

			private int getPolicy(Map properties, String propertyName,
					int defaultPolicy) {
				int policy = defaultPolicy;
				Integer time = (Integer) properties.get(propertyName);
				if (time != null) {
					switch (time.intValue()) {
					case SWTDatabindingContext.TIME_EARLY:
						policy = SWT.Modify;
						break;
					case SWTDatabindingContext.TIME_LATE:
						policy = SWT.FocusOut;
						break;
					case SWTDatabindingContext.TIME_NEVER:
						policy = SWT.None;
						break;
					}
				}
				return policy;
			}
		});
	}

	/**
	 * @return the default updateTime
	 */
	public int getUpdateTime() {
		return updateTime;
	}

	/**
	 * @param updateTime
	 */
	public void setUpdateTime(int updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * @return the default validation time
	 */
	public int getValidationTime() {
		return validationTime;
	}

	/**
	 * @param validationTime
	 */
	public void setValidationTime(int validationTime) {
		this.validationTime = validationTime;
	}

	protected void collectProperties(Map properties) {
		super.collectProperties(properties);
		properties.put(VALIDATION_TIME, new Integer(validationTime));
		properties.put(UPDATE_TIME, new Integer(updateTime));
	}
}
