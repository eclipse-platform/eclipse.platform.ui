/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.api;

import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;

/**
 * @since 3.2
 */
abstract public class BindSupportFactory implements IBindSupportFactory {

	public IValidator createValidator(Object fromType, Object toType) {
		return null;
	}

	public IDomainValidator createDomainValidator(Object modelType) {
		return null;
	}

	public IConverter createConverter(Object targetType, Object modelType) {
		return null;
	}
}
