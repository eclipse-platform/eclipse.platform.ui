/*******************************************************************************
 * Copyright (c) 2009 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.databinding;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;

public class AggregateNameObservableValue extends AbstractObservableValue {

	private final IObservableValue[] observableValues;
	private final IValueChangeListener listener;
	private volatile boolean isUpdating;
	private String currentStringValue;

	public AggregateNameObservableValue(WritableValue value) {
		String[] properties = new String[] { "firstName", "middleName",
				"lastName" };
		observableValues = new IObservableValue[properties.length];
		listener = new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				if (!isUpdating) {
					fireValueChange(Diffs.createValueDiff(currentStringValue,
							doGetValue()));
				}
			}
		};
		int i = 0;
		for (String property : properties) {
			observableValues[i] = PojoObservables.observeDetailValue(value,
					property, String.class);
			observableValues[i++].addValueChangeListener(listener);
		}
	}

	@Override
	protected Object doGetValue() {
		StringBuilder builder = new StringBuilder();
		for (IObservableValue value : observableValues) {
			String stringValue = ((String) value.getValue());
			if (stringValue != null && stringValue.trim().length() > 0) {
				builder.append(stringValue.trim());
				builder.append(" ");
			}
		}
		currentStringValue = builder.toString().trim();
		return currentStringValue;
	}

	@Override
	public void doSetValue(Object value) {
		Object oldValue = doGetValue();
		String[] nameValues = ((String) value).split(" ");

		try {
			isUpdating = true;
			if (nameValues.length == 3) {
				for (int i = 0; i < 3; i++) {
					observableValues[i].setValue(nameValues[i]);
				}
			} else if (nameValues.length == 2) {
				observableValues[0].setValue(nameValues[0]);
				observableValues[1].setValue("");
				observableValues[2].setValue(nameValues[1]);
			} else if (nameValues.length == 1) {
				observableValues[0].setValue(nameValues[0]);
				observableValues[1].setValue("");
				observableValues[2].setValue("");
			} else {
				for (int i = 0; i < 3; i++) {
					observableValues[i].setValue("");
				}
			}
		} finally {
			isUpdating = false;
		}
		doGetValue();
		fireValueChange(Diffs.createValueDiff(oldValue, value));
	}

	public Object getValueType() {
		return String.class;
	}

	@Override
	public synchronized void dispose() {
		for (int i = 0; i < observableValues.length; i++) {
			observableValues[i].removeValueChangeListener(listener);
		}
		super.dispose();
	}
}
