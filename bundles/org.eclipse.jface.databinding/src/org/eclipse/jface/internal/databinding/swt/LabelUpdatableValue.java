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
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.swt.widgets.Label;

/**
 * @since 3.2
 *
 */
public class LabelUpdatableValue extends UpdatableValue {

	private final Label label;

	/**
	 * @param label
	 */
	public LabelUpdatableValue(Label label) {
		this.label = label;
	}

	public void setValue(final Object value) {
		AsyncRunnable runnable = new AsyncRunnable(){
			public void run(){
				String oldValue = label.getText();
				label.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
				fireChangeEvent(ChangeEvent.CHANGE, oldValue, label.getText());		
			}
		};	
		runnable.runOn(label.getDisplay());
	}
 
	public Object computeValue() {
		
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				return label.getText();
			}
		};
		return runnable.runOn(label.getDisplay());
	}

	public Class getValueType() {
		return String.class;
	}
	
}
