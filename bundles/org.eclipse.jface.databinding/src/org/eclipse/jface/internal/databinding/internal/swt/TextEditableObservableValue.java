/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.internal.swt;

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
		if (Boolean.TRUE.equals(value)) {
			text.setEditable(true);
		} else if (Boolean.FALSE.equals(value)) {
			text.setEditable(false);
		}
	}
}
