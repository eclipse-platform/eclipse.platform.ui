/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
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

import org.eclipse.swt.widgets.Widget;

/**
 * Represents a property for widgets, like text, enabled state, image, ...
 *
 * Used to apply the property to the given widget in the {@link #apply(Widget)}
 * methods.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #apply(Widget)}.
 * </p>
 *
 * @param <T> the type of the widget the property is used for
 *
 * @noimplement this interface is not intended to be implemented by clients.
 * @noextend this class is not intended to be subclassed by clients.
 *
 * @since 3.18
 */
@FunctionalInterface
public interface Property<T extends Widget> {

	/**
	 * Called when the widget is created and the property should be applied.
	 */
	void apply(T widget);
}