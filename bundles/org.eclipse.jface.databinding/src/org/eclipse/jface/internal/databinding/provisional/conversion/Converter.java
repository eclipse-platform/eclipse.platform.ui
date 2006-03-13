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

package org.eclipse.jface.internal.databinding.provisional.conversion;

/**
 * @since 1.0
 *
 */
public abstract class Converter implements IConverter {

	private Class fromType;
	private Class toType;

	/**
	 * @param fromType
	 * @param toType
	 */
	public Converter(Class fromType, Class toType) {
		this.fromType = fromType;
		this.toType = toType;
	}

	public Object getFromType() {
		return fromType;
	}

	public Object getToType() {
		return toType;
	}

}
