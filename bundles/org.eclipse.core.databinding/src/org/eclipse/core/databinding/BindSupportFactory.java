/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.databinding;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.IDomainValidator;
import org.eclipse.core.databinding.validation.IValidator;

/**
 * A factory for creating validators and converters. This interface is not
 * intended to be implemented directly. Instead, extend the abstract
 * BindSupportFactory class.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 * 
 */
public abstract class BindSupportFactory {

	/**
	 * Creates a validator for the given from and to types and model
	 * description. Either toType or modelDescription can be null, but not both.
	 * The returned validator (if not null) should validate arbitrary values of
	 * type toType, and (in the case that toType is given) ensure that they can
	 * be converted to toType, and (in the case that modelDescription is given)
	 * ensure that they can be converted to the type expected by updatable
	 * objects created from the given model description.
	 * 
	 * @param fromType
	 *            The type to validate
	 * @param toType
	 *            The type to convert to after successful validation, or
	 *            <code>null</code> if not known
	 * @return a validator, or <code>null</code> if this factory cannot create
	 *         a validator for the given arguments.
	 */
	public IValidator createValidator(Object fromType, Object toType) {
		return null;
	}

	/**
	 * Creates a domain validator for the given model description. Either
	 * modelType or modelDescription can be null, but not both.
	 * 
	 * @param modelType
	 *            The type to validate or <code>null</code> if not known
	 * @return IDomainValidator
	 */
	public IDomainValidator createDomainValidator(Object modelType) {
		return null;
	}

	/**
	 * Creates a converter for the given from and to types and model
	 * description. Either toType or modelDescription can be null, but not both.
	 * The returned converter (if not null) should convert values of type
	 * fromType to values of type toType (in the case that toType is given), and
	 * (in the case that modelDescription is given) convert to the type expected
	 * by updatable objects created from the given model description.
	 * 
	 * @param fromType
	 *            The type to convert from
	 * @param toType
	 *            The type to convert to, or <code>null</code> if not known
	 * @return a converter, or <code>null</code> if this factory cannot create
	 *         a converter for the given arguments.
	 */
	public IConverter createConverter(Object fromType, Object toType) {
		return null;
	}

	/**
	 * @param fromType
	 * @param toType
	 * @return whether fromType is assignable to toType, or <code>null</code>
	 *         if this factory cannot determine assignability between the given
	 *         types
	 */
	public Boolean isAssignableFromTo(Object fromType, Object toType) {
		return null;
	}
}
