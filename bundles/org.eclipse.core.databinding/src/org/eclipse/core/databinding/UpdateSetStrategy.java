/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 124684)
 *     IBM Corporation - through UpdateListStrategy.java
 *     Matthew Hall - bug 270461
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Objects;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Customizes a {@link Binding} between two {@link IObservableSet observable
 * sets}. The following behaviors can be customized via the strategy:
 * <ul>
 * <li>Conversion</li>
 * <li>Automatic processing</li>
 * </ul>
 * <p>
 * Conversion:<br>
 * When elements are added they can be {@link #convert(Object) converted} to the
 * destination element type.
 * </p>
 * <p>
 * Automatic processing:<br>
 * The processing to perform when the source observable changes. This behavior
 * is configured via policies provided on construction of the strategy (e.g.
 * {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST}, {@link #POLICY_UPDATE}).
 * </p>
 *
 * @param <S> the type of the elements on the source side (i.e. the model side
 *            if this is a model-to-target update and the target side if this is
 *            a target-to-model update)
 * @param <D> the type of the elements on the destination side (i.e. the target
 *            side if this is a model-to-target update and the model side if
 *            this is a target-to-model update)
 * @see DataBindingContext#bindSet(IObservableSet, IObservableSet,
 *      UpdateSetStrategy, UpdateSetStrategy)
 * @see IConverter
 * @since 1.1
 */
public class UpdateSetStrategy<S, D> extends UpdateStrategy<S, D> {

	/**
	 * Policy constant denoting that the source observable's state should not be
	 * tracked and that the destination observable's state should never be
	 * updated.
	 */
	public static final int POLICY_NEVER = notInlined(1);

	/**
	 * Policy constant denoting that the source observable's state should not be
	 * tracked, but that conversion and updating the destination observable's
	 * state should be performed when explicitly requested.
	 */
	public static final int POLICY_ON_REQUEST = notInlined(2);

	/**
	 * Policy constant denoting that the source observable's state should be
	 * tracked, and that conversion and updating the destination observable's
	 * state should be performed automatically on every change of the source
	 * observable state.
	 */
	public static final int POLICY_UPDATE = notInlined(8);

	/**
	 * Helper method allowing API evolution of the above constant values. The
	 * compiler will not inline constant values into client code if values are
	 * "computed" using this helper.
	 *
	 * @param i
	 *            an integer
	 * @return the same integer
	 */
	private static int notInlined(int i) {
		return i;
	}

	private int updatePolicy;

	protected boolean provideDefaults;

	/**
	 * Creates a new update list strategy for automatically updating the
	 * destination observable list whenever the source observable list changes.
	 * A default converter will be provided. The defaults can be changed by
	 * calling one of the setter methods.
	 */
	public UpdateSetStrategy() {
		this(true, POLICY_UPDATE);
	}

	/**
	 * Creates a new update list strategy with a configurable update policy. A
	 * default converter will be provided. The defaults can be changed by
	 * calling one of the setter methods.
	 *
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST}, or
	 *            {@link #POLICY_UPDATE}
	 */
	public UpdateSetStrategy(int updatePolicy) {
		this(true, updatePolicy);
	}

	/**
	 * Creates a new update list strategy with a configurable update policy. A
	 * default converter will be provided if <code>provideDefaults</code> is
	 * <code>true</code>, see {@link DataBindingContext}. The defaults can be
	 * changed by calling one of the setter methods.
	 *
	 * @param provideDefaults
	 *            if <code>true</code>, default validators and a default
	 *            converter will be provided based on the observable list's
	 *            type, see {@link DataBindingContext}
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST}, or
	 *            {@link #POLICY_UPDATE}
	 */
	public UpdateSetStrategy(boolean provideDefaults, int updatePolicy) {
		this.provideDefaults = provideDefaults;
		this.updatePolicy = updatePolicy;
	}

	/**
	 *
	 * @param source      source observable, to be used for its type
	 * @param destination destination observable, to be used for its type
	 */
	@SuppressWarnings("unchecked")
	protected void fillDefaults(IObservableSet<? extends S> source, IObservableSet<? super D> destination) {
		Object sourceType = source.getElementType();
		Object destinationType = destination.getElementType();
		if (provideDefaults && sourceType != null && destinationType != null) {
			if (converter == null) {
				setConverter((IConverter<S, D>) createConverter(sourceType, destinationType));
			}
		}
		if (converter != null) {
			if (sourceType != null) {
				checkAssignable(converter.getFromType(), sourceType,
						"converter does not convert from type " + sourceType); //$NON-NLS-1$
			}
			if (destinationType != null) {
				checkAssignable(destinationType, converter.getToType(),
						"converter does not convert to type " + destinationType); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @return the update policy
	 */
	public int getUpdatePolicy() {
		return updatePolicy;
	}

	/**
	 * Sets the converter to be invoked when converting added elements from the
	 * source element type to the destination element type.
	 * <p>
	 * If the converter throws any exceptions they are reported as validation
	 * errors, using the exception message.
	 *
	 * @param converter the new converter
	 * @return the receiver, to enable method call chaining
	 */
	public UpdateSetStrategy<S, D> setConverter(IConverter<S, D> converter) {
		this.converter = converter;
		return this;
	}

	/**
	 * Adds the given element at the given index to the given observable list.
	 * Clients may extend but must call the super implementation.
	 *
	 * @param observableSet the set to add to
	 * @param element       element to add
	 * @return a status
	 */
	protected IStatus doAdd(IObservableSet<? super D> observableSet, D element) {
		try {
			observableSet.add(element);
		} catch (Exception ex) {
			return logErrorWhileSettingValue(ex);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Removes the element at the given index from the given observable list.
	 * Clients may extend but must call the super implementation.
	 *
	 * @param observableSet the set to remove from
	 * @param element       the element to remove
	 * @return a status
	 */
	protected IStatus doRemove(IObservableSet<? super D> observableSet, D element) {
		try {
			observableSet.remove(element);
		} catch (Exception ex) {
			return logErrorWhileSettingValue(ex);
		}
		return Status.OK_STATUS;
	}


	/**
	 * Convenience method that creates an {@link UpdateValueStrategy} with the given
	 * converter. It uses {@link #POLICY_UPDATE}.
	 *
	 * @param converter the converter
	 * @return the update strategy
	 * @since 1.8
	 */
	public static <S, D> UpdateSetStrategy<S, D> create(IConverter<S, D> converter) {
		Objects.requireNonNull(converter);
		return new UpdateSetStrategy<S, D>().setConverter(converter);
	}

	/**
	 * Convenience method that creates an update strategy that never updates its
	 * observables, using {@link #POLICY_NEVER} and no defaults.
	 *
	 * @return the update strategy
	 * @since 1.8
	 */
	public static <S, D> UpdateSetStrategy<S, D> never() {
		return new UpdateSetStrategy<>(false, POLICY_NEVER);
	}
}
