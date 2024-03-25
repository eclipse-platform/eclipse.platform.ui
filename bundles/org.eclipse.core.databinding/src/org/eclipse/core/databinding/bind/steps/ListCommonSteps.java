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
import org.eclipse.core.databinding.bind.steps.CommonSteps.ReadConfigStep;
import org.eclipse.core.databinding.bind.steps.CommonSteps.WriteConfigStep;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * Contains {@link Step} interfaces for the fluent databinding API common for
 * one-way and two-way {@link IObservableList} bindings. Each of these
 * interfaces define the operations that are allowed in a particular pipeline
 * step.
 *
 * @since 1.11
 */
public final class ListCommonSteps {
	private ListCommonSteps() {
	}

	/**
	 * Step for setting the from-end observable.
	 *
	 * @noimplement
	 */
	public interface ListFromStep extends Step {
		/**
		 * Sets the from-end observable of the resulting binding. All steps after this
		 * one but before a call to {@link ListToStep#to to} will configure the
		 * from-end of the binding.
		 *
		 * @see DataBindingContext#bindList
		 * @return next step
		 */
		<F> Step from(IObservableList<F> from);
	}

	/**
	 * Step for setting the to-end observable.
	 *
	 * @param <F> type of the from-end observable
	 * @param <T> type of the to-end observable
	 * @noimplement
	 */
	public interface ListToStep<F, T> extends Step {
		/**
		 * Sets the from-end observable of the resulting binding. All steps after this
		 * one will configure that from-end of the binding.
		 *
		 * @see DataBindingContext#bindList
		 * @return next step
		 */
		WriteConfigStep<F, T, ?> to(IObservableList<T> to);
	}

	/**
	 * Step for converting between from- and to-types.
	 *
	 * @param <F> type of the from-end observable
	 * @noimplement
	 */
	public interface ListUntypedTo<F> extends Step {
		/**
		 * @return TODO
		 */
		<T> WriteConfigStep<F, T, ?> to(IObservableList<T> to);
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
	public interface ListWriteConfigStep<F, T, THIS extends ListWriteConfigStep<F, T, THIS>>
			extends WriteConfigStep<F, T, THIS> {
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
	public interface ListReadConfigStep<F, T, THIS extends ListReadConfigStep<F, T, THIS>>
			extends ReadConfigStep<F, T, THIS> {
	}
}
