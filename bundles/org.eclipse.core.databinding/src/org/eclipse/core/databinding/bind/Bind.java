/*******************************************************************************
 * Copyright (c) 2022 Jens Lidestrom and others
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
package org.eclipse.core.databinding.bind;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.bind.steps.CommonSteps.OneWayConfigAndFromStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.TwoWayConfigAndFromStep;
import org.eclipse.core.internal.databinding.bind.BindingBuilder;

/**
 * This class contains static methods that are the entry points to the fluent
 * databinding API. Bindings are created using a single expression of chained
 * method calls.
 * <p>
 * This fluent API is a facade for the traditional databinding API that is based
 * on {@link DataBindingContext} and {@link UpdateValueStrategy}. It provides
 * short-hands, extra type safety and better readability, but no new
 * functionality. Everything that is possible to do with the traditional API is
 * also possible using the new API.
 * <p>
 * Example:
 *
 * <pre>
 * {@code
 * Bind.twoWay() // 1
 *     .from(WidgetProperties.text(SWT.Modify).observe(text)) // 2
 *     .validateAfterConvert(widgetValidator) // 3
 *     .convertTo(IConverter.create(i -> Objects.toString(i, ""))) // 4
 *     .convertFrom(IConverter.create(s -> s.isEmpty() ? 0 : Integer.decode(s)))
 *     .to(modelValue) // 5
 *     .validateBeforeSet(modelValidator) // 6
 *     .bind(bindingContext); // 7
 * }
 * </pre>
 *
 * <ul>
 * <li>1. First the user chooses between a two-way or one-way binding. The
 * binding direction (target-to-model or model-to-target) can also be chosen.
 *
 * <li>2. The from-end observable is given. Here the API chooses between value,
 * list of set bindings.
 *
 * <li>3. The from-end is configured. This involves setting validators and
 * binding policies (that is, convert-only or only-on-request). Only methods
 * that are relevant for two-way or one-way bindings are present for the
 * respective cases.
 *
 * <li>4. Converters are set. Here the to-end observable gets its type. The API
 * ensures that two converters are set for two-way bindings.
 *
 * <li>5. The to-end observable is set.
 *
 * <li>6. The to-end is configured, in the same was as the from-end.
 *
 * <li>7. The bind-method is called, with a DataBindingContext as argument. This
 * internally creates {@link UpdateValueStrategy} objects and calls
 * {@link DataBindingContext#bindValue}.
 * </ul>
 *
 * @noextend
 * @since 1.11
 */
public final class Bind {
	private Bind() {
	}

	/**
	 * Returns the first step in a pipeline that will create a two-way binding, for
	 * value, list or set bindings.
	 * <ul>
	 * <li>The direction of the binding is target-to-model by default.
	 * <li>Both directions of the binding will have policy
	 * {@link UpdateValueStrategy#POLICY_UPDATE} by default.
	 * </ul>
	 *
	 * @return the first binding step
	 *
	 * @see DataBindingContext#bindValue
	 * @see DataBindingContext#bindList
	 * @see DataBindingContext#bindSet
	 */
	public static TwoWayConfigAndFromStep<?> twoWay() {
		return BindingBuilder.twoWay();
	}

	/**
	 * Returns the first step in a pipeline that will create a one-way binding, for
	 * value, list or set bindings.
	 * <ul>
	 * <li>The direction of the binding is target-to-model by default.
	 * <li>The from-to direction of the binding has policy
	 * {@link UpdateValueStrategy#POLICY_UPDATE} by default.
	 * <li>The to-from direction of the binding has policy
	 * {@link UpdateValueStrategy#POLICY_NEVER}.
	 * </ul>
	 *
	 * @return the first binding step
	 *
	 * @see DataBindingContext#bindValue
	 * @see DataBindingContext#bindList
	 * @see DataBindingContext#bindSet
	 */
	public static OneWayConfigAndFromStep<?> oneWay() {
		return BindingBuilder.oneWay();
	}
}
