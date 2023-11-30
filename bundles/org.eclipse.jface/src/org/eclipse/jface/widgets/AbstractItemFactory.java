/*******************************************************************************
* Copyright (c) 2019 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract factory for items. Factories for widgets that inherit from Item
 * should extend this factory to handle the properties of Item itself, like
 * enabled or tool tip.
 *
 * @param <F> factory
 * @param <I> item
 * @param <P> parent
 *
 * @noextend this class is not intended to be subclassed by clients.
 *
 * @since 3.18
 */
public abstract class AbstractItemFactory<F extends AbstractItemFactory<?, ?, ?>, I extends Item, P extends Widget>
		extends AbstractWidgetFactory<F, I, P> {

	AbstractItemFactory(Class<F> factoryClass, WidgetSupplier<I, P> itemCreator) {
		super(factoryClass, itemCreator);
	}

	/**
	 * Sets the receiver's image to the argument, which may be null indicating that
	 * no image should be displayed.
	 *
	 * @param image the image to display on the receiver
	 * @return this
	 *
	 * @see Item#setImage(Image)
	 */
	public F image(Image image) {
		addProperty(i -> i.setImage(image));
		return cast(this);
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * Note: If control characters like '\n', '\t' etc. are used in the string, then
	 * the behavior is platform dependent.
	 * </p>
	 *
	 * @param text the text
	 * @return this
	 *
	 * @see Item#setText(String)
	 */
	public F text(String text) {
		addProperty(i -> i.setText(text));
		return cast(this);
	}
}