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
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListFromStep;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListReadConfigStep;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListToStep;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListUntypedTo;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListWriteConfigStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * Contains {@link Step} interfaces for the fluent databinding API for one-way
 * {@link IObservableList} bindings. Each of these interfaces define the
 * operations that are allowed in a particular pipeline step.
 *
 * @since 1.11
 */
public final class ListOneWaySteps {
	private ListOneWaySteps() {
	}

	/**
	 * Refines the return types of the {@link ListFromStep} methods for use with
	 * one-way bindings.
	 *
	 * @noimplement
	 */
	public interface ListOneWayFromStep extends ListFromStep {
		@Override
		<F> ListOneWayConvertStep<F, ?> from(IObservableList<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ListOneWayToStep<F, T> extends ListToStep<F, T> {
		@Override
		ListOneWayBindWriteConfigStep<F, T, ?> to(IObservableList<T> to);
	}

	/**
	 * Step for converting between from- and to-types.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <THIS> self type for this step
	 * @noimplement
	 */
	public interface ListOneWayConvertStep<F, THIS extends ListOneWayConvertStep<F, THIS>> extends //
			ListOneWayToStep<F, F>, //
			ListToStep<F, F>, //
			ConvertToStep<F>, //
			ListReadConfigStep<F, F, THIS> //
	{
		@Override
		ListOneWayUntypedTo<F> defaultConvert();

		@Override
		<T2> ListOneWayToStep<F, T2> convertTo(IConverter<? super F, ? extends T2> converter);
	}

	/**
	 * Step for settings to-end observable using default converters.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface ListOneWayUntypedTo<F> extends ListUntypedTo<F> {
		@Override
		<T> ListOneWayBindWriteConfigStep<F, T, ?> to(IObservableList<T> to);
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
	public interface ListOneWayBindWriteConfigStep<F, T, //
			THIS extends ListOneWayBindWriteConfigStep<F, T, THIS>> //
			extends ListWriteConfigStep<F, T, THIS>, //
			BindConfigStep<F, T, THIS> //
	{
	}
}
