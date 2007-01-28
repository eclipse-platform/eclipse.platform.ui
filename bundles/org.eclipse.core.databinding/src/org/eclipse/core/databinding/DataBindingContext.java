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
 * @since 1.0
 * 
 */
public class DataBindingContext {
	private List bindingEventListeners = new ArrayList();

	private WritableList bindings;

	/**
	 * Unmodifiable version of {@link #bindings} for public exposure.
	 */
	private IObservableList unmodifiableBindings;

	private IObservableMap validationStatusMap;

	private Realm validationRealm;

	/**
	 * Creates a data binding context, using the current default realm for the
	 * validation observables.
	 * 
	 * @see Realm
	 */
	public DataBindingContext() {
		this(Realm.getDefault());
	}

	/**
	 * Creates a data binding context using the given realm for the validation
	 * observables.
	 * 
	 * @param validationRealm
	 *            the realm to be used for the validation observables
	 * 
	 * @see Realm
	 */
	public DataBindingContext(Realm validationRealm) {
		Assert.isNotNull(validationRealm);
		this.validationRealm = validationRealm;
		bindings = new WritableList(validationRealm);

		unmodifiableBindings = Observables.unmodifiableObservableList(bindings);
		validationStatusMap = new ValidationStatusMap(validationRealm,
				bindings, false);
	}

	/**
	 * Add a listener to the set of listeners that will be notified when an
	 * event occurs in the data flow pipeline managed by any binding in this
	 * data binding context.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addBindingEventListener(IBindingListener listener) {
		bindingEventListeners.add(listener);
	}

	/**
	 * Binds two observable values using converter and validator as specified in
	 * bindSpec. If bindSpec is null, default converters and validators as
	 * defined by {@link DefaultBindSpec} will be used.
	 * 
	 * @param targetObservableValue
	 * @param modelObservableValue
	 * @param bindSpec
	 *            the bind spec, or null. A bindSpec object must not be reused
	 *            or changed after it is passed to this method.
	 * @return a Binding synchronizing the state of the two observables
	 */
	public Binding bindValue(IObservableValue targetObservableValue,
			IObservableValue modelObservableValue, BindSpec bindSpec) {
		if (bindSpec == null) {
			bindSpec = new DefaultBindSpec();
		}
		Binding result = new ValueBinding(targetObservableValue,
				modelObservableValue, bindSpec);
		result.init(this);
		return result;
	}

	/**
	 * Binds two observable lists using converter and validator as specified in
	 * bindSpec. If bindSpec is null, default converters and validators as
	 * defined by {@link DefaultBindSpec} will be used.
	 * 
	 * @param targetObservableList
	 * @param modelObservableList
	 * @param bindSpec
	 *            the bind spec, or null. A bindSpec object must not be reused
	 *            or changed after it is passed to this method.
	 * @return a Binding synchronizing the state of the two observables
	 */
	public Binding bindList(IObservableList targetObservableList,
			IObservableList modelObservableList, BindSpec bindSpec) {
		if (bindSpec == null) {
			bindSpec = new DefaultBindSpec();
		}
		Binding result = new ListBinding(targetObservableList,
				modelObservableList, bindSpec);
		result.init(this);
		return result;
	}

	/**
	 * Disposes of this data binding context and all bindings that were added to
	 * this context.
	 */
	public void dispose() {
		Binding[] bindingArray = (Binding[]) bindings.toArray(new Binding[bindings.size()]);
		for (int i = 0; i < bindingArray.length; i++) {
			bindingArray[i].dispose();
		}
	}

	/**
	 * Fires a binding event to all binding listeners. To be called by bindings
	 * in this data binding context.
	 * 
	 * @param event
	 * @return a status object  
	 */
	protected IStatus fireBindingEvent(BindingEvent event) {
		IStatus result = Status.OK_STATUS;
		for (Iterator bindingEventIter = bindingEventListeners.iterator(); bindingEventIter
				.hasNext();) {
			IBindingListener listener = (IBindingListener) bindingEventIter
					.next();
			result = listener.handleBindingEvent(event);
			if (!result.isOK())
				break;
		}
		return result;
	}

	/**
	 * Returns an unmodifiable observable list with elements of type {@link Binding},
	 * ordered by time of addition.
	 * 
	 * @return the observable list containing all bindings
	 */
	public IObservableList getBindings() {
		return unmodifiableBindings;
	}

	/**
	 * Returns an observable map from bindings (type: {@link Binding}) to
	 * statuses (type: {@link IStatus}). The keys of the map are the bindings
	 * returned by {@link #getBindings()}, and the values are the current
	 * validaion status objects for each binding.
	 * 
	 * @return the observable map from bindings to status objects.
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
	/* package */ void addBinding(Binding binding) {
		bindings.add(binding);
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
	 * Removes the given binding.
	 * 
	 * @param binding
	 * @return <code>true</code> if was associated with the context,
	 *         <code>false</code> if not
	 */
	/* package */ boolean removeBinding(Binding binding) {
		return bindings.remove(binding);
	}

	/**
	 * Returns the validation realm.
	 * 
	 * @return the realm for the validation observables
	 * @see Realm
	 */
	public Realm getValidationRealm() {
		return validationRealm;
	}
}