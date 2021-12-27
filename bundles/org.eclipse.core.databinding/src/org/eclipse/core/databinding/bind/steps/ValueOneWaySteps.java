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

import org.eclipse.core.databinding.bind.steps.CommonSteps.BindConfigStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.ConvertToStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueFromStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueReadConfigStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueToStep;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueUntypedTo;
import org.eclipse.core.databinding.bind.steps.ValueCommonSteps.ValueWriteConfigStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * Contains {@link Step} interfaces for the fluent databinding API for one-way
 * {@link IObservableValue} bindings. Each of these interfaces define the
 * operations that are allowed in a particular pipeline step.
 *
 * @since 1.11
 */
public final class ValueOneWaySteps {
	private ValueOneWaySteps() {
	}

	/**
	 * Refines the return types of the {@link ValueFromStep} methods for use with
	 * one-way bindings.
	 *
	 * @noimplement
	 */
	public interface ValueOneWayFromStep extends ValueFromStep {
		@Override
		<F> ValueOneWayConvertStep<F, ?> from(IObservableValue<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ValueOneWayToStep<F, T> extends ValueToStep<F, T> {
		@Override
		ValueOneWayBindWriteConfigStep<F, T, ?> to(IObservableValue<T> to);
	}

	/**
	 * Step for converting between from- and to-types.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <THIS> self type for this step
	 * @noimplement
	 */
	public interface ValueOneWayConvertStep<F, THIS extends ValueOneWayConvertStep<F, THIS>> extends //
			ValueOneWayToStep<F, F>, //
			ValueToStep<F, F>, //
			ConvertToStep<F>, //
			ValueReadConfigStep<F, F, THIS> //
	{
		@Override
		ValueOneWayUntypedTo<F> defaultConvert();

		@Override
		<T2> ValueOneWayToStep<F, T2> convertTo(IConverter<? super F, ? extends T2> converter);
	}

	/**
	 * Step for settings to-end observable using default converters.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface ValueOneWayUntypedTo<F> extends ValueUntypedTo<F> {
		@Override
		<T> ValueOneWayBindWriteConfigStep<F, T, ?> to(IObservableValue<T> to);
	}

	/**
	 * Step for configuring the to-end and creating the binding.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface ValueOneWayBindWriteConfigStep<F, T, //
			THIS extends ValueOneWayBindWriteConfigStep<F, T, THIS>> //
			extends ValueWriteConfigStep<F, T, THIS>, //
			BindConfigStep<F, T, THIS> //
	{
	}
}
