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
public abstract class BindSupportFactory implements IBindSupportFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createValidator(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public IValidator createValidator(Object fromType, Object toType,
			Object modelDescription) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createDomainValidator(java.lang.Object, java.lang.Object)
	 */
	public IDomainValidator createDomainValidator(Object modelType,
			Object modelDescription) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createConverter(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public IConverter createConverter(Object targetType, Object modelType,
			Object modelDescription) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createModelToTargetConverter(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public IConverter createModelToTargetConverter(Object fromType,
			Object toType, Object modelDescription) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createTargetToModelConverter(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public IConverter createTargetToModelConverter(Object fromType,
			Object toType, Object modelDescription) {
		return null;
	}

}
