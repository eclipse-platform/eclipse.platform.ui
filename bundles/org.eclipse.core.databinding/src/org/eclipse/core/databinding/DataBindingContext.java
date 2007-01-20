/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159539
 *     Brad Reynolds - bug 140644
 *     Brad Reynolds - bug 159940
 *     Brad Reynolds - bug 116920, 159768
 *******************************************************************************/
package org.eclipse.core.databinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.ListBinding;
import org.eclipse.core.internal.databinding.ValidationStatusMap;
import org.eclipse.core.internal.databinding.ValueBinding;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A context for binding observable objects. This class is not intended to be
 * subclassed by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.1
 * 
 */
public class DataBindingContext {
	private List bindingEventListeners = new ArrayList();

	private WritableList bindings;
    
    /**
     * Unmodifiable version of {@link #bindings} for exposure publicly.
     */
    private IObservableList unmodifiableBindings;

	private ComputedValue validationStatus;

	private IObservableMap validationStatusMap;

	private Realm validationRealm;

	/**
	 * Creates a data binding context, using the current default realm for the
	 * validation observables.
	 */
	public DataBindingContext() {
		this(Realm.getDefault());
	}

	/**
	 * Creates a data binding context using the given realm for the validation observables.
	 * 
	 * @param validationRealm
	 *            the realm to be used for the validation observables
	 */
	public DataBindingContext(Realm validationRealm) {
		Assert.isNotNull(validationRealm);
		this.validationRealm = validationRealm;
		bindings = new WritableList(validationRealm);
        
        unmodifiableBindings = Observables.unmodifiableObservableList(bindings);
		validationStatus = new ComputedValue(validationRealm) {
			protected Object calculate() {
				for(Iterator it=bindings.iterator(); it.hasNext();) {
					Binding binding = (Binding) it.next();
					IStatus status = (IStatus) binding.getValidationStatus().getValue();
					if(!status.isOK()) {
						return status;
					}
				}
				return Status.OK_STATUS;
			}
		};
		validationStatusMap = new ValidationStatusMap(validationRealm, bindings,
				false);
	}

	/**
	 * Add a listener to the set of listeners that will be notified when an
	 * event occurs in the data flow pipeline that is managed by any binding
	 * created by this data binding context.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addBindingEventListener(IBindingListener listener) {
		bindingEventListeners.add(listener);
	}

	/**
	 * Binds two observable values using converter and validator as specified in
	 * bindSpec. If bindSpec is null, a default converter and validator is used.
	 * 
	 * @param targetObservableValue
	 * @param modelObservableValue
	 * @param bindSpec
	 *            the bind spec, or null. Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bindValue(IObservableValue targetObservableValue,
			IObservableValue modelObservableValue, BindSpec bindSpec) {
		if (bindSpec == null) {
			bindSpec = new DefaultBindSpec();
		}
		Binding result = new ValueBinding(this, targetObservableValue,
				modelObservableValue, bindSpec);
        bindings.add(result);
		return result;
	}

	/**
	 * Binds two observable lists using converter and validator as specified in
	 * bindSpec. If bindSpec is null, a default converter and validator is used.
	 * 
	 * @param targetObservableList
	 * @param modelObservableList
	 * @param bindSpec
	 *            the bind spec, or null. Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bindList(IObservableList targetObservableList,
			IObservableList modelObservableList, BindSpec bindSpec) {
		if (bindSpec == null) {
			bindSpec = new DefaultBindSpec();
		}
		Binding result = new ListBinding(this, targetObservableList,
				modelObservableList, bindSpec);
        bindings.add(result);
		return result;
	}

	/**
	 * Disposes of this data binding context and all observable objects created
	 * in this context.
	 */
	public void dispose() {
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.dispose();
		}
	}

	protected IStatus fireBindingEvent(BindingEvent event) {
		IStatus result = Status.OK_STATUS;
		for (Iterator bindingEventIter = bindingEventListeners.iterator(); bindingEventIter
				.hasNext();) {
			IBindingListener listener = (IBindingListener) bindingEventIter
					.next();
			result = listener.bindingEvent(event);
			if (!result.isOK())
				break;
		}
		return result;
	}

	/**
	 * Returns an unmodifiable observable list with elements of type Binding, ordered by
	 * creation time
	 * 
	 * @return the observable list containing all bindings
	 */
	public IObservableList getBindings() {
		return unmodifiableBindings;
	}

	/**
	 * Returns an observable value of type IStatus, containing the most
	 * recent full validation error, i.e. the last element of the list returned
	 * by getValidationErrors().
	 * 
	 * @return the validation observable
	 */
	public IObservableValue getValidationStatus() {
		return validationStatus;
	}

	/**
	 * Returns an observable list with elements of type IStatus, ordered
	 * by the time of detection
	 * 
	 * @return the observable list containing all validation errors
	 */
	public IObservableMap getValidationStatusMap() {
		return validationStatusMap;
	}

	/**
	 * Adds the given binding to this data binding context.
	 * 
	 * @param binding
	 *            The binding to add.
	 */
	public void addBinding(Binding binding) {
		bindings.add(binding);
		binding.setDataBindingContext(this);
	}

	/**
	 * Removes a listener from the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by any binding
	 * created by this data binding context.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		bindingEventListeners.remove(listener);
	}

	/**
	 * Updates all model observable objects to reflect the current state of the
	 * target observable objects.
	 * 
	 */
	public void updateModels() {
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.updateModelFromTarget();
		}
	}

	/**
	 * Updates all target observable objects to reflect the current state of the
	 * model observable objects.
	 * 
	 */
	public void updateTargets() {
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.updateTargetFromModel();
		}
	}
    
    /**
     * Removes the binding.
     * 
     * @param binding
     * @return <code>true</code> if was associated with the context,
     *         <code>false</code> if not
     */
    public boolean removeBinding(Binding binding) {
        if (bindings.contains(binding)) {
            binding.setDataBindingContext(null);
        }

        return bindings.remove(binding);
    }

	/**
	 * @return the realm for the validation observables
	 */
	public Realm getValidationRealm() {
		return validationRealm;
	}
}