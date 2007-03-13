/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.3
 * 
 */
public class UpdateListStrategy extends UpdateStrategy {

	/**
	 * Policy constant denoting that the source observable's state should not be
	 * tracked and that the destination observable's state should never be
	 * updated.
	 */
	public static int POLICY_NEVER = notInlined(1);

	/**
	 * Policy constant denoting that the source observable's state should not be
	 * tracked, but that conversion and updating the destination observable's
	 * state should be performed when explicitly requested.
	 */
	public static int POLICY_ON_REQUEST = notInlined(2);

	/**
	 * Policy constant denoting that the source observable's state should be
	 * tracked, and that conversion and updating the destination observable's
	 * state should be performed automatically on every change of the source
	 * observable state.
	 */
	public static int POLICY_UPDATE = notInlined(8);

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

	protected IConverter converter;

	private int updatePolicy;

	protected boolean provideDefaults;

	/**
	 * Creates a new update list strategy for automatically updating the
	 * destination observable list whenever the source observable list changes.
	 * A default converter will be provided. The defaults can be changed by
	 * calling one of the setter methods.
	 */
	public UpdateListStrategy() {
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
	public UpdateListStrategy(int updatePolicy) {
		this(true, updatePolicy);
	}

	/**
	 * Creates a new update list strategy with a configurable update policy. A
	 * default converter will be provided if <code>provideDefaults</code> is
	 * <code>true</code>. The defaults can be changed by calling one of the
	 * setter methods.
	 * 
	 * @param provideDefaults
	 *            if <code>true</code>, default validators and a default
	 *            converter will be provided based on the observable list's
	 *            type.
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST}, or
	 *            {@link #POLICY_UPDATE}
	 */
	public UpdateListStrategy(boolean provideDefaults, int updatePolicy) {
		this.provideDefaults = provideDefaults;
		this.updatePolicy = updatePolicy;
	}

	/**
	 * @param element
	 * @return the converted element
	 */
	public Object convert(Object element) {
		return converter == null ? element : converter.convert(element);
	}

	/**
	 * 
	 * @param source
	 * @param destination
	 */
	protected void fillDefaults(IObservableList source,
			IObservableList destination) {
		Object sourceType = source.getElementType();
		Object destinationType = destination.getElementType();
		if (provideDefaults && sourceType != null && destinationType != null) {
			if (converter == null) {
				setConverter(createConverter(sourceType, destinationType));
			}
		}
		if (converter != null) {
			if (sourceType != null) {
				checkAssignable(converter.getFromType(), sourceType,
						"converter does not convert from type " + sourceType); //$NON-NLS-1$
			}
			if (destinationType != null) {
				checkAssignable(converter.getToType(), destinationType,
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
	 * @param converter
	 * @return the receiver, to enable method call chaining
	 */
	public UpdateListStrategy setConverter(IConverter converter) {
		this.converter = converter;
		return this;
	}

	/**
	 * Adds the given element at the given index to the given observable list.
	 * Clients may extend but must call the super implementation.
	 * 
	 * @param observableList
	 * @param element
	 * @param index
	 * @return a status
	 */
	protected IStatus doAdd(IObservableList observableList, Object element,
			int index) {
		try {
			observableList.add(index, element);
		} catch (Exception ex) {
			return ValidationStatus.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
					ex);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Removes the element at the given index from the given observable list.
	 * Clients may extend but must call the super implementation.
	 * 
	 * @param observableList
	 * @param index
	 * @return a status
	 */
	protected IStatus doRemove(IObservableList observableList, int index) {
		try {
			observableList.remove(index);
		} catch (Exception ex) {
			return ValidationStatus.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
					ex);
		}
		return Status.OK_STATUS;
	}
}
