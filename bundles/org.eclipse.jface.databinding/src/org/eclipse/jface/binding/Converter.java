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
package org.eclipse.jface.binding;

/**
 * Abstract base class that can be used as a convenience for implementing custom
 * converters.
 * 
 */
public abstract class Converter implements IConverter {

	private Class targetType;

	private Class modelType;

	/**
	 * @param targetType
	 * @param modelType
	 */
	public Converter(Class targetType, Class modelType) {
		this.targetType = targetType;
		this.modelType = modelType;
	}

	public Class getModelType() {
		return modelType;
	}

	public Class getTargetType() {
		return targetType;
	}

}
