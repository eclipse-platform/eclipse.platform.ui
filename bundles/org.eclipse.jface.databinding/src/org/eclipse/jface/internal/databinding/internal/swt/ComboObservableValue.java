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
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.BindingEvent;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.IBindingListener;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTProperties;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 * 
 */
public class ComboObservableValue extends AbstractSWTObservableValue {

	private final Combo combo;
	private final String attribute;
	private boolean updating = false;
	private String currentValue;

	private ComboObservableList items;
	private WritableList itemsList;
	protected boolean ignoreNextEvent = false;

	/**
	 * @param combo
	 * @param attribute
	 */
	public ComboObservableValue(Combo combo, String attribute) {
		super(combo);
		this.combo = combo;
		this.attribute = attribute;
		
		if (attribute.equals(SWTProperties.SELECTION)
				|| attribute.equals(SWTProperties.TEXT)) {
			this.currentValue = combo.getText();
			combo.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					if (!updating && !ignoreNextEvent) {
						String oldValue = currentValue;
						currentValue = ComboObservableValue.this.combo
								.getText();
						fireValueChange(Diffs.createValueDiff(oldValue,
								currentValue));
					} else {
						ignoreNextEvent = false;
					}
				}
			});
		} else
			throw new IllegalArgumentException();
	}

	public void setValue(final Object value) {
		String oldValue = combo.getText();
		try {
			updating = true;
			if (attribute.equals(SWTProperties.TEXT)) {
				String stringValue = value != null ? value.toString() : ""; //$NON-NLS-1$
				combo.setText(stringValue);
			} else if (attribute.equals(SWTProperties.SELECTION)) {
				String items[] = combo.getItems();
				int index = -1;
				if (items != null && value != null) {
					for (int i = 0; i < items.length; i++) {
						if (value.equals(items[i])) {
							index = i;
							break;
						}
					}
					if (index == -1) {
						combo.setText((String) value);
					} else {
						combo.select(index); // -1 will not "unselect"
					}
				}
			}
		} finally {
			updating = false;
		}
		fireValueChange(Diffs.createValueDiff(oldValue, combo.getText()));
	}

	public Object doGetValue() {
		if (attribute.equals(SWTProperties.TEXT))
			return combo.getText();

		Assert.isTrue(attribute.equals(SWTProperties.SELECTION),
				"unexpected attribute: " + attribute); //$NON-NLS-1$
		// The problem with a ccombo, is that it changes the text and
		// fires before it update its selection index
		return combo.getText();
	}

	public Object getValueType() {
		Assert.isTrue(attribute.equals(SWTProperties.TEXT)
				|| attribute.equals(SWTProperties.SELECTION),
				"unexpected attribute: " + attribute); //$NON-NLS-1$
		return String.class;
	}

	/**
	 * Returns an IObservableList that is bound to the Combo's items. The
	 * difference between binding to this WritableList and binding to the items
	 * directly is that when you update the items through this WritableList, the
	 * ComboObservableValue will attempt to maintain the value of the Text
	 * selection if at all possible. ie: If the set of Items after the refresh
	 * still contain the same Text value as the set of Items had before the
	 * refresh, the Text value will remain the same.
	 * <p>
	 * The only constraint with using this method is that if the Combo is *not*
	 * SWT.READ_ONLY, you have to bind the ComboObservableValue itself *before*
	 * you bind to the IObservableList returned by this method. If the Combo is
	 * SWT.READ_ONLY, you have to bind the ComboObservableValue itself *after*
	 * you bind to the IObservableList returned by this method.
	 * 
	 * @return an IObservableList bound to the items property
	 * 
	 * TODO this hack was put in as a temporary workaround for bug 147128
	 */
	public IObservableList getItems() {
		if (items == null) {
			items = new ComboObservableList(combo);
			DataBindingContext dbc = new DataBindingContext();
			itemsList = new WritableList(String.class);
			dbc.addBindSupportFactory(new DefaultBindSupportFactory());
			dbc.addBindingFactory(new DefaultBindingFactory());
			Binding binding = dbc.bind(items, itemsList, null);
			binding.addBindingEventListener(new IBindingListener() {
				public ValidationError bindingEvent(BindingEvent e) {
					if ((e.copyType == BindingEvent.EVENT_COPY_TO_MODEL || 
							e.copyType == BindingEvent.EVENT_COPY_TO_TARGET) &&
							e.pipelinePosition == BindingEvent.PIPELINE_AFTER_GET &&
							!ignoreNextEvent)
					{
						ignoreNextEvent = true;
						Display.getCurrent().asyncExec(new Runnable() {
							public void run() {
								ignoreNextEvent = false;
							}
						});
					}
					return null;
				}
			});
		}
		return itemsList;
	}

}
