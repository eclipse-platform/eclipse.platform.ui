/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 213893)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;

/**
 * @since 3.3
 * 
 */
public class LabelImageProperty extends WidgetImageValueProperty {
	Image doGetImageValue(Object source) {
		return ((Label) source).getImage();
	}

	void doSetImageValue(Object source, Image value) {
		((Label) source).setImage(value);
	}

	public String toString() {
		return "Label.image <Image>"; //$NON-NLS-1$
	}
}
