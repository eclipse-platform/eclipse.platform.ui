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

import org.eclipse.swt.widgets.Widget;

/**
 * Represents a supplier for widgets.
 *
 * Used to create a Widget (e.g. Button) in a given parent Widget (e.g.
 * Composite)
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #create(Widget)}.
 * </p>
 *
 * @param <W> the type of the widget to be created
 * @param <P> the type of the parent the widget should be created in
 */
@FunctionalInterface
public interface WidgetSupplier<W extends Widget, P extends Widget> {

	/**
	 * @param parent widget
	 * @return the created widget
	 */
	W create(P parent);
}
