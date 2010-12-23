/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 306611)
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * Simple value property which applies a given converter on a source object in
 * order to produce the property's value.
 */
public class ConverterValueProperty extends SimpleValueProperty {

	private final IConverter converter;

	/**
	 * Creates a new value property which applies the given converter on the
	 * source object in order to produce the property's value.
	 * 
	 * @param converter
	 *            The converter to apply to the source object.
	 */
	public ConverterValueProperty(IConverter converter) {
		this.converter = converter;
	}

	public Object getValueType() {
		// the property type is the converter's target type
		return converter.getToType();
	}

	public Object getValue(Object source) {
		// We do also pass null values to the converter.
		return doGetValue(source);
	}

	protected Object doGetValue(Object source) {
		// delegate to the IConverter
		return converter.convert(source);
	}

	protected void doSetValue(Object source, Object value) {
		// setting a value is not supported
		throw new UnsupportedOperationException(toString()
				+ ": Setter not supported on a converted value!"); //$NON-NLS-1$
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		// no listener API
		return null;
	}

	public String toString() {
		return "IConverter#convert(source) <IConverter#getToType()>"; //$NON-NLS-1$
	}
}
