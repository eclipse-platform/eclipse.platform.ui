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
 *     Matthew Hall - initial API and implementation (bug 262320)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

/**
 * @since 3.3
 *
 */
public class ItemImageProperty extends WidgetImageValueProperty<Item> {
	@Override
	Image doGetImageValue(Item source) {
		return source.getImage();
	}

	@Override
	void doSetImageValue(Item source, Image value) {
		source.setImage(value);
	}

	@Override
	public String toString() {
		return "Item.image <Image>"; //$NON-NLS-1$
	}
}
