/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.internal.swt;

import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.SWTProperties;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.databinding.ViewersProperties;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

/**
 * @since 3.2
 * 
 */
public class CComboUpdatableValue extends UpdatableValue {

	/**
	 * 
	 */

	private final CCombo ccombo;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param ccombo
	 * @param attribute
	 */
	public CComboUpdatableValue(CCombo ccombo, String attribute) {
		this.ccombo = ccombo;
		this.attribute = attribute;
		if (attribute.equals(ViewersProperties.CONTENT))
			attribute = SWTProperties.TEXT;

		if (attribute.equals(SWTProperties.SELECTION) || 
			attribute.equals(SWTProperties.TEXT)) {
			ccombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}
				}
			});
		}
		else
			throw new IllegalArgumentException();
	}

	public void setValue(Object value) {
		String oldValue = ccombo.getText();
		try {
			updating = true;
			if (attribute.equals(SWTProperties.TEXT)) {
				String stringValue = value!=null?value.toString() : ""; //$NON-NLS-1$
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
					ccombo.select(index); // -1 will not "unselect"
				}
			}
		} finally {
			updating = false;
		}
		fireChangeEvent(IChangeEvent.CHANGE, oldValue, ccombo
				.getText());
	}

	public Object getValue() {
		if (attribute.equals(SWTProperties.TEXT)) {
			return ccombo.getText();
		}
		Assert.isTrue(attribute.equals(SWTProperties.SELECTION), "unexpected attribute: " + attribute); //$NON-NLS-1$
		// The problem with a ccombo, is that it changes the text an fires before 
		// it update its selection index
		return ccombo.getText();
//			int index = ccombo.getSelectionIndex();
//			if (index >= 0)
//				return ccombo.getItem(index);
//			return null;
	}

	public Class getValueType() {
		Assert.isTrue(attribute.equals(SWTProperties.TEXT)
				|| attribute.equals(SWTProperties.SELECTION),
				"unexpected attribute: " + attribute); //$NON-NLS-1$
		return String.class;
	}

}
