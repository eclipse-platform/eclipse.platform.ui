/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * The interface that represents a binding between a model and a target.
 * 
 * This interface is not intended to be implemented by clients.
 * 
 * @since 1.0
 */
public abstract class Binding {

	private List bindingEventListeners = new ArrayList();

	protected final DataBindingContext context;

	/**
	 * @param context
	 */
	public Binding(DataBindingContext context) {
		this.context = context;
	}

	/**
	 * Add a listener to the set of listeners that will be notified when an
	 * event occurs in the data flow pipeline that is managed by this Binding.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addBindingEventListener(IBindingListener listener) {
		bindingEventListeners.add(listener);
	}

	/**
	 * Fires the given event to the binding event listeners, exiting early when
	 * one of the listeners flags a validation error. If no listener flags a
	 * validation error, the data binding context's binding listeners will be
	 * notified in the same manner.
	 * 
	 * @param event
	 * @return the validation error, or null
	 */
	protected ValidationError fireBindingEvent(BindingEvent event) {
		ValidationError result = null;
		IBindingListener[] listeners = (IBindingListener[]) bindingEventListeners
				.toArray(new IBindingListener[bindingEventListeners.size()]);
		for (int i = 0; i < listeners.length; i++) {
			IBindingListener listener = listeners[i];
			result = listener.bindingEvent(event);
			if (result != null)
				break;
		}
		if (result == null)
			result = context.fireBindingEvent(event);
		return result;
	}

	/**
	 * @return an observable value containing the current partial validation
	 *         error or null
	 */
	public abstract IObservableValue getPartialValidationError();

	/**
	 * @return an observable value containing the current validation error or
	 *         null
	 */
	public abstract IObservableValue getValidationError();

	/**
	 * Removes a listener from the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by this
	 * Binding.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		bindingEventListeners.remove(listener);
	}

	/**
	 * 
	 */
	public abstract void updateModelFromTarget();

	/**
	 * 
	 */
	public abstract void updateTargetFromModel();

}
