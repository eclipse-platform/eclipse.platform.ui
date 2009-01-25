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

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.3
 * 
 */
public class CLabelImageProperty extends WidgetImageValueProperty {
	Image doGetImageValue(Object source) {
		return ((CLabel) source).getImage();
	}

	void doSetImageValue(Object source, Image value) {
		((CLabel) source).setImage(value);
	}

	public String toString() {
		return "CLabel.image <Image>"; //$NON-NLS-1$
	}
}
