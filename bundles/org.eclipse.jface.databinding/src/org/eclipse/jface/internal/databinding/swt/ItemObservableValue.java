/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.widgets.Item;

/**
 * @since 3.5
 * 
 */
public class ItemObservableValue extends AbstractSWTObservableValue {

	private final Item item;

	/**
	 * @param item
	 */
	public ItemObservableValue(Item item) {
		super(item);
		this.item = item;
	}
	
	/**
	 * @param realm
	 * @param item
	 */
	public ItemObservableValue(Realm realm, Item item) {
		super(realm, item);
		this.item = item;
	}

	public void doSetValue(final Object value) {
		String oldValue = item.getText();
		String newValue = value == null ? "" : value.toString(); //$NON-NLS-1$
		item.setText(newValue);
		
		if (!newValue.equals(oldValue)) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}

	public Object doGetValue() {
		return item.getText();
	}

	public Object getValueType() {
		return String.class;
	}

}