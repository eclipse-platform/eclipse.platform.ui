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
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListFromStep;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListReadConfigStep;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListToStep;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListUntypedTo;
import org.eclipse.core.databinding.bind.steps.ListCommonSteps.ListWriteConfigStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * Contains {@link Step} interfaces for the fluent databinding API for two-way
 * {@link IObservableList} bindings. Each of these interfaces define the
 * operations that are allowed in a particular pipeline step.
 *
 * @since 1.11
 */
public final class ListTwoWaySteps {
	private ListTwoWaySteps() {
	}

	/**
	 * Refines the return types of the {@link ListFromStep} methods for use with
	 * two-way bindings.
	 *
	 * @noimplement
	 */
	public interface ListTwoWayFromStep extends ListFromStep {
		@Override
		<F> ListTwoWayConvertToStep<F, ?> from(IObservableList<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ListTwoWayToStep<F, T> extends ListToStep<F, T> {
		@Override
		ListTwoWayBindConfigStep<F, T, ?> to(IObservableList<T> to);
	}

	/**
	 * Step for setting the from-to direction converter.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <THIS> self type for this step
	 * @noimplement
	 */
	public interface ListTwoWayConvertToStep<F, THIS extends ListTwoWayConvertToStep<F, THIS>> extends //
			ListTwoWayToStep<F, F>, //
			ConvertToStep<F>, //
			ListTwoWayConfigStep<F, F, THIS> //
	{
		@Override
		<T> ListTwoWayConvertFromStep<F, T> convertTo(IConverter<? super F, ? extends T> converter);

		@Override
		ListTwoWayUntypedToStep<F> defaultConvert();
	}

	/**
	 * Step for settings to-end observable using default converters.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface ListTwoWayUntypedToStep<F> extends ListUntypedTo<F> {
		@Override
		<T> ListTwoWayBindConfigStep<F, T, ?> to(IObservableList<T> to);
	}

	/**
	 * Step for setting the to-from direction converter.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ListTwoWayConvertFromStep<F, T> extends ConvertFromStep<F, T> {
		@Override
		ListTwoWayToStep<F, T> convertFrom(IConverter<? super T, ? extends F> converter);
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
	public interface ListTwoWayConfigStep<F, T, THIS extends ListTwoWayConfigStep<F, T, THIS>>
			extends //
			WriteConfigStep<F, T, THIS>, //
			ListReadConfigStep<F, T, THIS>, //
			ListWriteConfigStep<F, T, THIS> //
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
	public interface ListTwoWayBindConfigStep<F, T, THIS extends ListTwoWayBindConfigStep<F, T, THIS>> extends //
			ListTwoWayConfigStep<F, T, THIS>, //
			BindConfigStep<F, T, THIS> //
	{
	}
}
