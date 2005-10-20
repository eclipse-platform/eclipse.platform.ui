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

import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IUpdatable;
import org.eclipse.jface.binding.IUpdatableFactory;
import org.eclipse.jface.binding.internal.swt.ButtonUpdatableValue;
import org.eclipse.jface.binding.internal.swt.ComboUpdatableValue;
import org.eclipse.jface.binding.internal.swt.ControlUpdatableValue;
import org.eclipse.jface.binding.internal.swt.LabelUpdatableValue;
import org.eclipse.jface.binding.internal.swt.SpinnerUpdatableValue;
import org.eclipse.jface.binding.internal.swt.TableUpdatableValue;
import org.eclipse.jface.binding.internal.swt.TextUpdatableValue;
import org.eclipse.jface.binding.internal.viewers.StructuredViewerUpdatableValue;
import org.eclipse.jface.binding.internal.viewers.TableViewerUpdatableCollection;
import org.eclipse.jface.binding.internal.viewers.UpdatableCollectionViewer;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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

	private final int validatePolicy;

	private final int updatePolicy;

	/**
	 * 
	 * @param control
	 * @param validatePolicy
	 *            one of SWT.Modify, SWT.FocusOut, or SWT.None
	 * @param updatePolicy
	 *            one of SWT.Modify, SWT.FocusOut, or SWT.None
	 */
	public SWTDatabindingContext(Control control, int validatePolicy,
			int updatePolicy) {
		super();
		this.validatePolicy = validatePolicy;
		this.updatePolicy = updatePolicy;
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
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
					return new ButtonUpdatableValue((Button) object, SWT.Selection);
				}
				return null;
			}
		});
		addUpdatableFactory(Combo.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				if (attribute.equals(SWTBindingConstants.TEXT) ||
					attribute.equals(SWTBindingConstants.SELECTION))
				    return new ComboUpdatableValue((Combo) object, (String) attribute);
			//	else if (attribute.equals())
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
					return new StructuredViewerUpdatableValue((AbstractListViewer) object, (String) attribute);
				else if (attribute.equals(SWTBindingConstants.CONTENT))
				    return new UpdatableCollectionViewer((AbstractListViewer) object);
				return null;
			}
		});

		addUpdatableFactory(TableViewer.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				return new TableViewerUpdatableCollection((TableViewer) object);
			}
		});

		addUpdatableFactory(Table.class, new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				return new TableUpdatableValue((Table) object,
						(String) attribute);
			}
		});
	}

	/**
	 * @return the update policy
	 */
	public int getUpdatePolicy() {
		return updatePolicy;
	}

	/**
	 * @return the validation policy
	 */
	public int getValidatePolicy() {
		return validatePolicy;
	}
}
