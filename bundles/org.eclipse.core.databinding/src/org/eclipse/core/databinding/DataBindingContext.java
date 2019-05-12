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
 *     Brad Reynolds - bugs 159539, 140644, 159940, 116920, 159768
 *     Matthew Hall - bugs 118516, 124684, 218269, 260329, 252732, 146906,
 *                    278550
 *     Boris Bokowski - bug 218269
 *******************************************************************************/
package org.eclipse.core.databinding;

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
 * </p>
 * <ul>
 * <li>Ability to create bindings between {@link IObservableValue observable
 * values}.</li>
 * <li>Ability to create bindings between {@link IObservableList observable
 * lists}.</li>
 * <li>Access to the bindings created by the instance.</li>
 * <li>Access to the list of validation status providers (this includes all
 * bindings).</li>
 * </ul>
 * <p>
 * Multiple contexts can be used at any point in time. One strategy for the
 * management of contexts is the aggregation of validation statuses. For example
 * an <code>IWizardPage</code> could use a single context and the statuses could
 * be aggregated to set the page status and fulfillment. Each page in the
 * <code>IWizard</code> would have its own context instance.
 * </p>
 *
 * @since 1.0
 */
public class DataBindingContext {
	private WritableList<Binding> bindings;
	private WritableList<ValidationStatusProvider> validationStatusProviders;

	/**
	 * Unmodifiable version of {@link #bindings} for public exposure.
	 */
	private IObservableList<Binding> unmodifiableBindings;
	/**
	 * Unmodifiable version of {@link #validationStatusProviders} for public
	 * exposure.
	 */
	private IObservableList<ValidationStatusProvider> unmodifiableStatusProviders;

	private IObservableMap<Binding, IStatus> validationStatusMap;

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
			bindings = new WritableList<>(validationRealm);
			unmodifiableBindings = Observables.unmodifiableObservableList(bindings);

			validationStatusProviders = new WritableList<>(validationRealm);
			unmodifiableStatusProviders = Observables.unmodifiableObservableList(validationStatusProviders);

			validationStatusMap = new ValidationStatusMap(validationRealm, bindings);
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
	public final <T, M> Binding bindValue(IObservableValue<T> targetObservableValue,
			IObservableValue<M> modelObservableValue) {
		return bindValue(targetObservableValue, modelObservableValue, null, null);
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
	public final <T, M> Binding bindValue(
			IObservableValue<T> targetObservableValue, IObservableValue<M> modelObservableValue,
			UpdateValueStrategy<? super T, ? extends M> targetToModel,
			UpdateValueStrategy<? super M, ? extends T> modelToTarget) {
		UpdateValueStrategy<? super T, ? extends M> targetToModelStrategy = targetToModel != null ? targetToModel
				: createTargetToModelUpdateValueStrategy(targetObservableValue, modelObservableValue);
		UpdateValueStrategy<? super M, ? extends T> modelToTargetStrategy = modelToTarget != null ? modelToTarget
				: createModelToTargetUpdateValueStrategy(modelObservableValue, targetObservableValue);
		targetToModelStrategy.fillDefaults(targetObservableValue, modelObservableValue);
		modelToTargetStrategy.fillDefaults(modelObservableValue, targetObservableValue);
		ValueBinding<M, T> result = new ValueBinding<>(
				targetObservableValue, modelObservableValue,
				targetToModelStrategy, modelToTargetStrategy);
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
	protected <M, T> UpdateValueStrategy<M, T> createModelToTargetUpdateValueStrategy(
			IObservableValue<M> fromValue, IObservableValue<T> toValue) {
		return new UpdateValueStrategy<>();
	}

	/**
	 * Returns an update value strategy to be used for copying values from the
	 * from value to the to value. Clients may override.
	 *
	 * @param fromValue
	 * @param toValue
	 * @return a update value strategy
	 */
	protected <T, M> UpdateValueStrategy<T, M> createTargetToModelUpdateValueStrategy(
			IObservableValue<T> fromValue, IObservableValue<M> toValue) {
		return new UpdateValueStrategy<>();
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
	public final <T, M> Binding bindList(IObservableList<T> targetObservableList,
			IObservableList<M> modelObservableList) {
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
	public final <T, M> Binding bindList(
			IObservableList<T> targetObservableList,
			IObservableList<M> modelObservableList,
			UpdateListStrategy<? super T, ? extends M> targetToModel,
			UpdateListStrategy<? super M, ? extends T> modelToTarget) {
		UpdateListStrategy<? super T, ? extends M> targetToModelStrategy = targetToModel != null ? targetToModel
				: createTargetToModelUpdateListStrategy(targetObservableList, modelObservableList);
		UpdateListStrategy<? super M, ? extends T> modelToTargetStrategy = modelToTarget != null ? modelToTarget
				: createModelToTargetUpdateListStrategy(modelObservableList, targetObservableList);
		targetToModelStrategy.fillDefaults(targetObservableList,
				modelObservableList);
		modelToTargetStrategy.fillDefaults(modelObservableList,
				targetObservableList);
		ListBinding<M, T> result = new ListBinding<>(targetObservableList,
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
	protected <M, T> UpdateListStrategy<M, T> createModelToTargetUpdateListStrategy(
			IObservableList<M> modelObservableList,
			IObservableList<T> targetObservableList) {
		return new UpdateListStrategy<>();
	}

	/**
	 * @param targetObservableList
	 * @param modelObservableList
	 * @return an update list strategy
	 */
	protected <T, M> UpdateListStrategy<T, M> createTargetToModelUpdateListStrategy(
			IObservableList<T> targetObservableList,
			IObservableList<M> modelObservableList) {
		return new UpdateListStrategy<>();
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
	public final <T, M> Binding bindSet(IObservableSet<T> targetObservableSet, IObservableSet<M> modelObservableSet) {
		return bindSet(targetObservableSet, modelObservableSet, null, null);
	}

	/**
	 * Creates a {@link Binding} to synchronize the values of two
	 * {@link IObservableSet observable sets}. During synchronization validation
	 * and conversion can be employed to customize the process. For specifics on
	 * the customization of the process see {@link UpdateSetStrategy}.
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
	public final <T, M> Binding bindSet(
			IObservableSet<T> targetObservableSet,
			IObservableSet<M> modelObservableSet,
			UpdateSetStrategy<? super T, ? extends M> targetToModel,
			UpdateSetStrategy<? super M, ? extends T> modelToTarget) {
		if (targetToModel == null)
			targetToModel = createTargetToModelUpdateSetStrategy(
					targetObservableSet, modelObservableSet);
		if (modelToTarget == null)
			modelToTarget = createModelToTargetUpdateSetStrategy(
					modelObservableSet, targetObservableSet);
		targetToModel.fillDefaults(targetObservableSet, modelObservableSet);
		modelToTarget.fillDefaults(modelObservableSet, targetObservableSet);
		SetBinding<? super M, ? extends T> result = new SetBinding<>(targetObservableSet,
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
	protected <T, M> UpdateSetStrategy<T, M> createTargetToModelUpdateSetStrategy(
			IObservableSet<T> targetObservableSet,
			IObservableSet<M> modelObservableSet) {
		return new UpdateSetStrategy<>();
	}

	/**
	 * @param modelObservableSet
	 * @param targetObservableSet
	 * @return a default set update strategy
	 * @since 1.1
	 */
	protected <M, T> UpdateSetStrategy<M, T> createModelToTargetUpdateSetStrategy(
			IObservableSet<M> modelObservableSet,
			IObservableSet<T> targetObservableSet) {
		return new UpdateSetStrategy<>();
	}

	/**
	 * Disposes of this data binding context and all bindings and validation
	 * status providers that were added to this context. This method must be
	 * called in the {@link #getValidationRealm() validation realm}.
	 */
	public final void dispose() {
		Binding[] bindingArray = bindings.toArray(new Binding[bindings.size()]);
		for (Binding binding : bindingArray) {
			binding.dispose();
		}
		ValidationStatusProvider[] statusProviderArray = validationStatusProviders
				.toArray(new ValidationStatusProvider[validationStatusProviders
						.size()]);
		for (ValidationStatusProvider statusProvider : statusProviderArray) {
			if (!statusProvider.isDisposed()) {
				statusProvider.dispose();
			}
		}
	}

	/**
	 * Returns an unmodifiable {@link IObservableList} of all bindings in order
	 * by time of addition.
	 *
	 * @return an unmodifiable {@link IObservableList} of all bindings
	 */
	public final IObservableList<Binding> getBindings() {
		return unmodifiableBindings;
	}

	/**
	 * Returns an unmodifiable {@link IObservableList} of all validation status
	 * providers in order by time of addition.
	 *
	 * @return an unmodifiable {@link IObservableList} of all validation status
	 *         providers
	 * @since 1.1
	 */
	public final IObservableList<ValidationStatusProvider> getValidationStatusProviders() {
		return unmodifiableStatusProviders;
	}

	/**
	 * Returns an {@link IObservableMap} mapping from bindings to current
	 * validation statuses. The keys of the map are the bindings returned by
	 * {@link #getBindings()}, and the values are the current IStatus objects
	 * for each binding.
	 *
	 * @return the observable map from bindings to status objects.
	 *
	 * @deprecated as of 1.1, please use {@link #getValidationStatusProviders()}
	 */
	@Deprecated
	public final IObservableMap<Binding, IStatus> getValidationStatusMap() {
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
		for (Binding binding : bindings) {
			binding.updateTargetToModel();
		}
	}

	/**
	 * Updates all target observable objects to reflect the current state of the
	 * model observable objects.
	 *
	 */
	public final void updateTargets() {
		for (Binding binding : bindings) {
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
