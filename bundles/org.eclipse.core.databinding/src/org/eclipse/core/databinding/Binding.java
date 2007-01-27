/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159768
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This abstract class represents a binding between a model and a target.
 * 
 * @since 1.0
 */
public abstract class Binding {

	private List bindingEventListeners = new ArrayList();

	protected DataBindingContext context;

	/**
	 * Creates a new binding, using the given context.
	 * @param context
	 */
	public Binding(DataBindingContext context) {
		this.context = context;
	}
	
	/**
	 * This method allows subclasses to fill bind spec defaults. Usually called
	 * from subclass constructors.
	 * 
	 * @param bindSpec
	 * @param target
	 * @param model
	 */
	protected void fillBindSpecDefaults(BindSpec bindSpec, IObservable target, IObservable model) {
		bindSpec.fillBindSpecDefaults(target, model);
	}

	/**
	 * Add a listener to the set of listeners that will be notified when an
	 * event occurs in the data flow pipeline that is managed by this Binding.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addBindingEventListener(IBindingListener listener) {
		synchronized (bindingEventListeners) {
			bindingEventListeners.add(listener);
		}
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
	protected IStatus fireBindingEvent(BindingEvent event) {
		IStatus result = Status.OK_STATUS;
		IBindingListener[] listeners;
		synchronized (bindingEventListeners) {
			listeners = (IBindingListener[]) bindingEventListeners
					.toArray(new IBindingListener[bindingEventListeners.size()]);
		}
		for (int i = 0; i < listeners.length; i++) {
			IBindingListener listener = listeners[i];
			result = listener.bindingEvent(event);
			if (!result.isOK())
				break;
		}
		if (result.isOK())
			result = context.fireBindingEvent(event);
		return result;
	}

	/**
	 * @return an observable value containing the current partial validation
	 *         status
	 */
	public abstract IObservableValue getPartialValidationStatus();

	/**
	 * @return an observable value containing the current validation status
	 */
	public abstract IObservableValue getValidationStatus();

	/**
	 * Removes a listener from the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by this
	 * Binding.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		synchronized (bindingEventListeners) {
			bindingEventListeners.remove(listener);
		}
	}

	/**
	 * Updates the model's state from the target's state at the next reasonable
	 * opportunity. There is no guarantee that the state will have been updated
	 * by the time this call returns.
	 */
	public abstract void updateModelFromTarget();

	/**
	 * Updates the target's state from the model's state at the next reasonable
	 * opportunity. There is no guarantee that the state will have been updated
	 * by the time this call returns.
	 */
	public abstract void updateTargetFromModel();
	
	/**
	 * Disposes of this Binding. Subclasses may extend, but must call super.dispose().
	 */
	public void dispose() {
		bindingEventListeners = null;
		context = null;
		disposed = true;
	}

	protected boolean disposed = false;
	
	/**
	 * @return true if the binding has been disposed.  false otherwise.
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * @param context
	 */
	/* package */ void setDataBindingContext(DataBindingContext context) {
		this.context = context;
	}

	/**
	 * Invokes the pipeline positions from the target to the model up to and
	 * including the provided <code>pipelinePosition</code> but no further.
	 * 
	 * @param pipelinePosition BindingEvent.PIPELINE_* constant
	 */
	public abstract void updateModelFromTarget(int pipelinePosition);

	/**
	 * Invokes the pipeline posistions from the model to the target up to and
	 * including the provided <code>pipelinePosition</code> but no further.
	 * 
	 * @param pipelinePosition BindingEvent.PIPELINE_* constant
	 */
	public abstract void updateTargetFromModel(int pipelinePosition);

	/**
	 * Returns a new BindingEvent object.
	 * 
	 * @param model
	 * @param target
	 * @param diff
	 * @param copyType
	 * @param pipelinePosition
	 *            The initial processing pipeline position.
	 * @return the new binding event
	 */
	protected BindingEvent createBindingEvent(IObservable model,
			IObservable target, IDiff diff, int copyType, int pipelinePosition) {
		return new BindingEvent(model, target, diff, copyType, pipelinePosition);
	}
}
