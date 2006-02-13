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
package org.eclipse.jface.examples.databinding.model;

import java.util.StringTokenizer;

import org.eclipse.jface.internal.databinding.api.observable.value.AbstractObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueDiff;
import org.eclipse.jface.internal.databinding.api.observable.value.ValueDiff;

public class AggregateObservableValue extends AbstractObservableValue {

	private IObservableValue[] observableValues;

	private String delimiter;

	private boolean updating = false;
	
	private String currentValue;

	private IValueChangeListener listener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, IValueDiff diff) {
			if (!updating) {
				fireValueChange(new ValueDiff(currentValue, doGetValue()));
			}
		}
	};

	public AggregateObservableValue(IObservableValue[] observableValues,
			String delimiter) {
		this.observableValues = observableValues;
		this.delimiter = delimiter;
		for (int i = 0; i < observableValues.length; i++) {
			observableValues[i].addValueChangeListener(listener);
		}
		doGetValue();
	}

	public void setValue(Object value) {
		Object oldValue = doGetValue();
		StringTokenizer tokenizer = new StringTokenizer((String) value,
				delimiter);
		try {
			updating = true;
			for (int i = 0; i < observableValues.length; i++) {
				if (tokenizer.hasMoreElements()) {
					observableValues[i].setValue(tokenizer.nextElement());
				} else {
					observableValues[i].setValue(null);
				}
			}
		} finally {
			updating = false;
		}
		doGetValue();
		fireValueChange(new ValueDiff(oldValue, value));
	}

	public Object doGetValue() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < observableValues.length; i++) {
			if (i > 0 & i < observableValues.length) {
				result.append(delimiter);
			}
			result.append(observableValues[i].getValue());
		}
		currentValue = result.toString();
		return currentValue;
	}

	public Object getValueType() {
		return String.class;
	}

	public void dispose() {
		for (int i = 0; i < observableValues.length; i++) {
			observableValues[i].removeValueChangeListener(listener);
		}
		super.dispose();
	}

}
