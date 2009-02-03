/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public Object getValueType() {
		return Image.class;
	}

	protected Object doGetValue(Object source) {
		return doGetImageValue(source);
	}

	protected void doSetValue(Object source, Object value) {
		doSetImageValue(source, (Image) value);
	}

	abstract Image doGetImageValue(Object source);

	abstract void doSetImageValue(Object source, Image value);
}
