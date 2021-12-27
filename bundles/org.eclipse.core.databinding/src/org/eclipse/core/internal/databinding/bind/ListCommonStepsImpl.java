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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.bind.steps.ListOneWaySteps.ListOneWayBindWriteConfigStep;
import org.eclipse.core.databinding.bind.steps.ListTwoWaySteps.ListTwoWayBindConfigStep;
import org.eclipse.core.databinding.bind.steps.Step;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.internal.databinding.bind.CommonStepsImpl.ConfigStepImpl;

/**
 * Implementation of the {@link Step}s common to both one-way and two-way
 * bindings.
 */
class ListCommonStepsImpl {
	private ListCommonStepsImpl() {
	}

	static class ListConfigStepImpl<F, T, THIS extends ListConfigStepImpl<F, T, THIS>> //
			extends ConfigStepImpl<F, T, THIS> //
			implements //
			ListOneWayBindWriteConfigStep<F, T, THIS>, //
			ListTwoWayBindConfigStep<F, T, THIS> //
	{
		public ListConfigStepImpl(BindingBuilder builder) {
			super(builder);
		}
		@Override
		protected <T2, M2> Binding doBind(DataBindingContext context, UpdataStrategyEntry toModelEntry,
				UpdataStrategyEntry toTargetEntry) {

			UpdateListStrategy<T2, M2> targetToModel = toModelEntry.createUpdateListStrategy();
			UpdateListStrategy<M2, T2> modelToTarget = toTargetEntry.createUpdateListStrategy();

			@SuppressWarnings("unchecked")
			IObservableList<M2> model = (IObservableList<M2>) toModelEntry.getObservable();
			@SuppressWarnings("unchecked")
			IObservableList<T2> target = (IObservableList<T2>) toTargetEntry.getObservable();

			return context.bindList(target, model, targetToModel, modelToTarget);
		}
	}
}
