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
import org.eclipse.core.databinding.bind.steps.CommonSteps.ConvertFromStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.ConvertToStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.WriteConfigStep;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetFromStep;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetReadConfigStep;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetToStep;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetUntypedTo;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetWriteConfigStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * Contains {@link Step} interfaces for the fluent databinding API for two-way
 * {@link IObservableSet} bindings. Each of these interfaces define the
 * operations that are allowed in a particular pipeline step.
 *
 * @since 1.11
 */
public final class SetTwoWaySteps {
	private SetTwoWaySteps() {
	}

	/**
	 * Refines the return types of the {@link SetFromStep} methods for use with
	 * two-way bindings.
	 *
	 * @noimplement
	 */
	public interface SetTwoWayFromStep extends SetFromStep {
		@Override
		<F> SetTwoWayConvertToStep<F, ?> from(IObservableSet<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface SetTwoWayToStep<F, T> extends SetToStep<F, T> {
		@Override
		SetTwoWayBindConfigStep<F, T, ?> to(IObservableSet<T> to);
	}

	/**
	 * Step for setting the from-to direction converter.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <THIS> self type for this step
	 * @noimplement
	 */
	public interface SetTwoWayConvertToStep<F, THIS extends SetTwoWayConvertToStep<F, THIS>> extends //
			SetTwoWayToStep<F, F>, //
			ConvertToStep<F>, //
			SetTwoWayConfigStep<F, F, THIS> //
	{
		@Override
		<T> SetTwoWayConvertFromStep<F, T> convertTo(IConverter<? super F, ? extends T> converter);

		@Override
		SetTwoWayUntypedToStep<F> defaultConvert();
	}

	/**
	 * Step for settings to-end observable using default converters.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface SetTwoWayUntypedToStep<F> extends SetUntypedTo<F> {
		@Override
		<T> SetTwoWayBindConfigStep<F, T, ?> to(IObservableSet<T> to);
	}

	/**
	 * Step for setting the to-from direction converter.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface SetTwoWayConvertFromStep<F, T> extends ConvertFromStep<F, T> {
		@Override
		SetTwoWayToStep<F, T> convertFrom(IConverter<? super T, ? extends F> converter);
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
	public interface SetTwoWayConfigStep<F, T, THIS extends SetTwoWayConfigStep<F, T, THIS>>
			extends //
			WriteConfigStep<F, T, THIS>, //
			SetReadConfigStep<F, T, THIS>, //
			SetWriteConfigStep<F, T, THIS> //
	{
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
	public interface SetTwoWayBindConfigStep<F, T, THIS extends SetTwoWayBindConfigStep<F, T, THIS>> extends //
			SetTwoWayConfigStep<F, T, THIS>, //
			BindConfigStep<F, T, THIS> //
	{
	}
}
