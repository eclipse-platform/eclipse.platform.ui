/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263709)
 *     Matthew Hall - bug 264954
 ******************************************************************************/

package org.eclipse.core.databinding;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.BindingModelProperty;
import org.eclipse.core.internal.databinding.BindingTargetProperty;
import org.eclipse.core.internal.databinding.DataBindingContextBindingsProperty;
import org.eclipse.core.internal.databinding.DataBindingContextValidationStatusProvidersProperty;
import org.eclipse.core.internal.databinding.ValidationStatusProviderModelsProperty;
import org.eclipse.core.internal.databinding.ValidationStatusProviderTargetsProperty;
import org.eclipse.core.internal.databinding.ValidationStatusProviderValidationStatusProperty;
import org.eclipse.core.runtime.IStatus;

/**
 * A factory for creating properties for core types in the DataBinding framework
 * e.g. {@link DataBindingContext} or ValidationStatusProvider.
 * 
 * @since 1.2
 */
public class BindingProperties {
	/**
	 * Returns an {@link IListProperty} &lt; {@link Binding} &gt; for observing
	 * the bindings of a {@link DataBindingContext}.
	 * 
	 * @return an {@link IListProperty} &lt; {@link Binding} &gt; for observing
	 *         the bindings of a {@link DataBindingContext}.
	 */
	public static IListProperty bindings() {
		return new DataBindingContextBindingsProperty();
	}

	/**
	 * Returns an {@link IValueProperty} &lt; {@link IObservable} &gt; for
	 * observing the model of a {@link Binding}.
	 * 
	 * @return an {@link IValueProperty} &lt; {@link IObservable} &gt; for
	 *         observing the model of a {@link Binding}.
	 */
	public static IValueProperty model() {
		return new BindingModelProperty();
	}

	/**
	 * Returns an {@link IListProperty} &lt; {@link IObservable} &gt; for
	 * observing the models of a {@link ValidationStatusProvider}.
	 * 
	 * @return an {@link IListProperty} &lt; {@link IObservable} &gt; for
	 *         observing the models of a {@link ValidationStatusProvider}.
	 */
	public static IListProperty models() {
		return new ValidationStatusProviderModelsProperty();
	}

	/**
	 * Returns an {@link IValueProperty} &lt; {@link IObservable} &gt; for
	 * observing the target of a {@link Binding}.
	 * 
	 * @return an {@link IValueProperty} &lt; {@link IObservable} &gt; for
	 *         observing the target of a {@link Binding}.
	 */
	public static IValueProperty target() {
		return new BindingTargetProperty();
	}

	/**
	 * Returns an {@link IListProperty} &lt; {@link IObservable} &gt; for
	 * observing the targets of a {@link ValidationStatusProvider}.
	 * 
	 * @return an {@link IListProperty} &lt; {@link IObservable} &gt; for
	 *         observing the targets of a {@link ValidationStatusProvider}.
	 */
	public static IListProperty targets() {
		return new ValidationStatusProviderTargetsProperty();
	}

	/**
	 * Returns an {@link IValueProperty} &lt; {@link IStatus} &gt; for observing
	 * the validation status of a {@link ValidationStatusProvider}.
	 * 
	 * @return an {@link IValueProperty} &lt; {@link IStatus} &gt; for observing
	 *         the validation status of a {@link ValidationStatusProvider}.
	 */
	public static IValueProperty validationStatus() {
		return new ValidationStatusProviderValidationStatusProperty()
				.value(Properties.observableValue(IStatus.class));
	}

	/**
	 * Returns an {@link IListProperty} &lt; {@link ValidationStatusProvider}
	 * &gt; for observing the validation status providers of a
	 * {@link DataBindingContext}.
	 * 
	 * @return an {@link IListProperty} &lt; {@link ValidationStatusProvider}
	 *         &gt; for observing the validation status providers of a
	 *         {@link DataBindingContext}.
	 */
	public static IListProperty validationStatusProviders() {
		return new DataBindingContextValidationStatusProvidersProperty();
	}
}
