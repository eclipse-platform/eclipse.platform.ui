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

import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps;
import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps.SetTwoWayBindConfigStep;
import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps.SetTwoWayConvertFromStep;
import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps.SetTwoWayConvertToStep;
import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps.SetTwoWayToStep;
import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps.SetTwoWayUntypedToStep;
import org.eclipse.core.databinding.bind.steps.Step;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.internal.databinding.bind.BindingBuilder.AbstractStep;
import org.eclipse.core.internal.databinding.bind.SetCommonStepsImpl.SetConfigStepImpl;

/**
 * Implementation of the {@link Step}s in {@link SetTwoWaySteps}.
 */
final class SetTwoWayStepsImpl {
	private SetTwoWayStepsImpl() {
	}

	private static final class ConvertTwoWayFromStepImpl<F, T> extends AbstractStep
			implements SetTwoWayConvertFromStep<F, T> {
		public ConvertTwoWayFromStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public SetTwoWayToStep<F, T> convertFrom(IConverter<? super T, ? extends F> converter) {
			builder.fromEntry.setConverter(converter);
			return new SetTwoWayToStepImpl<>(builder);
		}
	}

	private static final class SetTwoWayToStepImpl<F, T> extends AbstractStep implements SetTwoWayToStep<F, T> {
		public SetTwoWayToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public SetTwoWayBindConfigStep<F, T, ?> to(IObservableSet<T> to) {
			builder.updateToObservable(to);
			return new SetConfigStepImpl<>(builder);
		}
	}

	static final class SetTwoWayConvertToStepImpl<F>
			extends SetConfigStepImpl<F, F, SetTwoWayConvertToStepImpl<F>>
			implements SetTwoWayConvertToStep<F, SetTwoWayConvertToStepImpl<F>>,
			SetTwoWayBindConfigStep<F, F, SetTwoWayConvertToStepImpl<F>> {

		public SetTwoWayConvertToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <T> SetTwoWayConvertFromStep<F, T> convertTo(IConverter<? super F, ? extends T> converter) {
			builder.toEntry.setConverter(converter);
			return new ConvertTwoWayFromStepImpl<>(builder);
		}

		@Override
		public SetTwoWayBindConfigStep<F, F, ?> to(IObservableSet<F> to) {
			builder.updateToObservable(to);
			return this;
		}

		@Override
		public SetTwoWayUntypedToStep<F> defaultConvert() {
			builder.setDefaultConvert();
			return new SetTwoWayUntypedToStepImpl<>(builder);
		}
	}

	static final class SetTwoWayUntypedToStepImpl<F> extends AbstractStep implements SetTwoWayUntypedToStep<F> {
		public SetTwoWayUntypedToStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <T> SetTwoWayBindConfigStep<F, T, ?> to(IObservableSet<T> to) {
			builder.updateToObservable(to);
			return new SetConfigStepImpl<>(builder);
		}
	}
}
