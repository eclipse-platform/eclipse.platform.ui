/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

import java.util.StringTokenizer;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;

/**
 * @since 3.2
 */
public class AggregateObservableValue extends AbstractObservableValue<Object> {

	private final IObservableValue<Object>[] observableValues;

	private final String delimiter;

	private boolean updating = false;

	private String currentValue;

	private final IValueChangeListener<Object> listener = event -> {
		if (!updating) {
			fireValueChange(Diffs.createValueDiff(currentValue, doGetValue()));
		}
	};

	public AggregateObservableValue(IObservableValue<Object>[] observableValues, String delimiter) {
		this.observableValues = observableValues;
		this.delimiter = delimiter;
		for (IObservableValue<?> observableValue : observableValues) {
			observableValue.addValueChangeListener(listener);
		}
		doGetValue();
	}

	@Override
	public void doSetValue(Object value) {
		Object oldValue = doGetValue();
		StringTokenizer tokenizer = new StringTokenizer((String) value, delimiter);
		try {
			updating = true;
			for (IObservableValue<Object> observableValue : observableValues) {
				if (tokenizer.hasMoreElements()) {
					observableValue.setValue(tokenizer.nextElement());
				} else {
					observableValue.setValue(null);
				}
			}
		} finally {
			updating = false;
		}
		doGetValue();
		fireValueChange(Diffs.createValueDiff(oldValue, value));
	}

	@Override
	public Object doGetValue() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < observableValues.length; i++) {
			if (i > 0 & i < observableValues.length) {
				result.append(delimiter);
			}
			result.append(observableValues[i].getValue());
		}
		currentValue = result.toString();
		return currentValue;
	}

	@Override
	public Object getValueType() {
		return String.class;
	}

	@Override
	public synchronized void dispose() {
		for (IObservableValue<?> observableValue : observableValues) {
			observableValue.removeValueChangeListener(listener);
		}
		super.dispose();
	}

}
