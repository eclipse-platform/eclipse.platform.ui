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
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;

/**
 * @since 3.3
 */
public class LabelImageProperty extends WidgetImageValueProperty<Label> {
	@Override
	protected Image doGetImageValue(Label source) {
		return source.getImage();
	}

	@Override
	protected void doSetImageValue(Label source, Image value) {
		source.setImage(value);
	}

	@Override
	public String toString() {
		return "Label.image <Image>"; //$NON-NLS-1$
	}
}
