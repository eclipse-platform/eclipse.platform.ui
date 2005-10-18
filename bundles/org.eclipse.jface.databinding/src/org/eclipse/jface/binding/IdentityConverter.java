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

package org.eclipse.jface.binding;

/**
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
