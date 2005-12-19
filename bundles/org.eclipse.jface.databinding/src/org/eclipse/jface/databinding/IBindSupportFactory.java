/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;

/**
 * A factory for creating validators and converters.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface IBindSupportFactory {

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
	 * @param modelDescription
	 *            The model description object passed to
	 *            DataBindingContext#bind, or <code>null</code> if not known
	 * @return a validator, or <code>null</code> if this factory cannot create
	 *         a validator for the given arguments.
	 */
	IValidator createValidator(Class fromType, Class toType,
			Object modelDescription);

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
	 * @param modelDescription
	 *            The model description object passed to
	 *            DataBindingContext#bind, or <code>null</code> if not known
	 * @return a converter, or <code>null</code> if this factory cannot create
	 *         a converter for the given arguments.
	 */
	IConverter createConverter(Class fromType, Class toType,
			Object modelDescription);
}
