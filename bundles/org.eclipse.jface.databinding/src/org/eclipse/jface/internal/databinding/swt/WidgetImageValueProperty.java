/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 213893)
 *     Matthew Hall - bug 263413
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.3
 *
 */
public abstract class WidgetImageValueProperty extends WidgetValueProperty {
	@Override
	public Object getValueType() {
		return Image.class;
	}

	@Override
	protected Object doGetValue(Object source) {
		return doGetImageValue(source);
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		doSetImageValue(source, (Image) value);
	}

	abstract Image doGetImageValue(Object source);

	abstract void doSetImageValue(Object source, Image value);
}
