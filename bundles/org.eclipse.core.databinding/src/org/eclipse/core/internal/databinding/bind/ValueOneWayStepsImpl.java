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
import org.eclipse.core.databinding.bind.steps.ValueOneWaySteps;
import org.eclipse.core.databinding.bind.steps.ValueOneWaySteps.ValueOneWayBindWriteConfigStep;
import org.eclipse.core.databinding.bind.steps.ValueOneWaySteps.ValueOneWayConvertStep;
import org.eclipse.core.databinding.bind.steps.ValueOneWaySteps.ValueOneWayToStep;
import org.eclipse.core.databinding.bind.steps.ValueOneWaySteps.ValueOneWayUntypedTo;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.bind.BindingBuilder.AbstractStep;
import org.eclipse.core.internal.databinding.bind.ValueCommonStepsImpl.ValueConfigStepImpl;

/**
 * Implementation of the {@link Step}s in {@link ValueOneWaySteps}.
 */
final class ValueOneWayStepsImpl {
	private ValueOneWayStepsImpl() {
	}

	static final class ValueOneWayConvertToStepImpl<F>
			extends ValueConfigStepImpl<F, F, ValueOneWayConvertToStepImpl<F>> //
			implements //
			ValueOneWayConvertStep<F, ValueOneWayConvertToStepImpl<F>>//
	{
		public ValueOneWayConvertToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public ValueOneWayBindWriteConfigStep<F, F, ?> to(IObservableValue<F> to) {
			builder.updateToObservable(to);
			return new ValueConfigStepImpl<>(builder);
		}

		@Override
		public <T2> ValueOneWayToStep<F, T2> convertTo(IConverter<? super F, ? extends T2> converter) {
			builder.toEntry.setConverter(converter);
			return new ValueOneWayToStepImpl<>(builder);
		}

		@Override
		public ValueOneWayUntypedTo<F> defaultConvert() {
			builder.setDefaultConvert();
			return new ValueOneWayUntypedToStepImpl<>(builder);
		}
	}

	private static final class ValueOneWayToStepImpl<F, T> extends AbstractStep implements ValueOneWayToStep<F, T> {
		public ValueOneWayToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public ValueOneWayBindWriteConfigStep<F, T, ?> to(IObservableValue<T> to) {
			builder.updateToObservable(to);
			return new ValueConfigStepImpl<>(builder);
		}
	}

	private static final class ValueOneWayUntypedToStepImpl<F> extends AbstractStep implements ValueOneWayUntypedTo<F> {
		public ValueOneWayUntypedToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <T> ValueOneWayBindWriteConfigStep<F, T, ?> to(IObservableValue<T> to) {
			builder.updateToObservable(to);
			return new ValueConfigStepImpl<>(builder);
		}
	}
}
