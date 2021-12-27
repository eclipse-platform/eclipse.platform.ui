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

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.bind.steps.CommonSteps.BindConfigStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.ConvertFromStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.ConvertToStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.WriteConfigStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueFromStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueReadConfigStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueToStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueUntypedTo;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueWriteConfigStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;

/**
 * Contains {@link Step} interfaces for the fluent databinding API for two-way
 * {@link IObservableValue} bindings. Each of these interfaces define the
 * operations that are allowed in a particular pipeline step.
 *
 * @since 1.11
 */
public final class ValueTwoWaySteps {
	private ValueTwoWaySteps() {
	}

	/**
	 * Refines the return types of the {@link ValueFromStep} methods for use with
	 * two-way bindings.
	 *
	 * @noimplement
	 */
	public interface ValueTwoWayFromStep extends ValueFromStep {
		@Override
		<F> ValueTwoWayConvertToStep<F, ?> from(IObservableValue<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ValueTwoWayToStep<F, T> extends ValueToStep<F, T> {
		@Override
		ValueTwoWayBindConfigStep<F, T, ?> to(IObservableValue<T> to);
	}

	/**
	 * Step for setting the from-to direction converter.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <THIS> self type for this step
	 * @noimplement
	 */
	public interface ValueTwoWayConvertToStep<F, THIS extends ValueTwoWayConvertToStep<F, THIS>> extends //
			ValueTwoWayToStep<F, F>, //
			ConvertToStep<F>, //
			ValueTwoWayConfigStep<F, F, THIS> //
	{
		@Override
		<T> ValueTwoWayConvertFromStep<F, T> convertTo(IConverter<? super F, ? extends T> converter);

		@Override
		ValueTwoWayUntypedToStep<F> defaultConvert();
	}

	/**
	 * Step for settings to-end observable using default converters.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface ValueTwoWayUntypedToStep<F> extends ValueUntypedTo<F> {
		@Override
		<T> ValueTwoWayBindConfigStep<F, T, ?> to(IObservableValue<T> to);
	}

	/**
	 * Step for setting the to-from direction converter.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ValueTwoWayConvertFromStep<F, T> extends ConvertFromStep<F, T> {
		@Override
		ValueTwoWayToStep<F, T> convertFrom(IConverter<? super T, ? extends F> converter);
	}

	/**
	 * Step from configuring the from-end.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface ValueTwoWayConfigStep<F, T, THIS extends ValueTwoWayConfigStep<F, T, THIS>>
			extends //
			WriteConfigStep<F, T, THIS>, //
			ValueReadConfigStep<F, T, THIS>, //
			ValueWriteConfigStep<F, T, THIS> //
	{
		/**
		 * Sets the validator on both directions of the resulting binding. Sets the
		 * {@linkplain UpdateValueStrategy#setAfterGetValidator after-get} validator on
		 * the from-to direction and the
		 * {@link UpdateValueStrategy#setAfterConvertValidator after-convert} validator
		 * on the to-from direction.
		 *
		 * @see UpdateValueStrategy#setAfterGetValidator
		 * @see UpdateValueStrategy#validateAfterConvert
		 * @param validator
		 * @return next step
		 */
		THIS validateTwoWay(IValidator<? super T> validator);
	}

	/**
	 * Step from configuring the to-end and creating the binding.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface ValueTwoWayBindConfigStep<F, T, THIS extends ValueTwoWayBindConfigStep<F, T, THIS>> extends //
			ValueTwoWayConfigStep<F, T, THIS>, //
			BindConfigStep<F, T, THIS> //
	{
	}
}
