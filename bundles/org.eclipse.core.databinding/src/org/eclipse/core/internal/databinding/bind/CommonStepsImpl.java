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

import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.bind.steps.CommonSteps.BindConfigStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.DirectionStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.OneWayConfigAndFromStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.TwoWayConfigAndFromStep;
import org.eclipse.core.databinding.bind.steps.ListOneWaySteps.ListOneWayConvertStep;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayConvertToStep;
import org.eclipse.core.databinding.bind.steps.SetOneWaySteps.SetOneWayConvertStep;
import org.eclipse.core.databinding.bind.steps.SetTwoWaySteps.SetTwoWayConvertToStep;
import org.eclipse.core.databinding.bind.steps.Step;
import org.eclipse.core.databinding.bind.steps.ValueOneWaySteps.ValueOneWayConvertStep;
import org.eclipse.core.databinding.bind.steps.ValueTwoWaySteps.ValueTwoWayConvertToStep;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.bind.BindingBuilder.AbstractStep;
import org.eclipse.core.internal.databinding.bind.BindingBuilder.BindDirection;
import org.eclipse.core.internal.databinding.bind.ListOneWayStepsImpl.ListOneWayConvertToStepImpl;
import org.eclipse.core.internal.databinding.bind.ListTwoWayStepsImpl.ListTwoWayConvertToStepImpl;
import org.eclipse.core.internal.databinding.bind.SetOneWayStepsImpl.SetOneWayConvertToStepImpl;
import org.eclipse.core.internal.databinding.bind.SetTwoWayStepsImpl.SetTwoWayConvertToStepImpl;
import org.eclipse.core.internal.databinding.bind.ValueOneWayStepsImpl.ValueOneWayConvertToStepImpl;
import org.eclipse.core.internal.databinding.bind.ValueTwoWayStepsImpl.ValueTwoWayConvertToStepImpl;

/**
 * Implementation of {@link Step}s that are used by both values, lists and sets.
 */
class CommonStepsImpl {
	static final class OneWayConfigAndFromStepImpl extends DirectionStepImpl<OneWayConfigAndFromStepImpl>
			implements OneWayConfigAndFromStep<OneWayConfigAndFromStepImpl> //
	{
		public OneWayConfigAndFromStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <F> ValueOneWayConvertStep<F, ?> from(IObservableValue<F> from) {
			builder.doFromStep(from, false);
			return new ValueOneWayConvertToStepImpl<>(builder);
		}

		@Override
		public <F> ListOneWayConvertStep<F, ?> from(IObservableList<F> from) {
			builder.doFromStep(from, false);
			return new ListOneWayConvertToStepImpl<>(builder);
		}

		@Override
		public <F> SetOneWayConvertStep<F, ?> from(IObservableSet<F> from) {
			builder.doFromStep(from, false);
			return new SetOneWayConvertToStepImpl<>(builder);
		}
	}

	static final class TwoWayConfigAndFromStepImpl extends DirectionStepImpl<TwoWayConfigAndFromStepImpl>
			implements TwoWayConfigAndFromStep<TwoWayConfigAndFromStepImpl> //
	{
		public TwoWayConfigAndFromStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@Override
		public <F> ValueTwoWayConvertToStep<F, ?> from(IObservableValue<F> from) {
			builder.doFromStep(from, true);
			return new ValueTwoWayConvertToStepImpl<>(builder);
		}

		@Override
		public <F> ListTwoWayConvertToStep<F, ?> from(IObservableList<F> from) {
			builder.doFromStep(from, true);
			return new ListTwoWayConvertToStepImpl<>(builder);
		}

		@Override
		public <F> SetTwoWayConvertToStep<F, ?> from(IObservableSet<F> from) {
			builder.doFromStep(from, true);
			return new SetTwoWayConvertToStepImpl<>(builder);
		}
	}

	private static abstract class DirectionStepImpl<SELF extends DirectionStepImpl<SELF>> extends AbstractStep
			implements DirectionStep<SELF> {
		public DirectionStepImpl(BindingBuilder builder) {
			super(builder);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SELF targetToModel() {
			builder.bindDirection = BindDirection.TARGET_TO_MODEL;
			return (SELF) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public SELF modelToTarget() {
			builder.bindDirection = BindDirection.MODEL_TO_TARGET;
			return (SELF) this;
		}
	}

	static abstract class ConfigStepImpl<F, T, THIS extends ConfigStepImpl<F, T, THIS>> extends AbstractStep
			implements BindConfigStep<F, T, THIS> {

		public ConfigStepImpl(BindingBuilder builder) {
			super(builder);
		}

		protected abstract <T2, M> Binding doBind(DataBindingContext context, UpdataStrategyEntry toModelEntry,
				UpdataStrategyEntry toTargetEntry);

		@SuppressWarnings("unchecked")
		protected THIS thisSelfType() {
			return (THIS) this;
		}

		@Override
		public final THIS updateOnlyOnRequest() {
			// This uses UpdateValueStrategy but works for List and Set too
			builder.toEntry.setUpdatePolicy(UpdateValueStrategy.POLICY_ON_REQUEST);
			return thisSelfType();
		}

		@Override
		public final Binding bind(DataBindingContext context) {
			Objects.requireNonNull(context);
			return builder.bindDirection == BindDirection.MODEL_TO_TARGET //
					? doBind(context, builder.fromEntry, builder.toEntry)
					: doBind(context, builder.toEntry, builder.fromEntry);
		}

		@Override
		public final Binding bindWithNewContext() {
			return bind(new DataBindingContext());
		}
	}
}
