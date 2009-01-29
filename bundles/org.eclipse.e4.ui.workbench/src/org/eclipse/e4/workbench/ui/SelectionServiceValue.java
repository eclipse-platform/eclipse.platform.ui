/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SelectionServiceValue implements ISelectionService {
	
	private Object value;
	private List<IValueChangeListener> listeners = new ArrayList<IValueChangeListener>();

	public Object getSelection(Class api) {
		Object value = getValue();
		if (api.isInstance(value)) {
			return value;
		}

		if (value instanceof IStructuredSelection) {
			value = ((IStructuredSelection) value).getFirstElement();
		}
		if (api.isInstance(value)) {
			return value;
		} else if (value != null) {
			return Platform.getAdapterManager().loadAdapter(value,
					api.getName());
		}
		return null;
	}

	public void addValueChangeListener(IValueChangeListener listener) {
		listeners.add(listener);
	}

	public Object getValue() {
		return value;
	}

	public Object getValueType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeValueChangeListener(IValueChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void setValue(final Object newValue) {
		if (value == newValue)
			return;
		final Object oldValue = value;
		this.value = newValue;
		
		ValueDiff diff = new ValueDiff() {
			public Object getNewValue() {
				return newValue;
			}
			public Object getOldValue() {
				return oldValue;
			}};
			
		ValueChangeEvent event = new ValueChangeEvent(this, diff);
		
		for(IValueChangeListener listener : listeners) {
			listener.handleValueChange(event);
		}
	}

	public void addChangeListener(IChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void addDisposeListener(IDisposeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void addStaleListener(IStaleListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public Realm getRealm() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStale() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeChangeListener(IChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void removeDisposeListener(IDisposeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void removeStaleListener(IStaleListener listener) {
		// TODO Auto-generated method stub
		
	}
}
