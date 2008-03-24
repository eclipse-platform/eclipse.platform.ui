/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Text;

/**
 * Observable value for the editable property of a Text.
 * 
 * @since 1.1
 */
public class TextEditableObservableValue extends AbstractSWTObservableValue {
	private Text text;
	
	/**
	 * @param text
	 */
	public TextEditableObservableValue(Text text) {
		super(text);	
		this.text = text;
	}
	
	/**
	 * @param realm
	 * @param text
	 */
	public TextEditableObservableValue(Realm realm, Text text) {
		super(realm, text);
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doGetValue()
	 */
	protected Object doGetValue() {
		return (text.getEditable()) ? Boolean.TRUE : Boolean.FALSE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
	 */
	public Object getValueType() {
		return Boolean.TYPE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doSetValue(java.lang.Object)
	 */
	protected void doSetValue(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Parameter value was null."); //$NON-NLS-1$
		}
		
		Boolean oldValue = new Boolean(text.getEditable());
		Boolean newValue = (Boolean) value;
		
		text.setEditable(newValue.booleanValue());
		
		if (!oldValue.equals(newValue)) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}
}
