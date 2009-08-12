/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 159539, 140644, 159940, 116920, 159768
 *     Matthew Hall - bugs 118516, 124684, 218269, 260329, 252732, 146906,
 *                    278550
 *     Boris Bokowski - bug 218269
 *******************************************************************************/
package org.eclipse.core.databinding;

import java.util.Iterator;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.ValidationStatusMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * A DataBindingContext is the point of contact for the creation and management
 * of {@link Binding bindings}, and aggregates validation statuses of its
 * bindings, or more generally, its validation status providers.
 * <p>
 * A DataBindingContext provides the following abilities:
 * <ul>
 * <li>Ability to create bindings between
 * {@link IObservableValue observable values}.</li>
 * <li>Ability to create bindings between
 * {@link IObservableList observable lists}.</li>
 * <li>Access to the bindings created by the instance.</li>
 * <li>Access to the list of validation status providers (this includes all
 * bindings).</li>
 * </ul>
 * </p>
 * <p>
 * Multiple contexts can be used at any point in time. One strategy for the
 * management of contexts is the aggregation of validation statuses. For example
 * an <code>IWizardPage</code> could use a single context and the statuses
 * could be aggregated to set the page status and fulfillment. Each page in the
 * <code>IWizard</code> would have its own context instance.
 * </p>
 * 
 * @since 1.0
 */
public class DataBindingContext {
	private WritableList bindings;
	private WritableList validationStatusProviders;

	/**
	 * Unmodifiable version of {@link #bindings} for public exposure.
	 */
	private IObservableList unmodifiableBindings;
	/**
	 * Unmodifiable version of {@link #validationStatusProviders} for public
	 * exposure.
	 */
	private IObservableList unmodifiableStatusProviders;

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
		Assert.isNotNull(validationRealm, "Validation realm cannot be null"); //$NON-NLS-1$
		this.validationRealm = validationRealm;

		ObservableTracker.setIgnore(true);
		try {
			bindings = new WritableList(validationRealm);
			unmodifiableBindings = Observables
					.unmodifiableObservableList(bindings);

			validationStatusProviders = new WritableList(validationRealm);
			unmodifiableStatusProviders = Observables
					.unmodifiableObservableList(validationStatusProviders);

			validationStatusMap = new ValidationStatusMap(validationRealm,
					bindings);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	/**
	 * Creates a {@link Binding} to synchronize the values of two
	 * {@link IObservableValue observable values}. This method is an alias for
	 * <code>bindValue(targetObservableValue, modelObservableValue, null,
	 * null)</code>.
	 * 
	 * @param targetObservableValue
	 *            target value, commonly a UI widget
	 * @param modelObservableValue
	 *            model value
	 * @return created binding
	 * @since 1.2
	 */
	public final Binding bindValue(IObservableValue targetObservableValue,
			IObservableValue modelObservableValue) {
		return bindValue(targetObservableValue, modelObservableValue, null,
				null);
	}

	/**
	 * Creates a {@link Binding} to synchronize the values of two
	 * {@link IObservableValue observable values}. During synchronization
	 * validation and conversion can be employed to customize the process. For
	 * specifics on the customization of the process see
	 * {@link UpdateValueStrategy}.
	 * 
	 * @param targetObservableValue
	 *            target value, commonly a UI widget
	 * @param modelObservableValue
	 *            model value
	 * @param targetToModel
	 *            strategy to employ when the target is the source of the change
	 *            and the model is the destination
	 * @param modelToTarget
	 *            strategy to employ when the model is the source of the change
	 *            and the target is the destination
	 * @return created binding
	 * 
	 * @see UpdateValueStrategy
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
	 * Creates a {@link Binding} to synchronize the values of two
	 * {@link IObservableList observable lists}. This method is an alias for
	 * <code>bindList(targetObservableList, modelObservableList, null,
	 * null)</code>.
	 * 
	 * @param targetObservableList
	 *            target list, commonly a list representing a list in the UI
	 * @param modelObservableList
	 *            model list
	 * @return created binding
	 * 
	 * @see UpdateListStrategy
	 * @since 1.2
	 */
	public final Binding bindList(IObservableList targetObservableList,
			IObservableList modelObservableList) {
		return bindList(targetObservableList, modelObservableList, null, null);
	}

	/**
	 * Creates a {@link Binding} to synchronize the values of two
	 * {@link IObservableList observable lists}. During synchronization
	 * validation and conversion can be employed to customize the process. For
	 * specifics on the customization of the process see
	 * {@link UpdateListStrategy}.
	 * 
	 * @param targetObservableList
	 *            target list, commonly a list representing a list in the UI
	 * @param modelObservableList
	 *            model list
	 * @param targetToModel
	 *            strategy to employ when the target is the source of the change
	 *            and the model is the destination
	 * @param modelToTarget
	 *            strategy to employ when the model is the source of the change
	 *            and the target is the destination
	 * @return created binding
	 * 
	 * @see UpdateListStrategy
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
	 * Creates a {@link Binding} to synchronize the values of two
	 * {@link IObservableSet observable sets}. This method is an alias for
	 * <code>bindSet(targetObservableValue, modelObservableValue, null,
	 * null)</code>.
	 * 
	 * @param targetObservableSet
	 *            target set, commonly a set representing a set in the UI
	 * @param modelObservableSet
	 *            model set
	 * @return created binding
	 * @since 1.2
	 */
	public final Binding bindSet(IObservableSet targetObservableSet,
			IObservableSet modelObservableSet) {
		return bindSet(targetObservableSet, modelObservableSet, null, null);
	}

	/**
	 * Creates a {@link Binding} to synchronize the values of two
	 * {@link IObservableSet observable sets}. During synchronization
	 * validation and conversion can be employed to customize the process. For
	 * specifics on the customization of the process see
	 * {@link UpdateSetStrategy}.
	 * 
	 * @param targetObservableSet
	 *            target set, commonly a set representing a set in the UI
	 * @param modelObservableSet
	 *            model set
	 * @param targetToModel
	 *            strategy to employ when the target is the source of the change
	 *            and the model is the destination
	 * @param modelToTarget
	 *            strategy to employ when the model is the source of the change
	 *            and the target is the destination
	 * @return created binding
	 * @since 1.1
	 */
	public final Binding bindSet(IObservableSet targetObservableSet,
			IObservableSet modelObservableSet, UpdateSetStrategy targetToModel,
			UpdateSetStrategy modelToTarget) {
		if (targetToModel == null)
			targetToModel = createTargetToModelUpdateSetStrategy(
					targetObservableSet, modelObservableSet);
		if (modelToTarget == null)
			modelToTarget = createModelToTargetUpdateSetStrategy(
					modelObservableSet, targetObservableSet);
		targetToModel.fillDefaults(targetObservableSet, modelObservableSet);
		modelToTarget.fillDefaults(modelObservableSet, targetObservableSet);
		SetBinding result = new SetBinding(targetObservableSet,
				modelObservableSet, targetToModel, modelToTarget);
		result.init(this);
		return result;
	}

	/**
	 * @param targetObservableSet 
	 * @param modelObservableSet 
	 * @return a default set update strategy
	 * @since 1.1
	 */
	protected UpdateSetStrategy createTargetToModelUpdateSetStrategy(
			IObservableSet targetObservableSet,
			IObservableSet modelObservableSet) {
		return new UpdateSetStrategy();
	}

	/**
	 * @param modelObservableSet 
	 * @param targetObservableSet 
	 * @return a default set update strategy 
	 * @since 1.1
	 */
	protected UpdateSetStrategy createModelToTargetUpdateSetStrategy(
			IObservableSet modelObservableSet,
			IObservableSet targetObservableSet) {
		return new UpdateSetStrategy();
	}

	/**
	 * Disposes of this data binding context and all bindings and validation
	 * status providers that were added to this context. This method must be
	 * called in the {@link #getValidationRealm() validation realm}.
	 */
	public final void dispose() {
		Binding[] bindingArray = (Binding[]) bindings.toArray(new Binding[bindings.size()]);
		for (int i = 0; i < bindingArray.length; i++) {
			bindingArray[i].dispose();
		}
		ValidationStatusProvider[] statusProviderArray = (ValidationStatusProvider[]) validationStatusProviders
				.toArray(new ValidationStatusProvider[validationStatusProviders
						.size()]);
		for (int i = 0; i < statusProviderArray.length; i++) {
			if (!statusProviderArray[i].isDisposed())
				statusProviderArray[i].dispose();
		}
	}

	/**
	 * Returns an unmodifiable {@link IObservableList} &lt; {@link Binding} &gt;
	 * of all bindings in order by time of addition.
	 * 
	 * @return an unmodifiable {@link IObservableList} &lt; {@link Binding} &gt;
	 *         of all bindings
	 */
	public final IObservableList getBindings() {
		return unmodifiableBindings;
	}

	/**
	 * Returns an unmodifiable an unmodifiable {@link IObservableList} &lt;
	 * {@link ValidationStatusProvider} &gt; of all validation status providers
	 * in order by time of addition.
	 * 
	 * @return an unmodifiable {@link IObservableList} &lt;
	 *         {@link ValidationStatusProvider} &gt; of all validation status
	 *         providers
	 * @since 1.1
	 */
	public final IObservableList getValidationStatusProviders() {
		return unmodifiableStatusProviders;
	}

	/**
	 * Returns an {@link IObservableMap} &lt; {@link Binding}, {@link IStatus}
	 * &gt; mapping from bindings to current validation statuses. The keys of the
	 * map are the bindings returned by {@link #getBindings()}, and the values
	 * are the current IStatus objects for each binding.
	 * 
	 * @return the observable map from bindings to status objects.
	 * 
	 * @deprecated as of 1.1, please use {@link #getValidationStatusProviders()}
	 */
	public final IObservableMap getValidationStatusMap() {
		return validationStatusMap;
	}

	/**
	 * Adds the given binding to this data binding context. This will also add
	 * the given binding to the list of validation status providers.
	 * 
	 * @param binding
	 *            The binding to add.
	 * @see #addValidationStatusProvider(ValidationStatusProvider)
	 * @see #getValidationStatusProviders()
	 */
	public void addBinding(Binding binding) {
		addValidationStatusProvider(binding);
		bindings.add(binding);
	}

	/**
	 * Adds the given validation status provider to this data binding context.
	 * 
	 * @param validationStatusProvider
	 *            The validation status provider to add.
	 * @since 1.1
	 */
	public void addValidationStatusProvider(
			ValidationStatusProvider validationStatusProvider) {
		validationStatusProviders.add(validationStatusProvider);
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
		return bindings.remove(binding) && removeValidationStatusProvider(binding);
	}

	/**
	 * Removes the validation status provider.
	 * 
	 * @param validationStatusProvider
	 * @return <code>true</code> if was associated with the context,
	 *         <code>false</code> if not
	 * @since 1.1
	 */
	public boolean removeValidationStatusProvider(
			ValidationStatusProvider validationStatusProvider) {
		return validationStatusProviders.remove(validationStatusProvider);
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
