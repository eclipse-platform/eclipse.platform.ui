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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.bind.steps.CommonSteps.ReadConfigStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.WriteConfigStep;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;

/**
 * Contains {@link Step} interfaces for the fluent databinding API common for
 * one-way and two-way {@link IObservableValue} bindings. Each of these
 * interfaces define the operations that are allowed in a particular pipeline
 * step.
 *
 * @since 1.11
 */
public final class ValueCommonSteps {
	private ValueCommonSteps() {
	}

	/**
	 * Step for setting the from-end observable.
	 *
	 * @noimplement
	 */
	public interface ValueFromStep extends Step {
		/**
		 * Sets the from-end observable of the resulting binding. All steps after this
		 * one but before a call to {@link ValueToStep#to to} will configure the
		 * from-end of the binding.
		 *
		 * @see DataBindingContext#bindValue
		 * @return next step
		 */
		<F> Step from(IObservableValue<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ValueToStep<F, T> extends Step {
		/**
		 * Sets the from-end observable of the resulting binding. All steps after this
		 * one will configure that from-end of the binding.
		 *
		 * @see DataBindingContext#bindValue
		 * @return next step
		 */
		WriteConfigStep<F, T, ?> to(IObservableValue<T> to);
	}

	/**
	 * Step for converting between from- and to-types.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface ValueUntypedTo<F> extends Step {
		/**
		 * @return TODO
		 */
		<T> WriteConfigStep<F, T, ?> to(IObservableValue<T> to);
	}

	/**
	 * Step for configuring the end of a binding where data is written. This is the
	 * to-end for a one-way binding and both ends for a two-way binding. Some kinds
	 * of observables allow configuration of validators and special update policies.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface ValueWriteConfigStep<F, T, THIS extends ValueWriteConfigStep<F, T, THIS>>
			extends WriteConfigStep<F, T, THIS> {
		/**
		 * Sets the {@link UpdateValueStrategy#setAfterConvertValidator after-convert}
		 * validator on the to-from direction on the resulting binding.
		 *
		 * @see UpdateValueStrategy#setAfterConvertValidator
		 * @return next step
		 */
		THIS validateAfterConvert(IValidator<? super T> validator);

		/**
		 * Sets the {@link UpdateValueStrategy#setBeforeSetValidator before-set}
		 * validator for the to-from direction on the resulting binding.
		 *
		 * @see UpdateValueStrategy#setBeforeSetValidator
		 * @return next step
		 */
		THIS validateBeforeSet(IValidator<? super T> validator);

		/**
		 * Sets the {@link UpdateValueStrategy#POLICY_CONVERT convert-only} update
		 * policy for the active observable. This configures the resulting binding to
		 * only do validation and conversion, and not writing data to the observable.
		 * This is useful for generating a validation status.
		 *
		 * @see UpdateValueStrategy#POLICY_CONVERT
		 * @return next step
		 */
		THIS convertOnly();
	}

	/**
	 * A config step for the end of a binding where data is read. This is the
	 * from-end for a one-way binding and both ends for a two-way binding.
	 *
	 * @param <F>    type of the from-end observable
	 * @param <T>    type of the to-end observable
	 * @param <THIS> self type for this step
	 *
	 * @noimplement
	 */
	public interface ValueReadConfigStep<F, T, THIS extends ValueReadConfigStep<F, T, THIS>>
			extends ReadConfigStep<F, T, THIS> {
		/**
		 * Sets the {@link UpdateValueStrategy#setAfterGetValidator after-get} validator
		 * to use for the active observable.
		 * <p>
		 * Note: The validator is used when the active observable is <em>written
		 * to</em>, while the other validators are used when the active observable is
		 * <em>read from</em>.
		 *
		 * @see UpdateValueStrategy#setAfterGetValidator
		 * @param validator the validator to set
		 * @return next step
		 */
		THIS validateAfterGet(IValidator<? super F> validator);
	}
}
