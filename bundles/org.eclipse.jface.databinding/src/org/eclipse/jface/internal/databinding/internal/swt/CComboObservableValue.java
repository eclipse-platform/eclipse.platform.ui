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
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.value.AbstractObservableValue;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTProperties;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

/**
 * @since 3.2
 * 
 */
public class CComboObservableValue extends AbstractObservableValue {

	/**
	 * 
	 */

	private final CCombo ccombo;

	private final String attribute;

	private boolean updating = false;

	private String currentValue;

	/**
	 * @param ccombo
	 * @param attribute
	 */
	public CComboObservableValue(CCombo ccombo, String attribute) {
		this.ccombo = ccombo;
		this.attribute = attribute;

		if (attribute.equals(SWTProperties.SELECTION)
				|| attribute.equals(SWTProperties.TEXT)) {
			this.currentValue = ccombo.getText();
			ccombo.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					if (!updating) {
						String oldValue = currentValue;
						currentValue = CComboObservableValue.this.ccombo
								.getText();
						fireValueChange(Diffs.createValueDiff(oldValue,
								currentValue));
					}
				}
			});
		} else
			throw new IllegalArgumentException();
	}

	public void setValue(final Object value) {
		String oldValue = ccombo.getText();
		try {
			updating = true;
			if (attribute.equals(SWTProperties.TEXT)) {
				String stringValue = value != null ? value.toString() : ""; //$NON-NLS-1$
				ccombo.setText(stringValue);
			} else if (attribute.equals(SWTProperties.SELECTION)) {
				String items[] = ccombo.getItems();
				int index = -1;
				if (items != null && value != null) {
					for (int i = 0; i < items.length; i++) {
						if (value.equals(items[i])) {
							index = i;
							break;
						}
					}
					if (index == -1) {
						ccombo.setText((String) value);
					} else {
						ccombo.select(index); // -1 will not "unselect"
					}
				}
			}
		} finally {
			updating = false;
		}
		fireValueChange(Diffs.createValueDiff(oldValue, ccombo.getText()));
	}

	public Object doGetValue() {
		if (attribute.equals(SWTProperties.TEXT))
			return ccombo.getText();

		Assert.isTrue(attribute.equals(SWTProperties.SELECTION),
				"unexpected attribute: " + attribute); //$NON-NLS-1$
		// The problem with a ccombo, is that it changes the text and
		// fires before it update its selection index
		return ccombo.getText();
	}

	public Object getValueType() {
		Assert.isTrue(attribute.equals(SWTProperties.TEXT)
				|| attribute.equals(SWTProperties.SELECTION),
				"unexpected attribute: " + attribute); //$NON-NLS-1$
		return String.class;
	}

}
