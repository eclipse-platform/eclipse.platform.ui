/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.2
 * 
 */
public class ComboObservableValue extends AbstractSWTObservableValue {

	private final Combo combo;
	private final String attribute;
	private boolean updating = false;
	private String currentValue;

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
					if (!updating) {
						String oldValue = currentValue;
						currentValue = ComboObservableValue.this.combo
								.getText();
						fireValueChange(Diffs.createValueDiff(oldValue,
								currentValue));
					}
				}
			});
		} else
			throw new IllegalArgumentException();
	}

	public void doSetValue(final Object value) {
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
	 * @return attribute being observed
	 */
	public String getAttribute() {
		return attribute;
	}
}
