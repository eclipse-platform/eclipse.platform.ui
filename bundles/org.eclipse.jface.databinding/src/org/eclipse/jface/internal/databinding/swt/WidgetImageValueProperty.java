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
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public abstract class WidgetImageValueProperty<S extends Widget> extends WidgetValueProperty<S, Image> {
	@Override
	public Object getValueType() {
		return Image.class;
	}

	@Override
	protected Image doGetValue(S source) {
		return doGetImageValue(source);
	}

	@Override
	protected void doSetValue(S source, Image value) {
		doSetImageValue(source, value);
	}

	abstract Image doGetImageValue(S source);

	abstract void doSetImageValue(S source, Image value);
}
