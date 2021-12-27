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
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetFromStep;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetReadConfigStep;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetToStep;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetUntypedTo;
import org.eclipse.core.databinding.bind.steps.SetCommonSteps.SetWriteConfigStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * Contains {@link Step} interfaces for the fluent databinding API for one-way
 * {@link IObservableSet} bindings. Each of these interfaces define the
 * operations that are allowed in a particular pipeline step.
 *
 * @since 1.11
 */
public final class SetOneWaySteps {
	private SetOneWaySteps() {
	}

	/**
	 * Refines the return types of the {@link SetFromStep} methods for use with
	 * one-way bindings.
	 *
	 * @noimplement
	 */
	public interface SetOneWayFromStep extends SetFromStep {
		@Override
		<F> SetOneWayConvertStep<F, ?> from(IObservableSet<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface SetOneWayToStep<F, T> extends SetToStep<F, T> {
		@Override
		SetOneWayBindWriteConfigStep<F, T, ?> to(IObservableSet<T> to);
	}

	/**
	 * Step for converting between from- and to-types.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <THIS> self type for this step
	 * @noimplement
	 */
	public interface SetOneWayConvertStep<F, THIS extends SetOneWayConvertStep<F, THIS>> extends //
			SetOneWayToStep<F, F>, //
			SetToStep<F, F>, //
			ConvertToStep<F>, //
			SetReadConfigStep<F, F, THIS> //
	{
		@Override
		SetOneWayUntypedTo<F> defaultConvert();

		@Override
		<T2> SetOneWayToStep<F, T2> convertTo(IConverter<? super F, ? extends T2> converter);
	}

	/**
	 * Step for settings to-end observable using default converters.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface SetOneWayUntypedTo<F> extends SetUntypedTo<F> {
		@Override
		<T> SetOneWayBindWriteConfigStep<F, T, ?> to(IObservableSet<T> to);
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
	public interface SetOneWayBindWriteConfigStep<F, T, //
			THIS extends SetOneWayBindWriteConfigStep<F, T, THIS>> //
			extends SetWriteConfigStep<F, T, THIS>, //
			BindConfigStep<F, T, THIS> //
	{
	}
}
