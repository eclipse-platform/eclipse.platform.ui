/*******************************************************************************
 * Copyright (c) 2022 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.databinding.bind.steps;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.bind.steps.ListOneWaySteps.ListOneWayFromStep;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayFromStep;
import org.eclipse.core.databinding.bind.steps.SetOneWaySteps.SetOneWayFromStep;
import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps.SetTwoWayFromStep;
import org.eclipse.core.databinding.bind.steps.ValueOneWaySteps.ValueOneWayFromStep;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps.ValueTwoWayFromStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * Contains {@link Step} interfaces for the fluent databinding API that are
 * common for both {@link IObservableValue}, {@link IObservableList} and
 * {@link IObservableSet} bindings. Each of these interfaces define the
 * operations that are allowed in a particular pipeline step.
 *
 * @noextend
 * @since 1.11
 */
public final class CommonSteps {
	private CommonSteps() {
	}

	/**
	 * Step for setting the direction of the binding.
	 *
	 * @param <SELF> self type
	 *
	 * @noimplement
	 */
	public interface DirectionStep<SELF extends DirectionStep<SELF>> extends Step {
		/**
		 * Sets the binding to have the target-to-model direction. This is the default.
		 * This method has no effect, but using it might make the calling code clearer.
		 *
		 * @return next step
		 */
		SELF targetToModel();

		/**
		 * Sets the binding to have the model-to-target direction.
		 *
		 * @return next step
		 */
		SELF modelToTarget();
	}

	/**
	 * Step for settings the from-end observable of one-way-bindings. It this step
	 * the value-type (value, list or set) is chosen.
	 *
	 * @param <SELF> self type
	 *
	 * @noimplement
	 */
	public interface OneWayConfigAndFromStep<SELF extends OneWayConfigAndFromStep<SELF>> extends //
			DirectionStep<SELF>, //
			ValueOneWayFromStep, //
			ListOneWayFromStep, //
			SetOneWayFromStep //
	{
	}

	/**
	 * Step for settings the from-end observable of two-way-bindings. It this step
	 * the value-type (value, list or set) is chosen.
	 *
	 * @param <SELF> self type
	 *
	 * @noimplement
	 */
	public interface TwoWayConfigAndFromStep<SELF extends TwoWayConfigAndFromStep<SELF>> extends //
			DirectionStep<SELF>, //
			ValueTwoWayFromStep, //
			ListTwoWayFromStep, //
			SetTwoWayFromStep //
	{
	}

	/**
	 * Step for converting between from- and to-types.
	 *
	 * @noimplement
	 * @param <F> type of the from-end observable
	 */
	public interface ConvertToStep<F> extends Step {

		/**
		 * Sets the from-to direction converter on the resulting binding. The defines
		 * the type of the to-end observable.
		 *
		 * @see UpdateValueStrategy#setConverter
		 * @param converter the converter to set
		 * @param <T>       type of the to-end observable
		 * @return next step
		 */
		<T> Step convertTo(IConverter<? super F, ? extends T> converter);

		/**
		 * Makes the resulting binding use default converters between the two
		 * observables. Because of this an observable of any type may be used.
		 * <p>
		 * For example, converters to and from strings and numeric types are available.
		 * <p>
		 * However, the {@link IObservableValue#getValueType} or
		 * {@link IObservableCollection#getElementType} of both the observables must be
		 * non-null.
		 * <p>
		 * See {@link DataBindingContext} for information about types that have default
		 * converters and how they work.
		 *
		 * @see UpdateValueStrategy#UpdateValueStrategy(boolean, int)
		 * @return next step
		 */
		Step defaultConvert();
	}

	/**
	 * Step for setting the to-from converter.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ConvertFromStep<F, T> extends Step {
		/**
		 * Sets the from-to direction converter on the resulting binding. This defines
		 * the type of the to-end observable.
		 *
		 * @see UpdateValueStrategy#setConverter
		 * @param converter the converter to set
		 * @return next step
		 */
		Step convertFrom(IConverter<? super T, ? extends F> converter);
	}

	/**
	 * Step for updating the configuration for an observable that is being written
	 * to. This is the to-end observable for a one-way binding and both observables
	 * for a two-way binding.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface WriteConfigStep<F, T, THIS extends WriteConfigStep<F, T, THIS>> extends Step {
		/**
		 * Configures the resulting binding to update the written observable only when
		 * {@link DataBindingContext#updateTargets} or
		 * {@link DataBindingContext#updateModels} is called manually.
		 *
		 * @implNote Here methods can be added that applies to the end of the binding
		 *           that is being written to.
		 *
		 * @see UpdateValueStrategy#POLICY_ON_REQUEST
		 * @see UpdateListStrategy#POLICY_ON_REQUEST
		 * @see UpdateSetStrategy#POLICY_ON_REQUEST
		 *
		 * @return next step
		 */
		THIS updateOnlyOnRequest();
	}

	/**
	 * @implNote Here methods can be added that applies to the end of the binding
	 *           that is being read from.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface ReadConfigStep<F, T, THIS extends ReadConfigStep<F, T, THIS>> {
	}

	/**
	 * The last step in the binding pipeline, where the {@link BindConfigStep#bind}
	 * method is accessible.
	 *
	 * @implNote Here methods can be added that applies to the entire binding.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface BindConfigStep<F, T, THIS extends BindConfigStep<F, T, THIS>>
			extends WriteConfigStep<F, T, THIS> {
		/**
		 * Creates the binding according to the configuration is the previous steps.
		 * Creates {@link UpdateValueStrategy} objects and calls
		 * {@link DataBindingContext#bindValue}.
		 *
		 * @see DataBindingContext#bindValue
		 * @see DataBindingContext#bindList
		 * @see DataBindingContext#bindSet
		 *
		 * @param bindingContext used the create the binding
		 * @return the created binding
		 */
		Binding bind(DataBindingContext bindingContext);

		/**
		 * Short-hand way to create a binding. This method creates a new
		 * {@link DataBindingContext}, and calls {@link #bind(DataBindingContext)}.
		 * <p>
		 * Warnings: This method is not suitable if performance is critical or if the
		 * bindings need to be disposed.
		 *
		 * @see #bind(DataBindingContext)
		 * @see DataBindingContext#bindValue
		 * @see DataBindingContext#bindList
		 * @see DataBindingContext#bindSet
		 *
		 * @return the created binding
		 */
		Binding bindWithNewContext();
	}
}
