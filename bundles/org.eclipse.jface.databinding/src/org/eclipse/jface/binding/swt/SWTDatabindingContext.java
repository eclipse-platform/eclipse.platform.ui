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
package org.eclipse.jface.binding.swt;

import java.util.Map;

import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IUpdatable;
import org.eclipse.jface.binding.IUpdatableFactory;
import org.eclipse.jface.binding.IUpdatableFactory2;
import org.eclipse.jface.binding.IValidationContext;
import org.eclipse.jface.binding.internal.swt.ButtonUpdatableValue;
import org.eclipse.jface.binding.internal.swt.CComboUpdatableCollection;
import org.eclipse.jface.binding.internal.swt.CComboUpdatableValue;
import org.eclipse.jface.binding.internal.swt.ComboUpdatableCollection;
import org.eclipse.jface.binding.internal.swt.ComboUpdatableValue;
import org.eclipse.jface.binding.internal.swt.ControlUpdatableValue;
import org.eclipse.jface.binding.internal.swt.LabelUpdatableValue;
import org.eclipse.jface.binding.internal.swt.ListUpdatableCollection;
import org.eclipse.jface.binding.internal.swt.SpinnerUpdatableValue;
import org.eclipse.jface.binding.internal.swt.TableUpdatableValue;
import org.eclipse.jface.binding.internal.swt.TextUpdatableValue;
import org.eclipse.jface.binding.internal.viewers.StructuredViewerUpdatableValue;
import org.eclipse.jface.binding.internal.viewers.TableViewerUpdatableCollection;
import org.eclipse.jface.binding.internal.viewers.TableViewerUpdatableCollectionExtended;
import org.eclipse.jface.binding.internal.viewers.UpdatableCollectionViewer;
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
	 * collectProperties) that is passed to IUpdatableFactory2
	 */
	public static final String VALIDATION_TIME = "org.eclipse.jface.binding.swt.SWTDatabindingContext.validationTime"; //$NON-NLS-1$

	/**
	 * Key for the update time property in the properties map (see
	 * collectProperties) that is passed to IUpdatableFactory2
	 */
	public static final String UPDATE_TIME = "org.eclipse.jface.binding.swt.SWTDatabindingContext.updateTime"; //$NON-NLS-1$

	private int validationTime;

	private int updateTime;

	/**
	 * @param control
	 * @param validationTime
	 * @param updateTime
	 */
	public SWTDatabindingContext(Control control, int validationTime,
			int updateTime) {
		super();
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
	public SWTDatabindingContext(Control control) {
		this(control, TIME_EARLY, TIME_LATE);
	}

	protected void registerValueFactories() {
		super.registerValueFactories();
		addUpdatableFactory(Control.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.ENABLED)) {
					return new ControlUpdatableValue((Control) object,
							(String) attribute);
				}
				return null;
			}
		});
		addUpdatableFactory(Spinner.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.SELECTION)
						|| attribute.equals(SWTBindingConstants.MIN)
						|| attribute.equals(SWTBindingConstants.MAX)) {
					return new SpinnerUpdatableValue((Spinner) object,
							(String) attribute);
				}
				return null;
			}
		});
		addUpdatableFactory(Text.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.TEXT)) {
					return new TextUpdatableValue((Text) object, SWT.Modify,
							SWT.Modify);
				}
				return null;
			}
		});
		addUpdatableFactory(Label.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.TEXT)) {
					return new LabelUpdatableValue((Label) object);
				}
				return null;
			}
		});
		addUpdatableFactory(Button.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.SELECTION)) {
					return new ButtonUpdatableValue((Button) object,
							SWT.Selection);
				}
				return null;
			}
		});		
		addUpdatableFactory(Combo.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.TEXT)
						|| attribute.equals(SWTBindingConstants.SELECTION))
					return new ComboUpdatableValue((Combo) object,
							(String) attribute);
				else if (attribute.equals(SWTBindingConstants.ITEMS))
					return new ComboUpdatableCollection((Combo) object,
							(String) attribute);
				return null;
			}
		});
		addUpdatableFactory(CCombo.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.TEXT)
						|| attribute.equals(SWTBindingConstants.SELECTION))
					return new CComboUpdatableValue((CCombo) object,
							(String) attribute);
				else if (attribute.equals(SWTBindingConstants.ITEMS))
					return new CComboUpdatableCollection((CCombo) object,
							(String) attribute);
				return null;
			}
		});
		addUpdatableFactory(StructuredViewer.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.SELECTION)) {
					return new StructuredViewerUpdatableValue(
							(StructuredViewer) object, (String) attribute);
				}
				return null;
			}
		});

		addUpdatableFactory(AbstractListViewer.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.SELECTION))
					return new StructuredViewerUpdatableValue(
							(AbstractListViewer) object, (String) attribute);
				else if (attribute.equals(SWTBindingConstants.CONTENT))
					return new UpdatableCollectionViewer(
							(AbstractListViewer) object);
				return null;
			}
		});

		addUpdatableFactory(TableViewer.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.CONTENT)) {
					return new TableViewerUpdatableCollection(
							(TableViewer) object);
				}
				return null;
			}
		});

		addUpdatableFactory(Table.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				return new TableUpdatableValue((Table) object,
						(String) attribute);
			}
		});

		// new stuff
		addUpdatableFactory2(new IUpdatableFactory2() {
			public IUpdatable createUpdatable(Map properties, Object description, IValidationContext validationContext) {
				if (description instanceof AbstractListViewer) {
					// binding to a Viewer directly implies binding to its content
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
					return new ButtonUpdatableValue((Button) description, updatePolicy);
				} else if (description instanceof Combo) {
					return new ComboUpdatableCollection((Combo)description, SWTBindingConstants.CONTENT);
				} else if (description instanceof CCombo) {
					return new CComboUpdatableCollection((CCombo)description, SWTBindingConstants.CONTENT);										
				} else if (description instanceof List) {
					return new ListUpdatableCollection((List)description, SWTBindingConstants.CONTENT);					
				} else if (description instanceof TableViewerDescription) {
					return new TableViewerUpdatableCollectionExtended(
							(TableViewerDescription) description, validationContext);
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
