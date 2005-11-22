/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.converters;

import org.eclipse.jface.databinding.converter.Converter;
import org.eclipse.jface.databinding.converter.IConverter;

/**
 * A concrete implementation of {@link IConverter}.
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
public class IdentityConverter extends Converter implements IConverter {

	/**
	 * @param type
	 */
	public IdentityConverter(Class type) {
		super(type, type);
	}

	/**
	 * useful for converting between, e.g., Integer.class and int.class
	 * 
	 * @param targetType
	 * @param modelType
	 */
	public IdentityConverter(Class targetType, Class modelType) {
		super(targetType, modelType);
	}

	public Object convertTargetToModel(Object object) {
		return object;
	}

	public Object convertModelToTarget(Object object) {
		return object;
	}

}
