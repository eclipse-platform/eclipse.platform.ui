/*
 * Copyright (C) 2005, 2015 db4objects Inc.  http://www.db4o.com
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.core.internal.databinding.conversion;

/**
 * StringToBooleanConverter.
 */
public class StringToBooleanConverter extends StringToBooleanPrimitiveConverter {

	@Override
	public Boolean convert(String source) {
		if ("".equals(source.trim())) { //$NON-NLS-1$
			return null;
		}
		return super.convert(source);
	}

	@Override
	public Object getToType() {
		return Boolean.class;
	}

}
