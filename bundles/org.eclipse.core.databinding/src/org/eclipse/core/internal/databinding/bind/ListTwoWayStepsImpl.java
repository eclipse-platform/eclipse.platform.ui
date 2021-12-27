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
package org.eclipse.core.internal.databinding.bind;

import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayBindConfigStep;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayConvertFromStep;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayConvertToStep;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayToStep;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayUntypedToStep;
import org.eclipse.core.databinding.bind.steps.Step;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.internal.databinding.bind.BindingBuilder.AbstractStep;
import org.eclipse.core.internal.databinding.bind.ListCommonStepsImpl.ListConfigStepImpl;

/**
 * Implementation of the {@link Step}s in {@link ListTwoWaySteps}.
 */
final class ListTwoWayStepsImpl {
	private ListTwoWayStepsImpl() {
	}

	private static final class ConvertTwoWayFromStepImpl<F, T> extends AbstractStep
			implements ListTwoWayConvertFromStep<F, T> {
		public ConvertTwoWayFromStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public ListTwoWayToStep<F, T> convertFrom(IConverter<? super T, ? extends F> converter) {
			builder.fromEntry.setConverter(converter);
			return new ListTwoWayToStepImpl<>(builder);
		}
	}

	private static final class ListTwoWayToStepImpl<F, T> extends AbstractStep implements ListTwoWayToStep<F, T> {
		public ListTwoWayToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public ListTwoWayBindConfigStep<F, T, ?> to(IObservableList<T> to) {
			builder.updateToObservable(to);
			return new ListConfigStepImpl<>(builder);
		}
	}

	static final class ListTwoWayConvertToStepImpl<F>
			extends ListConfigStepImpl<F, F, ListTwoWayConvertToStepImpl<F>>
			implements ListTwoWayConvertToStep<F, ListTwoWayConvertToStepImpl<F>>,
			ListTwoWayBindConfigStep<F, F, ListTwoWayConvertToStepImpl<F>> {

		public ListTwoWayConvertToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <T> ListTwoWayConvertFromStep<F, T> convertTo(IConverter<? super F, ? extends T> converter) {
			builder.toEntry.setConverter(converter);
			return new ConvertTwoWayFromStepImpl<>(builder);
		}

		@Override
		public ListTwoWayBindConfigStep<F, F, ?> to(IObservableList<F> to) {
			builder.updateToObservable(to);
			return this;
		}

		@Override
		public ListTwoWayUntypedToStep<F> defaultConvert() {
			builder.setDefaultConvert();
			return new ListTwoWayUntypedToStepImpl<>(builder);
		}
	}

	static final class ListTwoWayUntypedToStepImpl<F> extends AbstractStep implements ListTwoWayUntypedToStep<F> {
		public ListTwoWayUntypedToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <T> ListTwoWayBindConfigStep<F, T, ?> to(IObservableList<T> to) {
			builder.updateToObservable(to);
			return new ListConfigStepImpl<>(builder);
		}
	}
}
