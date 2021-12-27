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

import org.eclipse.core.databinding.bind.steps.Step;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps.ValueTwoWayBindConfigStep;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps.ValueTwoWayConvertFromStep;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps.ValueTwoWayConvertToStep;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps.ValueTwoWayToStep;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps.ValueTwoWayUntypedToStep;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.bind.BindingBuilder.AbstractStep;
import org.eclipse.core.internal.databinding.bind.ValueCommonStepsImpl.ValueConfigStepImpl;

/**
 * Implementation of the {@link Step}s in {@link ValueTwoWaySteps}.
 */
final class ValueTwoWayStepsImpl {
	private ValueTwoWayStepsImpl() {
	}

	private static final class ConvertTwoWayFromStepImpl<F, T> extends AbstractStep
			implements ValueTwoWayConvertFromStep<F, T> {
		public ConvertTwoWayFromStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public ValueTwoWayToStep<F, T> convertFrom(IConverter<? super T, ? extends F> converter) {
			builder.fromEntry.setConverter(converter);
			return new ValueTwoWayToStepImpl<>(builder);
		}
	}

	private static final class ValueTwoWayToStepImpl<F, T> extends AbstractStep implements ValueTwoWayToStep<F, T> {
		public ValueTwoWayToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public ValueTwoWayBindConfigStep<F, T, ?> to(IObservableValue<T> to) {
			builder.updateToObservable(to);
			return new ValueConfigStepImpl<>(builder);
		}
	}

	static final class ValueTwoWayConvertToStepImpl<F>
			extends ValueConfigStepImpl<F, F, ValueTwoWayConvertToStepImpl<F>>
			implements ValueTwoWayConvertToStep<F, ValueTwoWayConvertToStepImpl<F>>,
			ValueTwoWayBindConfigStep<F, F, ValueTwoWayConvertToStepImpl<F>> {

		public ValueTwoWayConvertToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <T> ValueTwoWayConvertFromStep<F, T> convertTo(IConverter<? super F, ? extends T> converter) {
			builder.toEntry.setConverter(converter);
			return new ConvertTwoWayFromStepImpl<>(builder);
		}

		@Override
		public ValueTwoWayBindConfigStep<F, F, ?> to(IObservableValue<F> to) {
			builder.updateToObservable(to);
			return this;
		}

		@Override
		public ValueTwoWayUntypedToStep<F> defaultConvert() {
			builder.setDefaultConvert();
			return new ValueTwoWayUntypedToStepImpl<>(builder);
		}
	}

	static final class ValueTwoWayUntypedToStepImpl<F> extends AbstractStep implements ValueTwoWayUntypedToStep<F> {
		public ValueTwoWayUntypedToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <T> ValueTwoWayBindConfigStep<F, T, ?> to(IObservableValue<T> to) {
			builder.updateToObservable(to);
			return new ValueConfigStepImpl<>(builder);
		}
	}
}
