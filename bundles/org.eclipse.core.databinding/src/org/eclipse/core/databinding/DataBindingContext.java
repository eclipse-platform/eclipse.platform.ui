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

import java.util.Iterator;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.ValidationStatusMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * A context for binding observable objects.
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
				bindings);
	}

	/**
	 * @param targetObservableValue
	 * @param modelObservableValue
	 * @param targetToModel
	 * @param modelToTarget
	 * @return a binding
	 */
	public final Binding bindValue(IObservableValue targetObservableValue,
			IObservableValue modelObservableValue,
			UpdateValueStrategy targetToModel, UpdateValueStrategy modelToTarget) {
		UpdateValueStrategy targetToModelStrategy = targetToModel != null ? targetToModel
						: createTargetToModelUpdateValueStrategy(targetObservableValue, modelObservableValue);
		UpdateValueStrategy modelToTargetStrategy = modelToTarget != null ? modelToTarget
				: createModelToTargetUpdateValueStrategy(modelObservableValue, targetObservableValue);
		targetToModelStrategy.fillDefaults(targetObservableValue, modelObservableValue);
		modelToTargetStrategy.fillDefaults(modelObservableValue, targetObservableValue);
		ValueBinding result = new ValueBinding(targetObservableValue,
				modelObservableValue, targetToModelStrategy,
				modelToTargetStrategy);
		result.init(this);
		return result;
	}

	/**
	 * Returns an update value strategy to be used for copying values from the
	 * from value to the to value. Clients may override.
	 * 
	 * @param fromValue
	 * @param toValue
	 * @return a update value strategy
	 */
	protected UpdateValueStrategy createModelToTargetUpdateValueStrategy(
			IObservableValue fromValue, IObservableValue toValue) {
		return new UpdateValueStrategy();
	}

	/**
	 * Returns an update value strategy to be used for copying values from the
	 * from value to the to value. Clients may override.
	 * 
	 * @param fromValue
	 * @param toValue
	 * @return a update value strategy
	 */
	protected UpdateValueStrategy createTargetToModelUpdateValueStrategy(
			IObservableValue fromValue, IObservableValue toValue) {
		return new UpdateValueStrategy();
	}
	
	/**
	 * Binds two observable lists using the given update list strategies.
	 * 
	 * @param targetObservableList
	 * @param modelObservableList
	 * @param targetToModel 
	 * @param modelToTarget TODO
	 * @return a Binding synchronizing the state of the two observables
	 */
	public final Binding bindList(IObservableList targetObservableList,
			IObservableList modelObservableList,
			UpdateListStrategy targetToModel, UpdateListStrategy modelToTarget) {
		UpdateListStrategy targetToModelStrategy = targetToModel != null ? targetToModel
				: createTargetToModelUpdateListStrategy(targetObservableList,
						modelObservableList);
		UpdateListStrategy modelToTargetStrategy = modelToTarget != null ? modelToTarget
				: createModelToTargetUpdateListStrategy(modelObservableList,
						targetObservableList);
		targetToModelStrategy.fillDefaults(targetObservableList,
				modelObservableList);
		modelToTargetStrategy.fillDefaults(modelObservableList,
				targetObservableList);
		ListBinding result = new ListBinding(targetObservableList,
				modelObservableList, targetToModelStrategy,
				modelToTargetStrategy);
		result.init(this);
		return result;
	}

	/**
	 * @param modelObservableList
	 * @param targetObservableList
	 * @return an update list strategy
	 */
	protected UpdateListStrategy createModelToTargetUpdateListStrategy(
			IObservableList modelObservableList,
			IObservableList targetObservableList) {
		return new UpdateListStrategy();
	}

	/**
	 * @param targetObservableList
	 * @param modelObservableList
	 * @return an update list strategy 
	 */
	protected UpdateListStrategy createTargetToModelUpdateListStrategy(
			IObservableList targetObservableList,
			IObservableList modelObservableList) {
		return new UpdateListStrategy();
	}

	/**
	 * Disposes of this data binding context and all bindings that were added to
	 * this context.
	 */
	public final void dispose() {
		Binding[] bindingArray = (Binding[]) bindings.toArray(new Binding[bindings.size()]);
		for (int i = 0; i < bindingArray.length; i++) {
			bindingArray[i].dispose();
		}
	}

	/**
	 * Returns an unmodifiable observable list with elements of type {@link Binding},
	 * ordered by time of addition.
	 * 
	 * @return the observable list containing all bindings
	 */
	public final IObservableList getBindings() {
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
	public final IObservableMap getValidationStatusMap() {
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
	}

	/**
	 * Updates all model observable objects to reflect the current state of the
	 * target observable objects.
	 * 
	 */
	public final void updateModels() {
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.updateTargetToModel();
		}
	}

	/**
	 * Updates all target observable objects to reflect the current state of the
	 * model observable objects.
	 * 
	 */
	public final void updateTargets() {
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.updateModelToTarget();
		}
	}

	/**
	 * Removes the given binding.
	 * 
	 * @param binding
	 * @return <code>true</code> if was associated with the context,
	 *         <code>false</code> if not
	 */
	public boolean removeBinding(Binding binding) {
		return bindings.remove(binding);
	}

	/**
	 * Returns the validation realm.
	 * 
	 * @return the realm for the validation observables
	 * @see Realm
	 */
	public final Realm getValidationRealm() {
		return validationRealm;
	}
}